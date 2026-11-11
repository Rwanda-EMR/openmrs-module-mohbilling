package org.openmrs.module.mohbilling.businesslogic;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.Department;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.Proforma;
import org.openmrs.module.mohbilling.model.ProformaItem;
import org.openmrs.module.mohbilling.service.BillingService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Business logic for proforma invoices (price estimates issued before treatment).
 * Proformas never create or modify GlobalBill / Consommation records.
 */
public class ProformaUtil {

	public static final String GLOBAL_PROPERTY_VALIDITY_DAYS = "billing.proformaValidityDays";
	private static final int DEFAULT_VALIDITY_DAYS = 30;

	private static BillingService getService() {
		return Context.getService(BillingService.class);
	}

	/**
	 * Services billable under the given insurance on the given date, sorted by category then name.
	 * The price of each service is its insurance-specific tariff ({@code maximaToPay}).
	 */
	public static List<BillableService> getActiveBillableServices(Insurance insurance, Date date) {
		List<BillableService> services = new ArrayList<BillableService>();
		if (insurance == null) {
			return services;
		}
		for (BillableService bs : getService().getBillableServicesByInsurance(insurance)) {
			if (isActive(bs, date)) {
				services.add(bs);
			}
		}
		Collections.sort(services, new Comparator<BillableService>() {
			@Override
			public int compare(BillableService a, BillableService b) {
				int ret = StringUtils.defaultString(categoryName(a)).compareToIgnoreCase(StringUtils.defaultString(categoryName(b)));
				if (ret == 0) {
					ret = StringUtils.defaultString(serviceName(a)).compareToIgnoreCase(StringUtils.defaultString(serviceName(b)));
				}
				return ret;
			}
		});
		return services;
	}

	private static boolean isActive(BillableService bs, Date date) {
		if (bs == null || Boolean.TRUE.equals(bs.getRetired()) || bs.getMaximaToPay() == null) {
			return false;
		}
		FacilityServicePrice fsp = bs.getFacilityServicePrice();
		if (fsp == null || fsp.isRetired()) {
			return false;
		}
		if (bs.getStartDate() != null && bs.getStartDate().after(date)) {
			return false;
		}
		return bs.getEndDate() == null || bs.getEndDate().after(date);
	}

	public static String serviceName(BillableService bs) {
		return bs.getFacilityServicePrice() != null ? bs.getFacilityServicePrice().getName() : null;
	}

	public static String categoryName(BillableService bs) {
		return bs.getServiceCategory() != null ? bs.getServiceCategory().getName() : null;
	}

	/**
	 * Builds and saves a new proforma. Prices, rate and flat fee are snapshotted at issue time.
	 *
	 * @param quantities billable service id -> quantity (entries with quantity &lt;= 0 are ignored)
	 */
	public static Proforma createProforma(Patient patient, Insurance insurance, Department department,
			String addressee, String notes, Map<Integer, BigDecimal> quantities) {
		if (patient == null) {
			throw new APIException("Patient is required");
		}
		if (insurance == null) {
			throw new APIException("Insurance is required");
		}
		Date now = new Date();
		Proforma proforma = new Proforma();
		proforma.setPatient(patient);
		proforma.setInsurance(insurance);
		proforma.setDepartment(department);
		proforma.setAddressee(StringUtils.trimToNull(addressee));
		proforma.setNotes(StringUtils.trimToNull(notes));
		proforma.setStatus(Proforma.STATUS_ISSUED);
		proforma.setCreator(Context.getAuthenticatedUser());
		proforma.setCreatedDate(now);
		proforma.setValidUntil(computeValidUntil(now));
		proforma.setUuid(UUID.randomUUID().toString());

		InsuranceRate rate = insurance.getRateOnDate(now);
		proforma.setInsuranceRate(rate != null ? rate.getRate() : null);
		proforma.setFlatFee(rate != null ? rate.getFlatFee() : null);

		BigDecimal total = BigDecimal.ZERO;
		if (quantities != null) {
			for (Map.Entry<Integer, BigDecimal> entry : quantities.entrySet()) {
				BigDecimal quantity = entry.getValue();
				if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
					continue;
				}
				BillableService bs = getService().getBillableService(entry.getKey());
				if (bs == null || bs.getInsurance() == null
						|| !insurance.getInsuranceId().equals(bs.getInsurance().getInsuranceId()) || !isActive(bs, now)) {
					throw new APIException("Service " + entry.getKey() + " is not billable under " + insurance.getName());
				}
				ProformaItem item = new ProformaItem();
				item.setBillableService(bs);
				item.setServiceName(serviceName(bs));
				item.setCategoryName(categoryName(bs));
				item.setUnitPrice(bs.getMaximaToPay().setScale(2, RoundingMode.HALF_UP));
				item.setQuantity(quantity);
				item.setLineTotal(item.getUnitPrice().multiply(quantity).setScale(2, RoundingMode.HALF_UP));
				item.setUuid(UUID.randomUUID().toString());
				proforma.addItem(item);
				total = total.add(item.getLineTotal());
			}
		}
		if (proforma.getItems().isEmpty()) {
			throw new APIException("Add at least one service to the proforma");
		}

