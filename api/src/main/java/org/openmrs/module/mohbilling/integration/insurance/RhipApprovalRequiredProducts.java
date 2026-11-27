package org.openmrs.module.mohbilling.integration.insurance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.integration.IntegrationResponse;
import org.openmrs.module.mohbilling.utils.RhicCode;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The procedures insurers must approve before they are billed (RHIP Unified Insurance Integration Guide: MMI, section
 * 7.1), kept in moh_bill_approval_required_product. The insurances concerned are listed by id in
 * {@link #INSURANCES_PROPERTY}; each one's RHIP insurance type (mmi, rama, cbhi...) is the one whose eligibility setting
 * (rwandaemr.insuranceEligibility.&lt;type&gt;) lists it. RHIP publishes one list per type, kept once per type. A
 * scheduled task refreshes the table from RHIP /approval-required-products; everything else reads the table, so forms,
 * orders and vouchers never wait for RHIP and keep working while it is down.
 * <p>
 * Each row is linked to the billing item (moh_bill_facility_service_price) with the same rhic_code, which tells which
 * billing items need approval and which required codes have no billing item.
 */
public class RhipApprovalRequiredProducts {

	/**
	 * Ids of the insurances (moh_bill_insurance) whose patients' procedures may need approval, separated by commas
	 */
	public static final String INSURANCES_PROPERTY = "mohbilling.approvalRequired.insurances";

	/**
	 * The insurances of each RHIP insurance type, as set for the eligibility check: ids, names or categories
	 */
	static final String ELIGIBILITY_PREFIX = "rwandaemr.insuranceEligibility.";

	/**
	 * The RHIP insurance types
	 */
	static final String[] RHIP_INSURANCE_TYPES = { "mmi", "rama", "cbhi", "special_case" };

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final String TABLE = "moh_bill_approval_required_product";

	private final Log log = LogFactory.getLog(getClass());

	private RhipVoucherProvider voucherProvider;

	private RhipVoucherIntegrationConfig config;

	private SessionFactory sessionFactory;

	public void setVoucherProvider(RhipVoucherProvider voucherProvider) {
		this.voucherProvider = voucherProvider;
	}

	public void setConfig(RhipVoucherIntegrationConfig config) {
		this.config = config;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @return the procedures the insurer must approve, by upper-case code with their name, or null when the list has
	 * never been obtained from RHIP for that insurance
	 */
	@Transactional(readOnly = true)
	public Map<String, String> getProducts(String insuranceType, String facilityFosaId) {
		String type = normalizeType(insuranceType);
		if (type == null) {
			return null;
		}
		NativeQuery<?> query = sessionFactory.getCurrentSession().createSQLQuery(
				"select product_code, product_name, removed from " + TABLE + " where insurance_type = :type");
		query.setParameter("type", type);
		List<?> rows = query.list();
		if (rows.isEmpty()) {
			return null;
		}
		Map<String, String> ret = new LinkedHashMap<String, String>();
		for (Object row : rows) {
			Object[] values = (Object[]) row;
			if (!isTrue(values[2])) {
				ret.put(String.valueOf(values[0]), values[1] == null ? null : values[1].toString());
			}
		}
		return Collections.unmodifiableMap(ret);
	}

	/**
	 * @return the codes the insurer must approve, or null when the list has never been obtained from RHIP
	 */
	@Transactional(readOnly = true)
	public java.util.Set<String> getCodes(String insuranceType, String facilityFosaId) {
		Map<String, String> products = getProducts(insuranceType, facilityFosaId);
		return products == null ? null : products.keySet();
	}

	/**
	 * @return the list for display: every row of the insurance (or all insurances when blank), with its billing item
	 */
	@Transactional(readOnly = true)
	public List<Row> getRows(String insuranceType) {
		String type = normalizeType(insuranceType);
		NativeQuery<?> query = sessionFactory.getCurrentSession().createSQLQuery(
				"select a.insurance_type, a.product_code, a.product_name, a.product_type, a.tariff_price, a.removed, "
						+ "a.first_seen, a.last_seen, f.facility_service_price_id, f.name "
						+ "from " + TABLE + " a left join moh_bill_facility_service_price f "
						+ "on f.facility_service_price_id = a.facility_service_price_id "
						+ (type == null ? "" : "where a.insurance_type = :type ")
						+ "order by a.insurance_type, a.removed, a.product_code");
		if (type != null) {
			query.setParameter("type", type);
		}
		List<Row> ret = new ArrayList<Row>();
		for (Object row : query.list()) {
			Object[] v = (Object[]) row;
			ret.add(new Row(str(v[0]), str(v[1]), str(v[2]), str(v[3]), decimal(v[4]), isTrue(v[5]), (Date) v[6],
					(Date) v[7], v[8] == null ? null : ((Number) v[8]).intValue(), str(v[9])));
		}
		return ret;
	}

	/**
	 * @return the insurances whose approval this billing item needs, e.g. [mmi]
	 */
	@Transactional(readOnly = true)
	public List<String> getInsuranceTypesRequiringApproval(String rhicCode) {
		String code = RhicCode.normalize(rhicCode);
		List<String> ret = new ArrayList<String>();
		if (code == null) {
			return ret;
		}
		NativeQuery<?> query = sessionFactory.getCurrentSession().createSQLQuery(
				"select insurance_type from " + TABLE + " where product_code = :code and removed = false order by insurance_type");
		query.setParameter("code", code);
		for (Object type : query.list()) {
			ret.add(String.valueOf(type));
		}
		return ret;
	}

	/**
	 * @return the ids of the insurances in {@link #INSURANCES_PROPERTY}
	 */
	public List<Integer> getInsuranceIds() {
		List<Integer> ret = new ArrayList<Integer>();
		String configured = Context.getAdministrationService().getGlobalProperty(INSURANCES_PROPERTY, "");
		for (String value : StringUtils.defaultString(configured).split("[,;\\s]+")) {
			try {
				Integer id = Integer.valueOf(value.trim());
				if (!ret.contains(id)) {
					ret.add(id);
				}
			}
			catch (NumberFormatException ignored) {
			}
		}
		return ret;
	}

	/**
	 * @return the RHIP insurance type of the insurance when it is in {@link #INSURANCES_PROPERTY}, else null: only
	 * those insurances' patients have their procedures checked for approval
	 */
	public String getInsuranceTypeIfChecked(org.openmrs.module.mohbilling.model.Insurance insurance) {
		if (insurance == null || insurance.getInsuranceId() == null || !getInsuranceIds().contains(insurance.getInsuranceId())) {
			return null;
		}
		return rhipInsuranceType(insurance);
	}

	/**
	 * @return the RHIP insurance type whose eligibility setting lists this insurance (by id, name or category), or null
	 */
	public static String rhipInsuranceType(org.openmrs.module.mohbilling.model.Insurance insurance) {
		if (insurance == null) {
			return null;
		}
		for (String type : RHIP_INSURANCE_TYPES) {
			String listed = Context.getAdministrationService().getGlobalProperty(ELIGIBILITY_PREFIX + type, "");
			for (String value : StringUtils.defaultString(listed).split(",")) {
				String v = value.trim();
				if (!v.isEmpty() && (v.equals(String.valueOf(insurance.getInsuranceId())) || v.equalsIgnoreCase(insurance.getName())
						|| v.equalsIgnoreCase(insurance.getCategory()))) {
					return type;
				}
			}
		}
		return null;
	}

	/**
	 * @return the RHIP insurance types of the insurances in {@link #INSURANCES_PROPERTY}, whose lists are refreshed
	 */
	public List<String> getInsuranceTypes() {
		List<String> ret = new ArrayList<String>();
		for (Integer id : getInsuranceIds()) {
			org.openmrs.module.mohbilling.model.Insurance insurance = Context.getService(
					org.openmrs.module.mohbilling.service.BillingService.class).getInsurance(id);
			String type = rhipInsuranceType(insurance);
			if (type == null) {
				log.warn("Insurance " + id + " in " + INSURANCES_PROPERTY + " is in no " + ELIGIBILITY_PREFIX
						+ "<type> setting, so its RHIP insurance type is unknown");
			}
			else if (!ret.contains(type)) {
				ret.add(type);
			}
		}
		return ret;
	}

	/**
	 * Refreshes the list of every configured insurance
	 *
	 * @return one line per insurance telling what happened
	 */
	@Transactional
	public List<String> refreshAll() {
		List<String> ret = new ArrayList<String>();
		for (String type : getInsuranceTypes()) {
			ret.add(refresh(type));
		}
		return ret;
	}

	/**
	 * Replaces an insurance's list with RHIP's. Nothing changes when RHIP cannot be reached, or when it answers with an
	 * empty list while the table has codes (treated as a fault rather than "nothing needs approval"). A code RHIP no
	 * longer lists is marked removed, not deleted.
	 *
	 * @return what happened, e.g. "mmi: 198 procedures (2 added, 1 removed)"
	 */
	@Transactional
	public String refresh(String insuranceType) {
		String type = normalizeType(insuranceType);
		String fosaId = config == null ? null : StringUtils.trimToNull(config.getDefaultFosaId());
		if (type == null || fosaId == null) {
			return insuranceType + ": not refreshed, the facility FOSA ID (" + RhipVoucherIntegrationConfig.DEFAULT_FOSA_ID
					+ ") is not set";
		}
		List<Product> products = fetch(type, fosaId);
		if (products == null) {
			return type + ": not refreshed, RHIP did not give the list; the last list is kept";
		}
		int active = count(type, false);
		if (products.isEmpty() && active > 0) {
			log.warn("RHIP returned no procedure needing " + type + " approval while " + active
					+ " are known; the last list is kept");
			return type + ": not refreshed, RHIP returned an empty list; the last list of " + active + " is kept";
		}
		Date now = new Date();
		int added = 0;
		for (Product product : products) {
			if (update(type, product, now) == 0) {
				insert(type, product, now);
				added++;
			}
		}
		NativeQuery<?> remove = sessionFactory.getCurrentSession().createSQLQuery(
				"update " + TABLE + " set removed = true where insurance_type = :type and removed = false and last_seen < :now");
		remove.setParameter("type", type);
		remove.setParameter("now", now);
		int removed = remove.executeUpdate();
		linkBillingItems(type);
		String result = type + ": " + products.size() + " procedures (" + added + " added, " + removed + " removed)";
		log.info("Approval-required procedures refreshed from RHIP: " + result);
		return result;
	}

	/**
	 * Links each row to the active billing item with its code
	 */
	private void linkBillingItems(String type) {
		NativeQuery<?> query = sessionFactory.getCurrentSession().createSQLQuery(
				"update " + TABLE + " a set facility_service_price_id = (select min(f.facility_service_price_id) "
						+ "from moh_bill_facility_service_price f where f.retired = 0 and f.rhic_code = a.product_code) "
						+ "where a.insurance_type = :type");
		query.setParameter("type", type);
		query.executeUpdate();
	}

	private int update(String type, Product product, Date now) {
		NativeQuery<?> query = sessionFactory.getCurrentSession().createSQLQuery(
				"update " + TABLE + " set product_name = :name, product_type = :productType, tariff_price = :tariff, "
						+ "removed = false, last_seen = :now where insurance_type = :type and product_code = :code");
		bind(query, type, product, now);
		return query.executeUpdate();
	}

	private void insert(String type, Product product, Date now) {
		NativeQuery<?> query = sessionFactory.getCurrentSession().createSQLQuery(
				"insert into " + TABLE + " (insurance_type, product_code, product_name, product_type, tariff_price, removed, "
						+ "first_seen, last_seen) values (:type, :code, :name, :productType, :tariff, false, :now, :now)");
		bind(query, type, product, now);
		query.executeUpdate();
	}

	private void bind(NativeQuery<?> query, String type, Product product, Date now) {
		query.setParameter("type", type);
		query.setParameter("code", product.code);
		query.setParameter("name", StringUtils.abbreviate(product.name, 500));
		query.setParameter("productType", StringUtils.abbreviate(product.type, 100));
		query.setParameter("tariff", product.tariff);
		query.setParameter("now", now);
	}

	private int count(String type, boolean removed) {
		NativeQuery<?> query = sessionFactory.getCurrentSession().createSQLQuery(
				"select count(*) from " + TABLE + " where insurance_type = :type and removed = :removed");
		query.setParameter("type", type);
		query.setParameter("removed", removed);
		return ((Number) query.uniqueResult()).intValue();
	}

	private List<Product> fetch(String insuranceType, String facilityFosaId) {
		if (voucherProvider == null) {
			return null;
		}
		try {
			IntegrationResponse response = voucherProvider.getApprovalRequiredProducts(insuranceType, facilityFosaId);
			Integer code = response == null ? null : response.getResponseCode();
			if (code == null || code < 200 || code >= 300) {
				log.warn("Unable to get the procedures " + insuranceType + " must approve ("
						+ (code == null ? "no answer" : "HTTP " + code) + "); the last list is kept");
				return null;
			}
			return readProducts(toJson(response.getResponseEntity()));
		}
		catch (Exception e) {
			log.warn("Unable to get the procedures " + insuranceType + " must approve: " + e.getMessage()
					+ "; the last list is kept");
			return null;
		}
	}

	/**
	 * @return the products in an /approval-required-products answer, or null when it is not a successful answer
	 */
	static List<Product> readProducts(JsonNode root) {
		if (root == null || (root.has("success") && !root.get("success").asBoolean(false))) {
			return null;
		}
		JsonNode products = root.path("data").path("products");
		if (!products.isArray()) {
			return null;
		}
		Map<String, Product> ret = new LinkedHashMap<String, Product>();
		for (JsonNode product : products) {
			String productCode = product.hasNonNull("productCode") ? product.get("productCode").asText() : null;
			String code = RhicCode.normalize(productCode);
			if (code == null && StringUtils.isNotBlank(productCode)) {
				code = productCode.trim().toUpperCase();
			}
			if (code == null) {
				continue;
			}
			BigDecimal tariff = null;
			if (product.hasNonNull("tariffPrice")) {
				try {
					tariff = new BigDecimal(product.get("tariffPrice").asText());
				}
				catch (NumberFormatException ignored) {
				}
			}
			String name = product.hasNonNull("productName") ? product.get("productName").asText() : null;
			String productType = product.hasNonNull("productType") ? product.get("productType").asText() : null;
			Product first = ret.get(code);
			if (first != null) {
				// Listed twice: keep the first, completed with what the second adds
				name = StringUtils.defaultIfBlank(first.name, name);
				productType = StringUtils.defaultIfBlank(first.type, productType);
				tariff = first.tariff != null ? first.tariff : tariff;
			}
			ret.put(code, new Product(code, name, productType, tariff));
		}
		return new ArrayList<Product>(ret.values());
	}

	static String normalizeType(String insuranceType) {
		String type = StringUtils.trimToNull(insuranceType);
		return type == null ? null : type.toLowerCase();
	}

	private JsonNode toJson(Object entity) {
		if (entity instanceof JsonNode) {
			return (JsonNode) entity;
		}
		try {
			return entity == null ? null : OBJECT_MAPPER.readTree(entity.toString());
		}
		catch (Exception e) {
			return null;
		}
	}

	private static boolean isTrue(Object value) {
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return value instanceof Number && ((Number) value).intValue() != 0;
	}

	private static String str(Object value) {
		return value == null ? null : value.toString();
	}

	private static BigDecimal decimal(Object value) {
		if (value == null) {
			return null;
		}
		return value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(value.toString());
	}

	static class Product {

		final String code;

		final String name;

		final String type;

		final BigDecimal tariff;

		Product(String code, String name, String type, BigDecimal tariff) {
			this.code = code;
			this.name = name;
			this.type = type;
			this.tariff = tariff;
		}
	}

	/**
	 * A procedure needing approval, with the billing item it is linked to
	 */
	public static class Row {

		private final String insuranceType;

		private final String productCode;

		private final String productName;

		private final String productType;

		private final BigDecimal tariffPrice;

		private final boolean removed;

		private final Date firstSeen;

		private final Date lastSeen;

		private final Integer facilityServicePriceId;

		private final String facilityServiceName;

		Row(String insuranceType, String productCode, String productName, String productType, BigDecimal tariffPrice,
		    boolean removed, Date firstSeen, Date lastSeen, Integer facilityServicePriceId, String facilityServiceName) {
			this.insuranceType = insuranceType;
			this.productCode = productCode;
			this.productName = productName;
			this.productType = productType;
			this.tariffPrice = tariffPrice;
			this.removed = removed;
			this.firstSeen = firstSeen;
			this.lastSeen = lastSeen;
			this.facilityServicePriceId = facilityServicePriceId;
			this.facilityServiceName = facilityServiceName;
		}

		public String getInsuranceType() {
			return insuranceType;
		}

		public String getProductCode() {
			return productCode;
		}

		public String getProductName() {
			return productName;
		}

		public String getProductType() {
			return productType;
		}

		public BigDecimal getTariffPrice() {
			return tariffPrice;
		}

		/**
		 * @return true when RHIP no longer lists it
		 */
		public boolean isRemoved() {
			return removed;
		}

		public Date getFirstSeen() {
			return firstSeen;
		}

		public Date getLastSeen() {
			return lastSeen;
		}

		/**
		 * @return the billing item with this code, or null when the facility has none
		 */
		public Integer getFacilityServicePriceId() {
			return facilityServicePriceId;
		}

		public String getFacilityServiceName() {
			return facilityServiceName;
		}
	}
}
