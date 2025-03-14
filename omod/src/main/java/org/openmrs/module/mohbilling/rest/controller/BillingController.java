package org.openmrs.module.mohbilling.rest.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/mohbilling")
public class BillingController extends MainResourceController {

    /**
     * @see org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController#getNamespace()
     */
    @Override
    public String getNamespace() {
        return "v1/mohbilling";
    }

    @RequestMapping(value = "/globalBill/summary", method = RequestMethod.GET)
    public ResponseEntity<Map<String, BigDecimal>> getGlobalBillSummary() {
        BillingService billingService = Context.getService(BillingService.class);
        Map<String, BigDecimal> summary = billingService.getGlobalBillsSummary();
        return ResponseEntity.ok(summary);
    }
}
