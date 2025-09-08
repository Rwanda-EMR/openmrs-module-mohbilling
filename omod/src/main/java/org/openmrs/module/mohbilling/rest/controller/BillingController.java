package org.openmrs.module.mohbilling.rest.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.rest.dto.RevertGlobalBillResponse;
import org.openmrs.module.mohbilling.service.BillingService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * Reverts a closed global bill back to unpaid status
     * POST /rest/v1/mohbilling/globalBill/{id}/revert
     * 
     * @param id the global bill ID to revert
     * @param requestBody containing the reason for reverting
     * @return ResponseEntity with the revert response or error message
     */
    @RequestMapping(value = "/globalBill/{id}/revert", method = RequestMethod.POST)
    public ResponseEntity<?> revertGlobalBill(@PathVariable("id") String id, 
                                            @RequestBody Map<String, String> requestBody) {
        try {
            Integer globalBillId = Integer.valueOf(id);
            String reason = requestBody.get("reason");
            
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("Reason for reverting bill is required");
            }
            
            BillingService billingService = Context.getService(BillingService.class);
            billingService.revertClosedBill(globalBillId, reason, Context.getAuthenticatedUser());

            RevertGlobalBillResponse response = new RevertGlobalBillResponse(
                globalBillId, 
                reason, 
                Context.getAuthenticatedUser().getUsername()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                .body("Invalid global bill ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error reverting bill: " + e.getMessage());
        }
    }
}
