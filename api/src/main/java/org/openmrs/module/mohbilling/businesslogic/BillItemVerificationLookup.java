package org.openmrs.module.mohbilling.businesslogic;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.BillReportItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The verification status of bill items, and the reason given when an item was rejected or its rejection disputed.
 * Bill items are verified on the RHIP voucher submission page of the rwandaemr module, which saves the latest decision
 * on each item in {@code rwandaemr_bill_item_verification}.  The status is read live rather than from the ETL table,
 * so the report shows decisions made since the last ETL refresh.  Without the rwandaemr module the columns stay empty.
 */
public final class BillItemVerificationLookup {

	private static final Log log = LogFactory.getLog(BillItemVerificationLookup.class);

	static final String NOT_VERIFIED = "Pending";

	private static final int BATCH_SIZE = 1000;

	/**
	 * The labels of the statuses saved by rwandaemr ({@code BillItemVerificationStatus})
	 */
	private static final Map<String, String> STATUS_LABELS = new HashMap<String, String>();

	static {
		STATUS_LABELS.put("PENDING", "Pending");
		STATUS_LABELS.put("FACILITY_ACCEPTED", "Accepted by facility");
		STATUS_LABELS.put("FACILITY_REJECTED", "Rejected by facility");
		STATUS_LABELS.put("INSURER_ACCEPTED", "Accepted by insurer");
		STATUS_LABELS.put("INSURER_REJECTED", "Rejected by insurer");
		STATUS_LABELS.put("REJECTION_ACCEPTED", "Rejection accepted by facility");
		STATUS_LABELS.put("DISPUTED", "Disputed by facility");
		STATUS_LABELS.put("FINAL_ACCEPTED", "Accepted after dispute");
		STATUS_LABELS.put("FINAL_REJECTED", "Rejected after dispute");
	}

	private BillItemVerificationLookup() {
	}

	/**
	 * Sets the verification status and reason of each row
	 */
	public static void apply(List<BillReportItem> rows) {
		if (rows == null || rows.isEmpty()) {
			return;
		}
		List<Integer> itemIds = new ArrayList<Integer>();
		for (BillReportItem row : rows) {
			if (row.getPatientServiceBillId() != null) {
				itemIds.add(row.getPatientServiceBillId());
			}
		}
		Map<Integer, String[]> verifications = getVerifications(itemIds);
		for (BillReportItem row : rows) {
			String[] verification = verifications.get(row.getPatientServiceBillId());
			row.setVerificationStatus(verification == null ? NOT_VERIFIED : label(verification[0]));
			row.setVerificationReason(verification == null ? "" : StringUtils.defaultString(verification[1]));
		}
	}

	/**
	 * @return the status and reason of each of the given items that was verified, by patient service bill id
	 */
	static Map<Integer, String[]> getVerifications(List<Integer> itemIds) {
		if (itemIds == null || itemIds.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<Integer, String[]> ret = new HashMap<Integer, String[]>();
		try {
			for (int start = 0; start < itemIds.size(); start += BATCH_SIZE) {
				List<Integer> batch = itemIds.subList(start, Math.min(start + BATCH_SIZE, itemIds.size()));
				String sql = "select v.patient_service_bill_id, v.status, r.name from rwandaemr_bill_item_verification v "
						+ "left join rwandaemr_bill_verification_reason r on r.reason_id = v.reason_id "
						+ "where v.patient_service_bill_id in (" + StringUtils.join(batch, ",") + ")";
				List<List<Object>> rows = Context.getAdministrationService().executeSQL(sql, true);
				for (List<Object> row : rows) {
					if (row.get(0) instanceof Number) {
						ret.put(((Number) row.get(0)).intValue(), new String[] {
								row.get(1) == null ? null : row.get(1).toString(),
								row.get(2) == null ? null : row.get(2).toString() });
					}
				}
			}
		}
		catch (Exception e) {
			log.debug("Bill item verifications not available; is the rwandaemr module running?", e);
			return Collections.emptyMap();
		}
		return ret;
	}

	static String label(String status) {
		if (status == null) {
			return NOT_VERIFIED;
		}
		String label = STATUS_LABELS.get(status);
		return label == null ? status : label;
	}
}
