package org.openmrs.module.mohbilling.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.integration.IntegrationResponse;
import org.openmrs.module.mohbilling.integration.insurance.RhipVoucherIntegrationConfig;
import org.openmrs.module.mohbilling.integration.insurance.RhipVoucherService;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.businesslogic.ConsommationUtil;
import org.openmrs.module.mohbilling.businesslogic.FileExporter;
import org.openmrs.module.mohbilling.businesslogic.GlobalBillUtil;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MohBillingViewGlobalBillController extends
			ParameterizableViewController {
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private RhipVoucherService voucherService;
	private RhipVoucherIntegrationConfig voucherIntegrationConfig;
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.ParameterizableViewController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
												 HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName(getViewName());

		String globalBillStr = request.getParameter("globalBillId");
		Integer globalBillId = Integer.valueOf(request.getParameter("globalBillId"));
		List<ServiceRevenue> serviceRevenueList = new ArrayList<ServiceRevenue>();
		Consommation consommation =null;

		if(globalBillStr!=null && !globalBillStr.equals("")){
			GlobalBill globalBill= GlobalBillUtil.getGlobalBill(Integer.parseInt(globalBillStr));
			PatientIdentifier patientIdentifier= GlobalBillUtil.getGlobalBill(Integer.parseInt(globalBillStr)).getAdmission().getInsurancePolicy().getOwner().getPatientIdentifier(3);

			List<Consommation> consommations = ConsommationUtil.getConsommationsByGlobalBill(globalBill);
			List<PatientServiceBill> allItems = new ArrayList<PatientServiceBill>();
			for (Consommation c : consommations) {
				for (PatientServiceBill item : c.getBillItems()) {
					if (!item.getVoided()) {
						allItems.add(item);
					}
				}
			}
			List<HopService> revenuesCategories = GlobalPropertyConfig.getHospitalServiceByCategory("mohbilling.REVENUE");
			for (HopService hopService : revenuesCategories) {
				if(ReportsUtil.getServiceRevenues(allItems, hopService)!=null)
					serviceRevenueList.add(ReportsUtil.getServiceRevenues(allItems, hopService));

			}
			ServiceRevenue imagingRevenue = ReportsUtil.getServiceRevenue(allItems, "mohbilling.IMAGING");
			if(imagingRevenue!=null)
				serviceRevenueList.add(imagingRevenue);

			ServiceRevenue actsRevenue = ReportsUtil.getServiceRevenue(allItems, "mohbilling.ACTS");
			if(actsRevenue!=null)
				serviceRevenueList.add(actsRevenue);
			ServiceRevenue actsDCPRevenue = ReportsUtil.getServiceRevenue(allItems, "mohbilling.DCPACTS");
			if(actsDCPRevenue!=null)
				serviceRevenueList.add(actsDCPRevenue);

			ServiceRevenue autreRevenue = ReportsUtil.getServiceRevenue(allItems, "mohbilling.AUTRES");
			if(autreRevenue!=null)
				serviceRevenueList.add(autreRevenue);

			Map<Integer, String> billItemStatusesByItemId = getBillItemStatusesByItemId(globalBill, allItems);
			Map<String, Object> transferSummary = getTransferSummary(globalBill);
			mav.addObject("globalBill", globalBill);
			mav.addObject("patientIdentifier",patientIdentifier);
			mav.addObject("showRhipVoucherButton", shouldShowRhipVoucherButton(globalBill));
			mav.addObject("rhipVoucherItemRecordsByItemId", getLatestRhipVoucherItemRecordsByItemId(globalBill));
			mav.addObject("billItemStatusesByItemId", billItemStatusesByItemId);
			mav.addObject("transferSummary", transferSummary);
			mav.addObject("transferETransferId", transferSummary.get("eTransferId"));
			mav.addObject("transferValidFrom", transferSummary.get("transferValidFrom"));
			mav.addObject("transferringFacility", transferSummary.get("transferringFacility"));
			request.setAttribute("billItemStatusesByItemId", billItemStatusesByItemId);
			request.setAttribute("transferSummary", transferSummary);
			request.getSession().setAttribute("globalBill" , globalBill);

		}
		mav.addObject("serviceRevenueList", serviceRevenueList);
		request.getSession().setAttribute("serviceRevenueList" , serviceRevenueList);

		if(request.getParameter("print")!=null){
			GlobalBill gb = GlobalBillUtil.getGlobalBill(Integer.valueOf(request.getParameter("globalBillId")));
			FileExporter exp = new FileExporter();
			List<ServiceRevenue> sr = (List<ServiceRevenue>) request.getSession().getAttribute("serviceRevenueList" );

			String finalDiagnosisConceptQuestion=Context.getAdministrationService().getGlobalProperty("billing.finalDiagnosisConceptQuestionIDsTobeDisplayedOnGlobalBill");
			String differentialDiagnosisConceptQuestion=Context.getAdministrationService().getGlobalProperty("billing.differentialDiagnosisConceptQuestionIDsTobeDisplayedOnGlobalBill");

			Date diagnosisStartDate = getStartOfDay(gb.getAdmission().getAdmissionDate());
			Date diagnosisEndDate = getEndOfDay(gb.getClosingDate() == null ? new Date() : gb.getClosingDate());
			Integer patientId = gb.getAdmission().getInsurancePolicy().getOwner().getPatientId();

			String finalDiagnosis=GlobalBillUtil.getDiagnosisFromAdmissionToDischarge(finalDiagnosisConceptQuestion,diagnosisStartDate,diagnosisEndDate,patientId);
			String differentialDiagnosis=GlobalBillUtil.getDiagnosisFromAdmissionToDischarge(differentialDiagnosisConceptQuestion,diagnosisStartDate,diagnosisEndDate,patientId);

			exp.printGlobalBill(request, response, gb,differentialDiagnosis+"",finalDiagnosis+"",sr, gb.getBillIdentifier()+".pdf");
		}
		if(request.getParameter("revert_global_bill")!=null && Context.isAuthenticated()){
			GlobalBill gb = GlobalBillUtil.getGlobalBill(Integer.parseInt(request.getParameter("globalBillId")));
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String closingDate = df.format(gb.getClosingDate());
			String currDate = df.format(new Date());
			if(closingDate.equals(currDate)==true) {
				gb.setClosed(false);
				gb.setClosedBy(null);
				gb.setClosingDate(null);
				gb.setClosingReason(null);
				gb.setEditedBy(Context.getAuthenticatedUser());
				gb.setEditingReason(request.getParameter("editGBill"));
				GlobalBillUtil.saveGlobalBill(gb);
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,"Global Bill Reverted, You are allowed to change it");
				return new ModelAndView(new RedirectView("globalBill.list?insurancePolicyId=" + gb.getAdmission().getInsurancePolicy() +
						"&ipCardNumber=" + gb.getAdmission().getInsurancePolicy().getInsuranceCardNo()));
			}
			else {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,"Only Globall Bill closed TODAY can be reverted ");
			}
		}
		if (request.getParameter("send_voucher") != null && Context.isAuthenticated()) {
			GlobalBill gb = GlobalBillUtil.getGlobalBill(Integer.parseInt(request.getParameter("globalBillId")));
			if (!Boolean.TRUE.equals(gb.getClosed())) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Global Bill must be discharged before sending a voucher");
			} else if (StringUtils.isNotBlank(gb.getRhipVoucherCode()) || StringUtils.isNotBlank(gb.getRhipVoucherReferenceNumber())) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "RHIP voucher already exists for this global bill");
			} else if (voucherService == null) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "RHIP voucher service is not configured");
				} else {
					IntegrationResponse voucherResponse = voucherService.submitVoucherForGlobalBill(gb);
					if (voucherResponse == null) {
						request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
								"RHIP voucher submission failed: no response received");
						log.warn("RHIP voucher submission failed: no response received");
					} else if (!voucherResponse.isEnabled()) {
						request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
								"RHIP voucher submission failed: integration is disabled");
						log.warn("RHIP voucher submission failed: integration is disabled");
					} else if (voucherResponse.getErrorMessage() != null) {
						String friendlyError = toFriendlyVoucherError(voucherResponse);
						request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, friendlyError);
						log.warn("Error submitting RHIP voucher: " + friendlyError);
					} else if (voucherResponse.getResponseCode() == null && voucherResponse.getResponseEntity() == null) {
						request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
								"RHIP voucher submission failed: empty response from RHIP");
						log.warn("RHIP voucher submission failed: empty response from RHIP");
					} else if (voucherResponse.getResponseEntity() instanceof java.util.Map) {
						processVoucherResponse(request, gb, voucherResponse.getResponseEntity(), voucherResponse.getResponseCode());
					} else if (voucherResponse.getResponseEntity() instanceof String) {
						processVoucherResponse(request, gb, voucherResponse.getResponseEntity(), voucherResponse.getResponseCode());
					} else {
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "RHIP voucher submitted successfully");
				}
			}
			return new ModelAndView(new RedirectView("viewGlobalBill.form?globalBillId=" + gb.getGlobalBillId()));
		}
		return mav;
	}

	private Map<String, Object> getTransferSummary(GlobalBill globalBill) {
		Map<String, Object> summary = new HashMap<String, Object>();
		if (globalBill == null || globalBill.getAdmission() == null
				|| globalBill.getAdmission().getInsurancePolicy() == null
				|| globalBill.getAdmission().getInsurancePolicy().getOwner() == null) {
			return summary;
		}
		Integer patientId = globalBill.getAdmission().getInsurancePolicy().getOwner().getPatientId();
		if (patientId == null || !tableExists("transfers")) {
			return summary;
		}

		Map<String, String> transferColumns = columnsByLowerName("transfers");
		String patientColumn = firstColumn(transferColumns, "patient_id");
		if (StringUtils.isBlank(patientColumn)) {
			return summary;
		}
		String transferIdColumn = firstColumn(transferColumns, "transfer_id", "id", "uuid");
		String eTransferIdColumn = firstColumn(transferColumns, "e_transfer_id", "etransfer_id",
				"e_transfer_identifier", "transfer_identifier", "identifier", "reference_number", "reference");
		String emrIdColumn = firstColumn(transferColumns, "emr_id", "emr_identifier", "emr_uuid");
		String validFromColumn = firstColumn(transferColumns, "decision_to_transfer_at", "transfer_valid_from",
				"valid_from", "transfer_date", "date_transferred", "date_created");
		String facilityColumn = firstColumn(transferColumns, "sending_facility", "transferring_facility",
				"from_facility", "origin_facility", "source_facility", "facility_name");
		String dateCreatedColumn = firstColumn(transferColumns, "date_created");
		String voidedColumn = firstColumn(transferColumns, "voided");

		String orderColumn = StringUtils.defaultIfBlank(validFromColumn, dateCreatedColumn);
		if (StringUtils.isBlank(orderColumn)) {
			orderColumn = transferIdColumn;
		}
		String sql = "select " + selectNullableColumn(eTransferIdColumn) + ", "
				+ selectNullableColumn(emrIdColumn) + ", "
				+ selectNullableColumn(transferIdColumn) + ", "
				+ selectNullableColumn(validFromColumn) + ", "
				+ selectNullableColumn(facilityColumn)
				+ " from transfers where " + q(patientColumn) + " = " + patientId
				+ (StringUtils.isBlank(voidedColumn) ? "" : " and coalesce(" + q(voidedColumn) + ", 0) = 0")
				+ (StringUtils.isBlank(orderColumn) ? "" : " order by " + q(orderColumn) + " desc")
				+ (StringUtils.isBlank(transferIdColumn) ? "" : (StringUtils.isBlank(orderColumn) ? " order by " : ", ")
						+ q(transferIdColumn) + " desc")
				+ " limit 1";
		List<List<Object>> rows = executeSql(sql);
		if (rows.isEmpty()) {
			return summary;
		}

		List<Object> row = rows.get(0);
		String eTransferId = stringValue(value(row, 0));
		String emrId = stringValue(value(row, 1));
		String transferId = stringValue(value(row, 2));
		summary.put("eTransferId", StringUtils.defaultIfBlank(eTransferId, buildETransferId(emrId, transferId)));
		summary.put("transferValidFrom", dateValue(value(row, 3)));
		summary.put("transferringFacility", stringValue(value(row, 4)));
		return summary;
	}

	private String buildETransferId(String emrId, String transferId) {
		if (StringUtils.isBlank(emrId) && StringUtils.isBlank(transferId)) {
			return null;
		}
		if (StringUtils.isBlank(emrId)) {
			return transferId;
		}
		if (StringUtils.isBlank(transferId)) {
			return emrId;
		}
		return emrId + " - " + transferId;
	}

	private Map<Integer, String> getBillItemStatusesByItemId(GlobalBill globalBill, List<PatientServiceBill> billItems) {
		if (billItems == null || billItems.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<Integer, String> statuses = new HashMap<Integer, String>();
		for (PatientServiceBill billItem : billItems) {
			if (billItem == null || billItem.getPatientServiceBillId() == null) {
				continue;
			}
			String status = resolveBillItemStatus(globalBill, billItem);
			if (StringUtils.isNotBlank(status)) {
				statuses.put(billItem.getPatientServiceBillId(), status);
			}
		}
		return statuses;
	}

	private String resolveBillItemStatus(GlobalBill globalBill, PatientServiceBill billItem) {
		String categoryName = resolveCategoryName(billItem);
		if (isPharmacyCategory(categoryName)) {
			String pharmacyStatus = resolveRwandaPharmacyStatus(billItem);
			if (StringUtils.isNotBlank(pharmacyStatus)) {
				return pharmacyStatus;
			}
		}
		if (isLaboratoryOrImagingCategory(categoryName, billItem)) {
			String orderStatus = resolveOpenmrsOrderStatus(globalBill, billItem);
			if (StringUtils.isNotBlank(orderStatus)) {
				return orderStatus;
			}
		}
		return null;
	}

	private String resolveRwandaPharmacyStatus(PatientServiceBill billItem) {
		if (billItem == null || billItem.getConsommation() == null || !tableExists("rpm_patient_dispensing_request")
				|| !tableExists("rpm_patient_dispensing_request_line")) {
			return null;
		}
		Integer patientBillId = billItem.getConsommation().getPatientBill() == null ? null
				: billItem.getConsommation().getPatientBill().getPatientBillId();
		if (patientBillId == null) {
			return null;
		}

		Map<String, String> requestColumns = columnsByLowerName("rpm_patient_dispensing_request");
		Map<String, String> lineColumns = columnsByLowerName("rpm_patient_dispensing_request_line");
		String requestIdColumn = firstColumn(requestColumns, "patient_dispensing_request_id", "id");
		String requestPatientBillIdColumn = firstColumn(requestColumns, "patient_bill_id", "bill_id");
		String requestStatusColumn = firstColumn(requestColumns, "request_status", "status");
		String lineIdColumn = firstColumn(lineColumns, "patient_dispensing_request_line_id", "id", "line_id");
		String lineRequestIdColumn = firstColumn(lineColumns, "patient_dispensing_request_id", "request_id");
		String lineStatusColumn = firstColumn(lineColumns, "line_status", "status");
		String linePatientServiceBillIdColumn = firstColumn(lineColumns, "patient_service_bill_id", "bill_item_id");
		String lineFacilityServicePriceIdColumn = firstColumn(lineColumns, "facility_service_price_id", "service_price_id");
		if (StringUtils.isBlank(requestIdColumn) || StringUtils.isBlank(requestPatientBillIdColumn)
				|| StringUtils.isBlank(lineIdColumn) || StringUtils.isBlank(lineRequestIdColumn)) {
			return null;
		}

		List<String> itemConditions = new ArrayList<String>();
		if (StringUtils.isNotBlank(linePatientServiceBillIdColumn)) {
			itemConditions.add("l." + q(linePatientServiceBillIdColumn) + " = " + billItem.getPatientServiceBillId());
		}
		Integer facilityServicePriceId = resolveFacilityServicePriceId(billItem);
		if (facilityServicePriceId != null && StringUtils.isNotBlank(lineFacilityServicePriceIdColumn)) {
			itemConditions.add("l." + q(lineFacilityServicePriceIdColumn) + " = " + facilityServicePriceId);
		}
		if (itemConditions.isEmpty()) {
			return null;
		}

		String sql = "select " + selectNullableColumnWithAlias("l", lineStatusColumn) + ", "
				+ selectNullableColumnWithAlias("r", requestStatusColumn)
				+ " from rpm_patient_dispensing_request r "
				+ "join rpm_patient_dispensing_request_line l on l." + q(lineRequestIdColumn)
				+ " = r." + q(requestIdColumn)
				+ " where r." + q(requestPatientBillIdColumn) + " = " + patientBillId
				+ " and coalesce(r.voided, 0) = 0 and coalesce(l.voided, 0) = 0"
				+ " and (" + StringUtils.join(itemConditions, " or ") + ")"
				+ " order by l." + q(lineIdColumn) + " desc limit 1";
		List<List<Object>> rows = executeSql(sql);
		if (rows.isEmpty()) {
			return null;
		}
		String lineStatus = stringValue(value(rows.get(0), 0));
		String requestStatus = stringValue(value(rows.get(0), 1));
		return displayStatus(StringUtils.defaultIfBlank(lineStatus, requestStatus));
	}

	private String resolveOpenmrsOrderStatus(GlobalBill globalBill, PatientServiceBill billItem) {
		Integer patientId = globalBill == null || globalBill.getAdmission() == null
				|| globalBill.getAdmission().getInsurancePolicy() == null
				|| globalBill.getAdmission().getInsurancePolicy().getOwner() == null ? null
				: globalBill.getAdmission().getInsurancePolicy().getOwner().getPatientId();
		Integer conceptId = resolveConceptId(billItem);
		if (patientId == null || conceptId == null || !tableExists("orders")) {
			return null;
		}

		Map<String, String> orderColumns = columnsByLowerName("orders");
		String orderIdColumn = firstColumn(orderColumns, "order_id");
		String conceptColumn = firstColumn(orderColumns, "concept_id");
		String patientColumn = firstColumn(orderColumns, "patient_id");
		String dateColumn = firstColumn(orderColumns, "date_activated", "start_date", "date_created");
		String fulfillerStatusColumn = firstColumn(orderColumns, "fulfiller_status");
		String actionColumn = firstColumn(orderColumns, "action");
		String discontinuedColumn = firstColumn(orderColumns, "discontinued");
		String voidedColumn = firstColumn(orderColumns, "voided");
		if (StringUtils.isBlank(orderIdColumn) || StringUtils.isBlank(conceptColumn)
				|| StringUtils.isBlank(patientColumn)) {
			return null;
		}

		String sql = "select " + q(orderIdColumn) + ", " + selectNullableColumn(fulfillerStatusColumn)
				+ ", " + selectNullableColumn(actionColumn) + ", " + selectNullableColumn(discontinuedColumn)
				+ " from orders where " + q(patientColumn) + " = " + patientId
				+ " and " + q(conceptColumn) + " = " + conceptId
				+ (StringUtils.isBlank(voidedColumn) ? "" : " and coalesce(" + q(voidedColumn) + ", 0) = 0")
				+ dateWindowClause(dateColumn, billItem.getServiceDate(), globalBill)
				+ " order by " + (StringUtils.isBlank(dateColumn) ? q(orderIdColumn) : q(dateColumn))
				+ " desc, " + q(orderIdColumn) + " desc limit 1";
		List<List<Object>> rows = executeSql(sql);
		if (rows.isEmpty()) {
			return null;
		}
		Integer orderId = integerValue(value(rows.get(0), 0));
		String fulfillerStatus = stringValue(value(rows.get(0), 1));
		String action = stringValue(value(rows.get(0), 2));
		Boolean discontinued = booleanValue(value(rows.get(0), 3));
		if (StringUtils.isNotBlank(fulfillerStatus)) {
			return displayStatus(fulfillerStatus);
		}
		if (Boolean.TRUE.equals(discontinued)) {
			return "Discontinued";
		}
		if (orderHasResults(orderId)) {
			return "Result available";
		}
		return displayStatus(StringUtils.defaultIfBlank(action, "Ordered"));
	}

	private boolean orderHasResults(Integer orderId) {
		if (orderId == null || !tableExists("obs")) {
			return false;
		}
		Map<String, String> obsColumns = columnsByLowerName("obs");
		String orderColumn = firstColumn(obsColumns, "order_id");
		String voidedColumn = firstColumn(obsColumns, "voided");
		if (StringUtils.isBlank(orderColumn)) {
			return false;
		}
		String sql = "select obs_id from obs where " + q(orderColumn) + " = " + orderId
				+ (StringUtils.isBlank(voidedColumn) ? "" : " and coalesce(" + q(voidedColumn) + ", 0) = 0")
				+ " limit 1";
		return !executeSql(sql).isEmpty();
	}

	private Date getStartOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	private Date getEndOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	public void setVoucherService(RhipVoucherService voucherService) {
		this.voucherService = voucherService;
	}

	public void setVoucherIntegrationConfig(RhipVoucherIntegrationConfig voucherIntegrationConfig) {
		this.voucherIntegrationConfig = voucherIntegrationConfig;
	}

	private Map<Integer, RhipVoucherItemRecord> getLatestRhipVoucherItemRecordsByItemId(GlobalBill globalBill) {
		Map<Integer, RhipVoucherItemRecord> recordsByItemId = new HashMap<Integer, RhipVoucherItemRecord>();
		if (globalBill == null) {
			return recordsByItemId;
		}
		try {
			BillingService billingService = Context.getService(BillingService.class);
			List<RhipVoucherItemRecord> records = billingService.getRhipVoucherItemRecordsByGlobalBill(globalBill);
			if (records == null) {
				return recordsByItemId;
			}
			for (RhipVoucherItemRecord record : records) {
				if (record == null || record.getPatientServiceBill() == null
						|| record.getPatientServiceBill().getPatientServiceBillId() == null) {
					continue;
				}
				Integer itemId = record.getPatientServiceBill().getPatientServiceBillId();
				if (!recordsByItemId.containsKey(itemId)) {
					recordsByItemId.put(itemId, record);
				}
			}
		} catch (Exception e) {
			log.warn("Unable to load RHIP voucher item records for global bill " + globalBill.getGlobalBillId(), e);
		}
		return recordsByItemId;
	}

	private boolean shouldShowRhipVoucherButton(GlobalBill globalBill) {
		if (globalBill == null || !Boolean.TRUE.equals(globalBill.getClosed())) {
			return false;
		}
		if (StringUtils.isNotBlank(globalBill.getRhipVoucherCode())
				|| StringUtils.isNotBlank(globalBill.getRhipVoucherReferenceNumber())) {
			return false;
		}
		Admission admission = globalBill.getAdmission();
		InsurancePolicy insurancePolicy = admission == null ? null : admission.getInsurancePolicy();
		Insurance insurance = insurancePolicy == null ? null : insurancePolicy.getInsurance();
		if (insurance == null) {
			return false;
		}
		RhipVoucherIntegrationConfig config = voucherIntegrationConfig == null
				? new RhipVoucherIntegrationConfig()
				: voucherIntegrationConfig;
		return config.isVoucherButtonEnabledForInsurance(insurance.getCategory(), insurance.getName());
	}

	private void processVoucherResponse(HttpServletRequest request, GlobalBill globalBill, Object responseEntity, Integer responseCode) {
		String responseBody = toJsonString(responseEntity);
		JsonNode root = toJsonNode(responseEntity);

		String success = jsonValueString(root == null ? null : root.get("success"));
		String message = jsonValueString(root == null ? null : root.get("message"));
		String failureMessage = extractRhipFailureMessage(root);

		JsonNode data = root == null ? null : root.get("data");
		String voucherCode = jsonValueString(data == null ? null : data.get("voucherCode"));
		String voucherReferenceNumber = jsonValueString(data == null ? null : data.get("voucherReferenceNumber"));
		String status = jsonValueString(data == null ? null : data.get("status"));

		log.info("RHIP voucher response received (status=" + responseCode
				+ ", success=" + success
				+ ", message=" + message
				+ ", failureMessage=" + failureMessage
				+ ", voucherCode=" + voucherCode
				+ ", voucherReferenceNumber=" + voucherReferenceNumber
				+ ", voucherStatus=" + status
				+ ")");
		if ("true".equalsIgnoreCase(success)) {
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					StringUtils.isBlank(message) ? "RHIP voucher submitted successfully" : message);
			log.info("RHIP voucher submitted successfully");
			if (voucherCode != null) {
				request.getSession().setAttribute("rhipVoucherCode", voucherCode);
			}
			if (voucherReferenceNumber != null) {
				request.getSession().setAttribute("rhipVoucherReferenceNumber", voucherReferenceNumber);
			}
			if (status != null) {
				request.getSession().setAttribute("rhipVoucherStatus", status);
			}
			if (globalBill != null && (voucherCode != null || voucherReferenceNumber != null)) {
				globalBill.setRhipVoucherCode(voucherCode);
				globalBill.setRhipVoucherReferenceNumber(voucherReferenceNumber);
				try {
					GlobalBillUtil.saveGlobalBill(globalBill);
				} catch (Exception e) {
					log.warn("Unable to persist RHIP voucher details for global bill " + globalBill.getGlobalBillId(), e);
				}
			}
		} else {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					StringUtils.isBlank(failureMessage) ? "RHIP voucher submission failed" : failureMessage);
			log.warn("RHIP voucher submission failed"
					+ (StringUtils.isBlank(failureMessage) ? "" : ": " + failureMessage));
		}
	}

	private JsonNode toJsonNode(Object responseEntity) {
		if (responseEntity == null) {
			return null;
		}
		if (responseEntity instanceof JsonNode) {
			return (JsonNode) responseEntity;
		}
		if (responseEntity instanceof String) {
			String body = ((String) responseEntity).trim();
			try {
				JsonNode node = OBJECT_MAPPER.readTree(body);
				if (node != null && node.isTextual()) {
					String inner = node.asText();
					try {
						return OBJECT_MAPPER.readTree(inner);
					} catch (Exception ignored) {
						return node;
					}
				}
				return node;
			} catch (Exception ignored) {
			}

			String normalized = body;
			if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() >= 2) {
				normalized = normalized.substring(1, normalized.length() - 1);
			}
			normalized = normalized.replace("\\\"", "\"").replace("\\\\", "\\");
			try {
				return OBJECT_MAPPER.readTree(normalized);
			} catch (Exception ignored) {
				return null;
			}
		}
		try {
			return OBJECT_MAPPER.valueToTree(responseEntity);
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	private String jsonValueString(JsonNode node) {
		if (node == null || node.isNull()) {
			return null;
		}
		if (node.isTextual()) {
			return node.asText();
		}
		if (node.isBoolean()) {
			return String.valueOf(node.booleanValue());
		}
		if (node.isNumber()) {
			return node.numberValue().toString();
		}
		return node.toString();
	}

	private String toJsonString(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof String) {
			return (String) value;
		}
		if (value instanceof JsonNode) {
			return value.toString();
		}
		try {
			return OBJECT_MAPPER.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			return String.valueOf(value);
		}
	}

	private String toFriendlyVoucherError(IntegrationResponse response) {
		String message = extractRhipMessage(response);
		if (StringUtils.isBlank(message)) {
			String raw = response == null ? null : response.getErrorMessage();
			return StringUtils.isBlank(raw) ? "Unable to submit RHIP voucher. Please try again." : "Error submitting RHIP voucher: " + raw;
		}
		String normalized = message.toLowerCase();
		if (normalized.contains("at least one diagnosis id")
				|| normalized.contains("icd-11")
				|| normalized.contains("diagnosis")) {
			return "Unable to submit RHIP voucher: at least one ICD-11 diagnosis code is required. "
					+ "Please enter/select a diagnosis with code format like XM1QR3 and try again.";
		}
		if (normalized.contains("treatmentfornewborn")) {
			return "Unable to submit RHIP voucher: treatment for newborn must be set to Yes or No.";
		}
		if (normalized.contains("receptionnumber")) {
			return "Unable to submit RHIP voucher: MMI reception number is required. "
					+ "Please create MMI patient reception first, then try again.";
		}
		return "Unable to submit RHIP voucher: " + message;
	}

	private String extractRhipMessage(IntegrationResponse response) {
		if (response == null) {
			return null;
		}
		String fromEntity = extractMessageFromJsonString(toJsonString(response.getResponseEntity()));
		if (StringUtils.isNotBlank(fromEntity)) {
			return fromEntity;
		}
		String errorMessage = response.getErrorMessage();
		if (StringUtils.isBlank(errorMessage)) {
			return null;
		}
		int firstBrace = errorMessage.indexOf('{');
		if (firstBrace >= 0) {
			String jsonPart = errorMessage.substring(firstBrace);
			String fromErrorJson = extractMessageFromJsonString(jsonPart);
			if (StringUtils.isNotBlank(fromErrorJson)) {
				return fromErrorJson;
			}
		}
		return errorMessage;
	}

	private String extractMessageFromJsonString(String json) {
		if (StringUtils.isBlank(json)) {
			return null;
		}
		try {
			JsonNode node = OBJECT_MAPPER.readTree(json);
			return extractRhipFailureMessage(node);
		}
		catch (Exception ignored) {
			return null;
		}
	}

	private String extractRhipFailureMessage(JsonNode root) {
		String detailedErrors = extractRhipErrorMessages(root == null ? null : root.get("errors"));
		if (StringUtils.isNotBlank(detailedErrors)) {
			return detailedErrors;
		}
		return jsonValueString(root == null ? null : root.get("message"));
	}

	private String extractRhipErrorMessages(JsonNode errorsNode) {
		if (errorsNode == null || errorsNode.isNull()) {
			return null;
		}
		List<String> messages = new ArrayList<String>();
		if (errorsNode.isArray()) {
			for (JsonNode errorNode : errorsNode) {
				String message = null;
				if (errorNode != null && errorNode.isObject()) {
					message = jsonValueString(errorNode.get("message"));
				} else {
					message = jsonValueString(errorNode);
				}
				message = normalizeRhipErrorMessage(message);
				if (StringUtils.isNotBlank(message) && !messages.contains(message)) {
					messages.add(message);
				}
			}
		} else {
			String message = normalizeRhipErrorMessage(jsonValueString(errorsNode));
			if (StringUtils.isNotBlank(message)) {
				messages.add(message);
			}
		}
		return messages.isEmpty() ? null : StringUtils.join(messages, "; ");
	}

	private String normalizeRhipErrorMessage(String message) {
		if (StringUtils.isBlank(message)) {
			return null;
		}
		String normalized = message.trim();
		if (normalized.startsWith("[") || normalized.startsWith("\"") || normalized.startsWith("'")) {
			try {
				JsonNode parsed = OBJECT_MAPPER.readTree(normalized);
				if (parsed != null && parsed.isArray()) {
					List<String> values = new ArrayList<String>();
					for (JsonNode value : parsed) {
						String parsedValue = normalizeRhipErrorMessage(jsonValueString(value));
						if (StringUtils.isNotBlank(parsedValue)) {
							values.add(parsedValue);
						}
					}
					if (!values.isEmpty()) {
						return StringUtils.join(values, "; ");
					}
				}
				if (parsed != null && parsed.isTextual()) {
					normalized = parsed.asText();
				}
			} catch (Exception ignored) {
			}
		}
		normalized = normalized.trim();
		while (normalized.startsWith("[") || normalized.startsWith("\"") || normalized.startsWith("'")) {
			normalized = normalized.substring(1).trim();
		}
		while (normalized.endsWith("]") || normalized.endsWith("\"") || normalized.endsWith("'")) {
			normalized = normalized.substring(0, normalized.length() - 1).trim();
		}
		return normalized;
	}

	private String dateWindowClause(String dateColumn, Date serviceDate, GlobalBill globalBill) {
		if (StringUtils.isBlank(dateColumn)) {
			return "";
		}
		Calendar calendar = Calendar.getInstance();
		Date start = null;
		Date end = null;
		if (serviceDate != null) {
			calendar.setTime(serviceDate);
			calendar.add(Calendar.DATE, -1);
			start = calendar.getTime();
			calendar.setTime(serviceDate);
			calendar.add(Calendar.DATE, 2);
			end = calendar.getTime();
		} else if (globalBill != null && globalBill.getAdmission() != null) {
			start = globalBill.getAdmission().getAdmissionDate();
			end = globalBill.getClosingDate() == null ? new Date() : globalBill.getClosingDate();
		}
		if (start == null || end == null) {
			return "";
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return " and " + q(dateColumn) + " >= '" + df.format(start) + "'"
				+ " and " + q(dateColumn) + " < '" + df.format(end) + "'";
	}

	private boolean isPharmacyCategory(String categoryName) {
		if (StringUtils.isBlank(categoryName)) {
			return false;
		}
		String normalized = categoryName.toLowerCase();
		return normalized.contains("medicament") || normalized.contains("pharm")
				|| normalized.contains("drug") || normalized.contains("consommable")
				|| normalized.contains("consumable");
	}

	private boolean isLaboratoryOrImagingCategory(String categoryName, PatientServiceBill billItem) {
		String normalized = StringUtils.defaultString(categoryName).toLowerCase();
		if (normalized.contains("lab") || normalized.contains("laboratoire")
				|| normalized.contains("imaging") || normalized.contains("imagerie")
				|| normalized.contains("radiology") || normalized.contains("radiologie")
				|| normalized.contains("xray") || normalized.contains("x-ray")) {
			return true;
		}
		String hopService = billItem == null || billItem.getHopService() == null ? null : billItem.getHopService().getName();
		normalized = StringUtils.defaultString(hopService).toLowerCase();
		return normalized.contains("lab") || normalized.contains("laboratoire")
				|| normalized.contains("imaging") || normalized.contains("imagerie")
				|| normalized.contains("radiology") || normalized.contains("radiologie");
	}

	private String resolveCategoryName(PatientServiceBill billItem) {
		if (billItem == null || billItem.getService() == null || billItem.getService().getServiceCategory() == null) {
			return null;
		}
		return StringUtils.trimToNull(billItem.getService().getServiceCategory().getName());
	}

	private Integer resolveFacilityServicePriceId(PatientServiceBill billItem) {
		if (billItem == null || billItem.getService() == null || billItem.getService().getFacilityServicePrice() == null) {
			return null;
		}
		return billItem.getService().getFacilityServicePrice().getFacilityServicePriceId();
	}

	private Integer resolveConceptId(PatientServiceBill billItem) {
		if (billItem == null || billItem.getService() == null || billItem.getService().getFacilityServicePrice() == null
				|| billItem.getService().getFacilityServicePrice().getConcept() == null) {
			return null;
		}
		return billItem.getService().getFacilityServicePrice().getConcept().getConceptId();
	}

	@SuppressWarnings("unchecked")
	private List<List<Object>> executeSql(String sql) {
		if (StringUtils.isBlank(sql)) {
			return new ArrayList<List<Object>>();
		}
		try {
			return Context.getAdministrationService().executeSQL(sql, true);
		}
		catch (Exception e) {
			log.debug("Unable to execute global bill lookup SQL", e);
			return new ArrayList<List<Object>>();
		}
	}

	private boolean tableExists(String tableName) {
		if (!safeIdentifier(tableName)) {
			return false;
		}
		return !executeSql("select table_name from information_schema.tables where table_schema = database()"
				+ " and table_name = '" + escapeSql(tableName) + "' limit 1").isEmpty();
	}

	private Map<String, String> columnsByLowerName(String tableName) {
		Map<String, String> columns = new LinkedHashMap<String, String>();
		if (!safeIdentifier(tableName)) {
			return columns;
		}
		String sql = "select column_name from information_schema.columns where table_schema = database()"
				+ " and table_name = '" + escapeSql(tableName) + "'";
		for (List<Object> row : executeSql(sql)) {
			String column = stringValue(value(row, 0));
			if (StringUtils.isNotBlank(column)) {
				columns.put(column.toLowerCase(), column);
			}
		}
		return columns;
	}

	private String firstColumn(Map<String, String> columns, String... candidates) {
		if (columns == null || candidates == null) {
			return null;
		}
		for (String candidate : candidates) {
			String column = columns.get(candidate);
			if (StringUtils.isNotBlank(column)) {
				return column;
			}
		}
		return null;
	}

	private String selectNullableColumn(String column) {
		return StringUtils.isBlank(column) ? "null" : q(column);
	}

	private String selectNullableColumnWithAlias(String alias, String column) {
		return StringUtils.isBlank(column) ? "null" : alias + "." + q(column);
	}

	private Object value(List<Object> row, int index) {
		return row == null || row.size() <= index ? null : row.get(index);
	}

	private Integer integerValue(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		try {
			return Integer.valueOf(value.toString());
		}
		catch (Exception ignored) {
			return null;
		}
	}

	private Boolean booleanValue(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue() != 0;
		}
		String text = value.toString().trim();
		if ("1".equals(text)) {
			return Boolean.TRUE;
		}
		if ("0".equals(text)) {
			return Boolean.FALSE;
		}
		return Boolean.valueOf(text);
	}

	private Date dateValue(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Date) {
			return (Date) value;
		}
		String text = stringValue(value);
		if (StringUtils.isBlank(text)) {
			return null;
		}
		String[] patterns = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"};
		for (String pattern : patterns) {
			try {
				return new SimpleDateFormat(pattern).parse(text.length() > pattern.length()
						? text.substring(0, pattern.length()) : text);
			}
			catch (Exception ignored) {
			}
		}
		return null;
	}

	private String stringValue(Object value) {
		return value == null ? null : StringUtils.trimToNull(value.toString());
	}

	private String displayStatus(String status) {
		if (StringUtils.isBlank(status)) {
			return null;
		}
		String normalized = status.trim().replace('_', ' ').replace('-', ' ').toLowerCase();
		StringBuilder display = new StringBuilder();
		for (String token : normalized.split("\\s+")) {
			if (StringUtils.isBlank(token)) {
				continue;
			}
			if (display.length() > 0) {
				display.append(' ');
			}
			display.append(Character.toUpperCase(token.charAt(0)));
			if (token.length() > 1) {
				display.append(token.substring(1));
			}
		}
		return display.length() == 0 ? null : display.toString();
	}

	private String escapeSql(String value) {
		return value == null ? null : value.replace("'", "''");
	}

	private String q(String identifier) {
		if (!safeIdentifier(identifier)) {
			throw new IllegalArgumentException("Unsafe SQL identifier: " + identifier);
		}
		return "`" + identifier + "`";
	}

	private boolean safeIdentifier(String identifier) {
		return StringUtils.isNotBlank(identifier) && identifier.matches("[A-Za-z0-9_]+");
	}

}
