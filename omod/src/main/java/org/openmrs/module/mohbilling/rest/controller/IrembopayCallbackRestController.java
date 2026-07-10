package org.openmrs.module.mohbilling.rest.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.rest.resource.IrembopayCallbackRequest;
import org.openmrs.module.mohbilling.rest.resource.IrembopayCallbackResource;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Handles POST /openmrs/ws/rest/v1/mohbilling/irembopay/callback with explicit HTTP status codes.
 * This avoids the generic REST update handler, which always returns 200 via RestUtil.updated().
 */
@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/mohbilling/irembopay")
public class IrembopayCallbackRestController {

    private static final Log log = LogFactory.getLog(IrembopayCallbackRestController.class);

    @RequestMapping(value = "/callback", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<SimpleObject> handleCallback(@RequestBody SimpleObject body) {
        log.info("Irembopay callback: dedicated REST controller received request");
        try {
            IrembopayCallbackResource callbackResource = (IrembopayCallbackResource) Context.getService(RestService.class)
                .getResourceByName(RestConstants.VERSION_1 + "/mohbilling/irembopay/callback");
            IrembopayCallbackRequest delegate = callbackResource.convert(body);
            IrembopayCallbackProcessor.process(delegate);
            delegate.setSuccess(true);

            SimpleObject ok = new SimpleObject();
            ok.put("success", true);
            ok.put("data", delegate);
            return ResponseEntity.ok(ok);
        } catch (Exception e) {
            log.error("Irembopay callback: processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(IrembopayCallbackErrorResponse.fromThrowable(e));
        }
    }
}
