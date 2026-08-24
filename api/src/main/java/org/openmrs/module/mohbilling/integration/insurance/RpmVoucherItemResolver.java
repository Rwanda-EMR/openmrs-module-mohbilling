package org.openmrs.module.mohbilling.integration.insurance;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Admission;
import org.openmrs.module.mohbilling.model.BillableService;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.FacilityServicePrice;
import org.openmrs.module.mohbilling.model.GlobalBill;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RpmVoucherItemResolver {
	
	private static final Log log = LogFactory.getLog(RpmVoucherItemResolver.class);
	
	private static final String MEDICAMENTS_SERVICE_CATEGORY = "MEDICAMENTS";
	private static final String CONSOMMABLES_SERVICE_CATEGORY = "CONSOMMABLES";
	private static final String RAMA_INSURANCE_TYPE = "RAMA";
	private static final String MMI_INSURANCE_TYPE = "MMI";
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	
	private static final String REQUEST_TABLE_GP = "mohbilling.rpm.dispensingRequestTable";
	private static final String LINE_TABLE_GP = "mohbilling.rpm.dispensingLineTable";
	private static final String ITEM_TABLE_GP = "mohbilling.rpm.itemTable";
	private static final String REQUEST_ID_COLUMN_GP = "mohbilling.rpm.request.idColumn";
	private static final String REQUEST_PATIENT_BILL_ID_COLUMN_GP = "mohbilling.rpm.request.patientBillIdColumn";
	private static final String REQUEST_REFERENCE_ID_COLUMN_GP = "mohbilling.rpm.request.referenceIdColumn";
	private static final String REQUEST_ORDER_ID_COLUMN_GP = "mohbilling.rpm.request.orderIdColumn";
	private static final String LINE_ID_COLUMN_GP = "mohbilling.rpm.line.idColumn";
	private static final String LINE_REQUEST_ID_COLUMN_GP = "mohbilling.rpm.line.requestIdColumn";
	private static final String LINE_ITEM_ID_COLUMN_GP = "mohbilling.rpm.line.itemIdColumn";
	private static final String LINE_PATIENT_SERVICE_BILL_ID_COLUMN_GP = "mohbilling.rpm.line.patientServiceBillIdColumn";
	private static final String LINE_FACILITY_SERVICE_PRICE_ID_COLUMN_GP = "mohbilling.rpm.line.facilityServicePriceIdColumn";
	private static final String LINE_DISPENSING_DATE_COLUMN_GP = "mohbilling.rpm.line.dispensingDateColumn";
	private static final String ITEM_ID_COLUMN_GP = "mohbilling.rpm.item.idColumn";
	private static final String ITEM_CODE_COLUMN_GP = "mohbilling.rpm.item.codeColumn";
	private static final String ITEM_TYPE_COLUMN_GP = "mohbilling.rpm.item.typeColumn";
	
	public boolean supports(PatientServiceBill billItem) {
		String categoryName = resolveCategoryName(billItem);
		return MEDICAMENTS_SERVICE_CATEGORY.equalsIgnoreCase(categoryName)
				|| CONSOMMABLES_SERVICE_CATEGORY.equalsIgnoreCase(categoryName);
	}
	
	public RhipVoucherProcedure resolve(GlobalBill globalBill, Consommation consommation, PatientServiceBill billItem,
	                                    Admission admission, String insuranceType) {
		if (!supports(billItem)) {
			return null;
		}
		RpmSchema schema = resolveSchema();
		if (schema == null || !schema.isUsable()) {
			return resolveByItemName(consommation, billItem, admission);
		}
		List<RpmRequest> requests = findRequests(schema, globalBill, consommation);
		if (requests == null || requests.isEmpty()) {
			log.debug("No Rwanda Pharma dispensing request found for bill; globalBillId="
					+ id(globalBill == null ? null : globalBill.getGlobalBillId()) + ", patientBillId="
					+ id(resolvePatientBillId(consommation)));
			return resolveByItemName(consommation, billItem, admission);
		}
		List<RpmLine> matchingLines = findMatchingLines(schema, requests, billItem);
		if (matchingLines.isEmpty()) {
			return resolveByItemName(consommation, billItem, admission);
		}
		if (matchingLines.size() > 1) {
			throw new IllegalStateException("More than one RPM dispensing line matches PatientServiceBill "
					+ id(billItem == null ? null : billItem.getPatientServiceBillId()));
		}
		RpmLine line = matchingLines.get(0);
		RpmItem item = findItem(schema, line.itemId);
		if (item == null) {
			return resolveByItemName(consommation, billItem, admission);
		}
		if (StringUtils.isBlank(item.code)) {
			return resolveByItemName(consommation, billItem, admission);
		}
		RhipVoucherProcedure procedure = new RhipVoucherProcedure();
		procedure.setCode(item.code.trim());
		procedure.setQuantity(defaultIfNull(billItem.getQuantity()));
		procedure.setPrice(resolvePrice(billItem));
		procedure.setPrescribedAt(resolvePrescribedAt(billItem, admission));
		Date dispensingDate = resolveDispensingDate(requests, consommation, item.itemId);
		procedure.setDispensingDate(formatDate(dispensingDate == null ? line.dispensingDate : dispensingDate));
		if ((isRamaInsuranceType(insuranceType) || isMmiInsuranceType(insuranceType)) && isDrugItem(item, line)) {
			applyDrugOrderDetails(procedure, line.orderId);
		}
		return procedure;
	}

	private RhipVoucherProcedure resolveByItemName(Consommation consommation, PatientServiceBill billItem,
	                                             Admission admission) {
		RpmItem item = findItemByBillingName(billItem);
		if (item == null) {
			throw new IllegalStateException("Unable to find an active RPM item with NPC code matching billing item name '"
					+ defaultIfBlank(resolveBillingItemName(billItem), "<missing>") + "'");
		}
		RhipVoucherProcedure procedure = new RhipVoucherProcedure();
		procedure.setCode(item.code.trim());
		procedure.setQuantity(defaultIfNull(billItem == null ? null : billItem.getQuantity()));
		procedure.setPrice(resolvePrice(billItem));
		procedure.setPrescribedAt(resolvePrescribedAt(billItem, admission));
		procedure.setDispensingDate(formatDate(resolveDispensingDate(null, consommation, item.itemId)));
		return procedure;
	}

	private Date resolveDispensingDate(List<RpmRequest> requests, Consommation consommation, Integer itemId) {
		if (itemId == null) {
			return null;
		}
		List<String> requestIds = new ArrayList<>();
		if (requests != null) {
			for (RpmRequest request : requests) {
				if (request != null && request.requestId != null) {
					requestIds.add(request.requestId.toString());
				}
			}
		}
		Integer patientBillId = resolvePatientBillId(consommation);
		if (requestIds.isEmpty() && patientBillId == null) {
			return null;
		}
		if (!tableExists("rpm_dispense_record") || !tableExists("rpm_dispense_record_line")) {
			return null;
		}

		StringBuilder sql = new StringBuilder("select coalesce(dr.date_created, drl.date_created) "
				+ "from rpm_dispense_record dr "
				+ "join rpm_dispense_record_line drl on drl.dispense_record_id = dr.dispense_record_id ");
		if (requestIds.isEmpty()) {
			if (!tableExists("rpm_patient_dispensing_request")) {
				return null;
			}
			sql.append("join rpm_patient_dispensing_request r on r.patient_dispensing_request_id = "
					+ "dr.patient_dispensing_request_id ");
		}
		sql.append("where drl.item_id = ").append(itemId)
				.append(" and coalesce(dr.voided, 0) = 0 and coalesce(drl.voided, 0) = 0 ");
		if (requestIds.isEmpty()) {
			sql.append("and r.patient_bill_id = ").append(patientBillId)
					.append(" and coalesce(r.voided, 0) = 0 ");
		} else {
			sql.append("and dr.patient_dispensing_request_id in (")
					.append(StringUtils.join(requestIds, ",")).append(") ");
		}
		sql.append("order by coalesce(dr.date_created, drl.date_created) desc limit 1");
		List<List<Object>> rows = executeSql(sql.toString());
		return rows.isEmpty() ? null : dateValue(value(rows.get(0), 0));
	}

	private RpmItem findItemByBillingName(PatientServiceBill billItem) {
		String itemTable = "rpm_item";
		if (!tableExists(itemTable)) {
			return null;
		}
		Map<String, String> columns = columnsByLowerName(itemTable);
		String itemIdColumn = firstColumn(columns, "item_id", "id", "rpm_item_id");
		String itemNameColumn = firstColumn(columns, "name", "item_name");
		String itemCodeColumn = firstColumn(columns, "code", "npc_code", "insurance_code", "rhip_code");
		String voidedColumn = firstColumn(columns, "voided");
		if (StringUtils.isBlank(itemIdColumn) || StringUtils.isBlank(itemNameColumn)
				|| StringUtils.isBlank(itemCodeColumn)) {
			return null;
		}

		Set<String> candidateNames = resolveBillingItemNames(billItem);
		if (candidateNames.isEmpty()) {
			return null;
		}
		List<String> nameConditions = new ArrayList<>();
		for (String candidateName : candidateNames) {
			nameConditions.add("lower(trim(" + q(itemNameColumn) + ")) = lower('"
					+ escapeSql(candidateName.trim()) + "')");
		}
		String sql = "select " + q(itemIdColumn) + ", " + q(itemCodeColumn) + ", " + q(itemNameColumn)
				+ " from " + q(itemTable) + " where " + joinOr(nameConditions)
				+ (StringUtils.isBlank(voidedColumn) ? "" : " and coalesce(" + q(voidedColumn) + ", 0) = 0")
				+ " order by " + q(itemIdColumn);
		List<List<Object>> rows = executeSql(sql);
		if (rows.isEmpty()) {
			return null;
		}

		Set<String> npcCodes = new LinkedHashSet<>();
		for (List<Object> row : rows) {
			String code = stringValue(value(row, 1));
			if (StringUtils.isNotBlank(code)) {
				npcCodes.add(code.trim());
			}
		}
		if (npcCodes.isEmpty()) {
			throw new IllegalStateException("RPM item matching billing item name '" + resolveBillingItemName(billItem)
					+ "' has no NPC code");
		}
		if (npcCodes.size() > 1) {
			throw new IllegalStateException("More than one RPM NPC code matches billing item name '"
					+ resolveBillingItemName(billItem) + "': " + StringUtils.join(npcCodes, ", "));
		}
		RpmItem item = new RpmItem();
		item.itemId = integerValue(value(rows.get(0), 0));
		item.code = npcCodes.iterator().next();
		item.name = stringValue(value(rows.get(0), 2));
		return item;
	}

	private Set<String> resolveBillingItemNames(PatientServiceBill billItem) {
		Set<String> names = new LinkedHashSet<>();
		FacilityServicePrice price = resolveFacilityServicePrice(billItem);
		if (price != null) {
			if (StringUtils.isNotBlank(price.getName())) {
				names.add(price.getName().trim());
			}
			if (StringUtils.isNotBlank(price.getShortName())) {
				names.add(price.getShortName().trim());
			}
		}
		return names;
	}

	private String resolveBillingItemName(PatientServiceBill billItem) {
		Set<String> names = resolveBillingItemNames(billItem);
		return names.isEmpty() ? null : names.iterator().next();
	}
	
	private RpmSchema resolveSchema() {
		RpmSchema configured = resolveConfiguredSchema();
		if (configured != null && configured.isUsable()) {
			return configured;
		}
		return discoverSchema();
	}
	
	private RpmSchema resolveConfiguredSchema() {
		RpmSchema schema = new RpmSchema();
		schema.requestTable = gp(REQUEST_TABLE_GP);
		schema.lineTable = gp(LINE_TABLE_GP);
		schema.itemTable = gp(ITEM_TABLE_GP);
		schema.requestIdColumn = defaultIfBlank(gp(REQUEST_ID_COLUMN_GP), "id");
		schema.requestPatientBillIdColumn = gp(REQUEST_PATIENT_BILL_ID_COLUMN_GP);
		schema.requestReferenceIdColumn = gp(REQUEST_REFERENCE_ID_COLUMN_GP);
		schema.requestOrderIdColumn = gp(REQUEST_ORDER_ID_COLUMN_GP);
		schema.lineIdColumn = defaultIfBlank(gp(LINE_ID_COLUMN_GP), "id");
		schema.lineRequestIdColumn = gp(LINE_REQUEST_ID_COLUMN_GP);
		schema.lineItemIdColumn = gp(LINE_ITEM_ID_COLUMN_GP);
		schema.linePatientServiceBillIdColumn = gp(LINE_PATIENT_SERVICE_BILL_ID_COLUMN_GP);
		schema.lineFacilityServicePriceIdColumn = gp(LINE_FACILITY_SERVICE_PRICE_ID_COLUMN_GP);
		schema.lineDispensingDateColumn = gp(LINE_DISPENSING_DATE_COLUMN_GP);
		schema.itemIdColumn = defaultIfBlank(gp(ITEM_ID_COLUMN_GP), "id");
		schema.itemCodeColumn = gp(ITEM_CODE_COLUMN_GP);
		schema.itemTypeColumn = gp(ITEM_TYPE_COLUMN_GP);
		return schema.hasTables() ? schema : null;
	}
	
	private RpmSchema discoverSchema() {
		List<String> requestTables = findTables("dispensing", "request");
		List<String> lineTables = findTables("dispensing", "line");
		List<String> itemTables = findTables("item", null);
		for (String requestTable : requestTables) {
			Map<String, String> requestColumns = columnsByLowerName(requestTable);
			for (String lineTable : lineTables) {
				Map<String, String> lineColumns = columnsByLowerName(lineTable);
				for (String itemTable : itemTables) {
					Map<String, String> itemColumns = columnsByLowerName(itemTable);
					RpmSchema schema = new RpmSchema();
					schema.requestTable = requestTable;
					schema.lineTable = lineTable;
					schema.itemTable = itemTable;
					schema.requestIdColumn = firstColumn(requestColumns, "id", "request_id",
							"patient_dispensing_request_id", "dispensing_request_id");
					schema.requestPatientBillIdColumn = firstColumn(requestColumns, "patient_bill_id", "bill_id",
							"billing_id");
					schema.requestReferenceIdColumn = firstColumn(requestColumns, "reference_id", "bill_reference_id",
							"billing_reference_id", "patient_bill_reference_id");
					schema.requestOrderIdColumn = firstColumn(requestColumns, "order_id");
					schema.lineIdColumn = firstColumn(lineColumns, "id", "line_id", "dispensing_line_id",
							"patient_dispensing_line_id");
					schema.lineRequestIdColumn = firstColumn(lineColumns, "request_id", "dispensing_request_id",
							"patient_dispensing_request_id");
					schema.lineItemIdColumn = firstColumn(lineColumns, "item_id", "rpm_item_id", "product_id",
							"drug_id");
					schema.linePatientServiceBillIdColumn = firstColumn(lineColumns, "patient_service_bill_id",
							"bill_item_id", "billing_item_id");
					schema.lineFacilityServicePriceIdColumn = firstColumn(lineColumns, "facility_service_price_id",
							"service_price_id");
					schema.lineDispensingDateColumn = firstColumn(lineColumns, "dispensing_date", "dispensed_at",
							"date_dispensed", "dispensed_date");
					schema.itemIdColumn = firstColumn(itemColumns, "id", "item_id", "rpm_item_id");
					schema.itemCodeColumn = firstColumn(itemColumns, "code", "insurance_code", "rhip_code");
					schema.itemTypeColumn = firstColumn(itemColumns, "type", "item_type", "category");
					if (schema.isUsable()) {
						return schema;
					}
				}
			}
		}
		return null;
	}
	
	private List<RpmRequest> findRequests(RpmSchema schema, GlobalBill globalBill, Consommation consommation) {
		List<RpmRequest> requests = new ArrayList<>();
		List<String> conditions = new ArrayList<>();
		Integer patientBillId = resolvePatientBillId(consommation);
		String patientBillReference = resolvePatientBillReference(consommation);
		if (patientBillId != null && StringUtils.isNotBlank(schema.requestPatientBillIdColumn)) {
			conditions.add(q(schema.requestPatientBillIdColumn) + " = " + patientBillId);
		}
		if (StringUtils.isNotBlank(patientBillReference) && StringUtils.isNotBlank(schema.requestReferenceIdColumn)) {
			conditions.add(q(schema.requestReferenceIdColumn) + " = '" + escapeSql(patientBillReference) + "'");
		}
		if (globalBill != null && StringUtils.isNotBlank(globalBill.getBillIdentifier())
				&& StringUtils.isNotBlank(schema.requestReferenceIdColumn)) {
			conditions.add(q(schema.requestReferenceIdColumn) + " = '" + escapeSql(globalBill.getBillIdentifier()) + "'");
		}
		if (conditions.isEmpty()) {
			return requests;
		}
		String sql = "select " + q(schema.requestIdColumn) + ", "
				+ selectNullableColumn(schema.requestOrderIdColumn)
				+ " from " + q(schema.requestTable) + " where " + joinOr(conditions);
		for (List<Object> row : executeSql(sql)) {
			RpmRequest request = new RpmRequest();
			request.requestId = integerValue(value(row, 0));
			request.orderId = integerValue(value(row, 1));
			if (request.requestId != null) {
				requests.add(request);
			}
		}
		return requests;
	}
	
	private List<RpmLine> findMatchingLines(RpmSchema schema, List<RpmRequest> requests, PatientServiceBill billItem) {
		List<RpmLine> matches = new ArrayList<>();
		for (RpmRequest request : requests) {
			if (request == null || request.requestId == null) {
				continue;
			}
			for (RpmLine line : findLines(schema, request)) {
				if (matchesBillItem(schema, line, billItem)) {
					if (line.orderId == null) {
						line.orderId = request.orderId;
					}
					matches.add(line);
				}
			}
		}
		return matches;
	}
	
	private List<RpmLine> findLines(RpmSchema schema, RpmRequest request) {
		List<RpmLine> lines = new ArrayList<>();
		String sql = "select " + q(schema.lineIdColumn) + ", " + q(schema.lineItemIdColumn)
				+ ", " + selectNullableColumn(schema.linePatientServiceBillIdColumn)
				+ ", " + selectNullableColumn(schema.lineFacilityServicePriceIdColumn)
				+ ", " + selectNullableColumn(schema.lineDispensingDateColumn)
				+ " from " + q(schema.lineTable)
				+ " where " + q(schema.lineRequestIdColumn) + " = " + request.requestId;
		for (List<Object> row : executeSql(sql)) {
			RpmLine line = new RpmLine();
			line.lineId = integerValue(value(row, 0));
			line.itemId = integerValue(value(row, 1));
			line.patientServiceBillId = integerValue(value(row, 2));
			line.facilityServicePriceId = integerValue(value(row, 3));
			line.dispensingDate = dateValue(value(row, 4));
			line.orderId = request.orderId;
			lines.add(line);
		}
		return lines;
	}
	
	private boolean matchesBillItem(RpmSchema schema, RpmLine line, PatientServiceBill billItem) {
		if (line == null || billItem == null) {
			return false;
		}
		if (StringUtils.isNotBlank(schema.linePatientServiceBillIdColumn) && line.patientServiceBillId != null
				&& billItem.getPatientServiceBillId() != null) {
			return line.patientServiceBillId.equals(billItem.getPatientServiceBillId());
		}
		FacilityServicePrice price = resolveFacilityServicePrice(billItem);
		if (StringUtils.isNotBlank(schema.lineFacilityServicePriceIdColumn) && line.facilityServicePriceId != null
				&& price != null && price.getFacilityServicePriceId() != null) {
			return line.facilityServicePriceId.equals(price.getFacilityServicePriceId());
		}
		return false;
	}
	
	private RpmItem findItem(RpmSchema schema, Integer itemId) {
		if (itemId == null) {
			return null;
		}
		String sql = "select " + q(schema.itemCodeColumn) + ", " + selectNullableColumn(schema.itemTypeColumn)
				+ " from " + q(schema.itemTable)
				+ " where " + q(schema.itemIdColumn) + " = " + itemId;
		List<List<Object>> rows = executeSql(sql);
		if (rows.isEmpty()) {
			return null;
		}
		RpmItem item = new RpmItem();
		item.itemId = itemId;
		item.code = stringValue(value(rows.get(0), 0));
		item.type = stringValue(value(rows.get(0), 1));
		return item;
	}
	
	private void applyDrugOrderDetails(RhipVoucherProcedure procedure, Integer orderId) {
		if (orderId == null || !tableExists("drug_order")) {
			throw new IllegalStateException("Drug order is missing for RAMA RPM drug item");
		}
		Map<String, String> columns = columnsByLowerName("drug_order");
		String doseColumn = firstColumn(columns, "dose");
		String doseUnitsColumn = firstColumn(columns, "dose_units");
		String frequencyColumn = firstColumn(columns, "frequency");
		String durationColumn = firstColumn(columns, "duration");
		String durationUnitsColumn = firstColumn(columns, "duration_units");
		String instructionsColumn = firstColumn(columns, "dosing_instructions", "instructions");
		String sql = "select " + selectNullableColumn(doseColumn)
				+ ", " + selectNullableColumn(doseUnitsColumn)
				+ ", " + selectNullableColumn(frequencyColumn)
				+ ", " + selectNullableColumn(durationColumn)
				+ ", " + selectNullableColumn(durationUnitsColumn)
				+ ", " + selectNullableColumn(instructionsColumn)
				+ " from drug_order where order_id = " + orderId;
		List<List<Object>> rows = executeSql(sql);
		if (rows.isEmpty()) {
			throw new IllegalStateException("Drug order not found for orderId=" + id(orderId));
		}
		List<Object> row = rows.get(0);
		String posology = buildPosology(value(row, 0), value(row, 1), value(row, 5));
		String frequency = displayValue(value(row, 2));
		Integer durationDays = resolveDurationDays(value(row, 3), value(row, 4));
		String instructions = StringUtils.trimToNull(stringValue(value(row, 5)));
		if (StringUtils.isBlank(posology)) {
			throw new IllegalStateException("Dose or posology is missing for orderId=" + id(orderId));
		}
		if (StringUtils.isBlank(frequency)) {
			throw new IllegalStateException("Prescription frequency is missing for orderId=" + id(orderId));
		}
		if (durationDays == null) {
			throw new IllegalStateException("Duration is missing or cannot be converted to days for orderId=" + id(orderId));
		}
		procedure.setPosology(posology);
		procedure.setFrequency(frequency);
		procedure.setDurationDays(durationDays);
		procedure.setInstructions(StringUtils.defaultIfBlank(instructions, posology));
	}
	
	private String buildPosology(Object dose, Object doseUnits, Object fallbackInstructions) {
		String doseValue = stringValue(dose);
		String doseUnitsValue = displayValue(doseUnits);
		if (StringUtils.isNotBlank(doseValue)) {
			return StringUtils.trimToNull((doseValue + " " + defaultIfBlank(doseUnitsValue, "")).trim());
		}
		return StringUtils.trimToNull(stringValue(fallbackInstructions));
	}
	
	private Integer resolveDurationDays(Object duration, Object durationUnits) {
		BigDecimal durationValue = decimalValue(duration);
		if (durationValue == null) {
			return null;
		}
		String units = displayValue(durationUnits);
		if (StringUtils.isBlank(units)) {
			return durationValue.intValue();
		}
		String normalized = units.trim().toLowerCase();
		if (normalized.contains("day") || normalized.equals("d")) {
			return durationValue.intValue();
		}
		if (normalized.contains("week") || normalized.equals("wk") || normalized.equals("w")) {
			return durationValue.multiply(new BigDecimal(7)).intValue();
		}
		if (normalized.contains("hour") || normalized.equals("h")) {
			BigDecimal[] divided = durationValue.divideAndRemainder(new BigDecimal(24));
			if (BigDecimal.ZERO.compareTo(divided[1]) == 0) {
				return divided[0].intValue();
			}
			throw new IllegalStateException("Duration in hours is not an exact number of days");
		}
		return null;
	}
	
	private String displayValue(Object value) {
		String raw = stringValue(value);
		Integer conceptId = integerValue(value);
		if (conceptId != null && tableExists("concept_name")) {
			List<List<Object>> rows = executeSql("select name from concept_name where concept_id = " + conceptId
					+ " and locale_preferred = 1 limit 1");
			if (rows.isEmpty()) {
				rows = executeSql("select name from concept_name where concept_id = " + conceptId + " limit 1");
			}
			if (!rows.isEmpty()) {
				return stringValue(value(rows.get(0), 0));
			}
		}
		return raw;
	}
	
	private List<String> findTables(String required, String optional) {
		List<String> tables = new ArrayList<>();
		String sql = "select table_name from information_schema.tables where table_schema = database()"
				+ " and lower(table_name) regexp '(^|_)(rpm|rwpharma|rwandapharma|pharma)'";
		for (List<Object> row : executeSql(sql)) {
			String table = stringValue(value(row, 0));
			String lower = table == null ? "" : table.toLowerCase();
			if (lower.contains(required) && (optional == null || lower.contains(optional))) {
				tables.add(table);
			}
		}
		return tables;
	}
	
	private boolean tableExists(String tableName) {
		return !executeSql("select table_name from information_schema.tables where table_schema = database()"
				+ " and table_name = '" + escapeSql(tableName) + "' limit 1").isEmpty();
	}
	
	private Map<String, String> columnsByLowerName(String tableName) {
		Map<String, String> columns = new LinkedHashMap<>();
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
	
	@SuppressWarnings("unchecked")
	protected List<List<Object>> executeSql(String sql) {
		if (StringUtils.isBlank(sql)) {
			return new ArrayList<>();
		}
		try {
			return Context.getAdministrationService().executeSQL(sql, true);
		}
		catch (Exception e) {
			log.debug("Unable to execute RPM voucher lookup SQL", e);
			return new ArrayList<>();
		}
	}
	
	private String resolveCategoryName(PatientServiceBill billItem) {
		if (billItem == null || billItem.getService() == null || billItem.getService().getServiceCategory() == null) {
			return null;
		}
		return StringUtils.trimToNull(billItem.getService().getServiceCategory().getName());
	}
	
	private Integer resolvePatientBillId(Consommation consommation) {
		PatientBill patientBill = consommation == null ? null : consommation.getPatientBill();
		return patientBill == null ? null : patientBill.getPatientBillId();
	}
	
	private String resolvePatientBillReference(Consommation consommation) {
		PatientBill patientBill = consommation == null ? null : consommation.getPatientBill();
		return patientBill == null ? null : patientBill.getReferenceId();
	}
	
	private FacilityServicePrice resolveFacilityServicePrice(PatientServiceBill billItem) {
		BillableService service = billItem == null ? null : billItem.getService();
		return service == null ? null : service.getFacilityServicePrice();
	}
	
	private BigDecimal resolvePrice(PatientServiceBill billItem) {
		if (billItem == null) {
			return null;
		}
		if (billItem.getUnitPrice() != null) {
			return billItem.getUnitPrice();
		}
		FacilityServicePrice price = resolveFacilityServicePrice(billItem);
		return price == null ? null : price.getFullPrice();
	}
	
	private String resolvePrescribedAt(PatientServiceBill billItem, Admission admission) {
		Date date = billItem == null ? null : billItem.getServiceDate();
		if (date == null && billItem != null) {
			date = billItem.getCreatedDate();
		}
		if (date == null && admission != null) {
			date = admission.getAdmissionDate();
		}
		return date == null ? null : new java.text.SimpleDateFormat(DATE_FORMAT).format(date);
	}

	private String formatDate(Date date) {
		return date == null ? null : new java.text.SimpleDateFormat(DATE_FORMAT).format(date);
	}
	
	private boolean isDrugItem(RpmItem item, RpmLine line) {
		if (line != null && line.orderId != null) {
			return true;
		}
		return item != null && StringUtils.isNotBlank(item.type)
				&& (item.type.toLowerCase().contains("drug") || item.type.toLowerCase().contains("medic"));
	}
	
	private boolean isRamaInsuranceType(String insuranceType) {
		return StringUtils.isNotBlank(insuranceType) && RAMA_INSURANCE_TYPE.equalsIgnoreCase(insuranceType.trim());
	}

	private boolean isMmiInsuranceType(String insuranceType) {
		return StringUtils.isNotBlank(insuranceType) && MMI_INSURANCE_TYPE.equalsIgnoreCase(insuranceType.trim());
	}
	
	private BigDecimal defaultIfNull(BigDecimal value) {
		return value == null ? BigDecimal.ONE : value;
	}
	
	private String selectNullableColumn(String column) {
		return StringUtils.isBlank(column) ? "null" : q(column);
	}
	
	private String joinOr(List<String> conditions) {
		return "(" + StringUtils.join(conditions, " or ") + ")";
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
	
	private BigDecimal decimalValue(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof BigDecimal) {
			return (BigDecimal) value;
		}
		if (value instanceof Number) {
			return new BigDecimal(value.toString());
		}
		try {
			return new BigDecimal(value.toString());
		}
		catch (Exception ignored) {
			return null;
		}
	}
	
	private String stringValue(Object value) {
		return value == null ? null : StringUtils.trimToNull(value.toString());
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
		try {
			return new java.text.SimpleDateFormat(DATE_FORMAT).parse(text.length() > 10 ? text.substring(0, 10) : text);
		}
		catch (Exception ignored) {
			return null;
		}
	}
	
	private String gp(String key) {
		try {
			return StringUtils.trimToNull(Context.getAdministrationService().getGlobalProperty(key));
		}
		catch (Exception ignored) {
			return null;
		}
	}
	
	private String defaultIfBlank(String value, String defaultValue) {
		return StringUtils.isBlank(value) ? defaultValue : value;
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
	
	private String id(Object id) {
		return id == null ? "<missing>" : id.toString();
	}
	
	private static class RpmSchema {
		private String requestTable;
		private String lineTable;
		private String itemTable;
		private String requestIdColumn;
		private String requestPatientBillIdColumn;
		private String requestReferenceIdColumn;
		private String requestOrderIdColumn;
		private String lineIdColumn;
		private String lineRequestIdColumn;
		private String lineItemIdColumn;
		private String linePatientServiceBillIdColumn;
		private String lineFacilityServicePriceIdColumn;
		private String lineDispensingDateColumn;
		private String itemIdColumn;
		private String itemCodeColumn;
		private String itemTypeColumn;
		
		private boolean hasTables() {
			return StringUtils.isNotBlank(requestTable) && StringUtils.isNotBlank(lineTable)
					&& StringUtils.isNotBlank(itemTable);
		}
		
		private boolean isUsable() {
			return hasTables()
					&& StringUtils.isNotBlank(requestIdColumn)
					&& (StringUtils.isNotBlank(requestPatientBillIdColumn)
							|| StringUtils.isNotBlank(requestReferenceIdColumn))
					&& StringUtils.isNotBlank(lineIdColumn)
					&& StringUtils.isNotBlank(lineRequestIdColumn)
					&& StringUtils.isNotBlank(lineItemIdColumn)
					&& (StringUtils.isNotBlank(linePatientServiceBillIdColumn)
							|| StringUtils.isNotBlank(lineFacilityServicePriceIdColumn))
					&& StringUtils.isNotBlank(itemIdColumn)
					&& StringUtils.isNotBlank(itemCodeColumn);
		}
	}
	
	private static class RpmRequest {
		private Integer requestId;
		private Integer orderId;
	}
	
	private static class RpmLine {
		private Integer lineId;
		private Integer itemId;
		private Integer patientServiceBillId;
		private Integer facilityServicePriceId;
		private Date dispensingDate;
		private Integer orderId;
	}
	
	private static class RpmItem {
		private Integer itemId;
		private String name;
		private String code;
		private String type;
	}
}
