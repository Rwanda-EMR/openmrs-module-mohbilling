package org.openmrs.module.mohbilling.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.integration.insurance.RhipApprovalRequiredProducts;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 * Refreshes the procedures each insurer must approve from RHIP (moh_bill_approval_required_product)
 */
public class RefreshApprovalRequiredProductsTask extends AbstractTask {

	private static final Log log = LogFactory.getLog(RefreshApprovalRequiredProductsTask.class);

	@Override
	public void execute() {
		try {
			RhipApprovalRequiredProducts products = Context.getRegisteredComponent("rhipApprovalRequiredProducts",
					RhipApprovalRequiredProducts.class);
			for (String result : products.refreshAll()) {
				log.info("Approval-required procedures: " + result);
			}
		}
		catch (Exception e) {
			log.error("Unable to refresh the approval-required procedures", e);
		}
	}
}
