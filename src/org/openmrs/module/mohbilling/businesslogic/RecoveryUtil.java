package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Recovery;
import org.openmrs.module.mohbilling.service.BillingService;

public class RecoveryUtil {

	public Recovery createRecovery(Recovery recovery) {

		BillingService service = Context.getService(BillingService.class);

		if (recovery != null) {
			service.saveRecovery(recovery);
			return recovery;
		}

		return null;
	}

}