		BigDecimal[] shares = calculateShares(total, proforma.getInsuranceRate(), proforma.getFlatFee());
		proforma.setTotalAmount(total);
		proforma.setInsuranceAmount(shares[0]);
		proforma.setPatientAmount(shares[1]);

		getService().saveProforma(proforma);
		proforma.setIdentifier(buildIdentifier(proforma));
		return getService().saveProforma(proforma);
	}

	/**
	 * Insurance vs patient split, using the same rule as the printed global bill: when a flat fee
	 * is configured the patient pays the flat fee (capped at the total), otherwise the percentage
	 * rate applies.
	 *
	 * @return [insuranceAmount, patientAmount]
	 */
	public static BigDecimal[] calculateShares(BigDecimal totalAmount, Float insuranceRate, BigDecimal flatFee) {
		BigDecimal total = totalAmount != null ? totalAmount : BigDecimal.ZERO;
		if (flatFee != null && flatFee.compareTo(BigDecimal.ZERO) > 0) {
			BigDecimal patientDue = flatFee.min(total).setScale(2, RoundingMode.HALF_UP);
			return new BigDecimal[] { total.subtract(patientDue).setScale(2, RoundingMode.HALF_UP), patientDue };
		}
		float rate = insuranceRate != null ? insuranceRate : 0f;
		BigDecimal insuranceDue = total.multiply(BigDecimal.valueOf(rate))
				.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
		return new BigDecimal[] { insuranceDue, total.subtract(insuranceDue).setScale(2, RoundingMode.HALF_UP) };
	}

	public static Proforma cancelProforma(Proforma proforma, String reason) {
		if (proforma == null) {
			throw new APIException("Proforma not found");
		}
		if (proforma.isCancelled()) {
			return proforma;
		}
		if (StringUtils.isBlank(reason)) {
			throw new APIException("A cancellation reason is required");
		}
		proforma.setStatus(Proforma.STATUS_CANCELLED);
		proforma.setCancelledBy(Context.getAuthenticatedUser());
		proforma.setCancelledDate(new Date());
		proforma.setCancelReason(reason.trim());
		return getService().saveProforma(proforma);
	}

	private static Date computeValidUntil(Date from) {
		int days = DEFAULT_VALIDITY_DAYS;
		String gp = Context.getAdministrationService().getGlobalProperty(GLOBAL_PROPERTY_VALIDITY_DAYS);
		if (StringUtils.isNotBlank(gp)) {
			try {
				days = Integer.parseInt(gp.trim());
			}
			catch (NumberFormatException ignored) {
				// keep default
			}
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(from);
		cal.add(Calendar.DAY_OF_MONTH, days);
		return cal.getTime();
	}

	private static String buildIdentifier(Proforma proforma) {
		String shortCode = StringUtils.trimToNull(Context.getAdministrationService().getGlobalProperty(
				BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_SHORT_CODE));
		String year = new SimpleDateFormat("yyyy").format(proforma.getCreatedDate());
		return "PF-" + (shortCode != null ? shortCode + "-" : "") + year + "-"
				+ String.format("%06d", proforma.getProformaId());
	}
}
