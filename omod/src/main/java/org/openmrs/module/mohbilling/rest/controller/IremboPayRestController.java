/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.mohbilling.rest.controller;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.module.mohbilling.integration.irembopay.IremboPayIntegrationProvider;
import org.openmrs.module.mohbilling.integration.irembopay.dto.IremboPayCreateInvoiceResponse;
import org.openmrs.module.mohbilling.integration.irembopay.dto.IremboPayCreateInvoiceResponseDataDto;
import org.openmrs.module.mohbilling.integration.irembopay.dto.IremboPayInitiateMomoPushPaymentDto;
import org.openmrs.module.mohbilling.integration.irembopay.dto.IremboPayInvoicePaymentDataDto;
import org.openmrs.module.mohbilling.integration.irembopay.dto.IremboPayInvoicePaymentResponseDto;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/mohbilling/irembopay")
public class IremboPayRestController extends MainResourceController {

    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
    IremboPayIntegrationProvider iremboPayIntegrationProvider;

    /**
     * @see org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController#getNamespace()
     */
    @Override
    public String getNamespace() {
        return "v1/mohbilling/irembopay";
    }

    /*
    @RequestMapping(value = "/invoices", method = RequestMethod.POST)
    public ResponseEntity<?> createIremboPayInvoice(@RequestBody Map<String, String> requestBody) {
        return iremboPayIntegrationProvider.createIremboPayInvoice(consommationId);
    }*/

    @RequestMapping(value = "/transactions/initiate", method = RequestMethod.POST)
    public ResponseEntity<?> initiateIremboPayMobileMoneyPushPayment(@RequestBody Map<String, String> requestBody) {
        Integer consommationId = Integer.valueOf(requestBody.get("consommationId"));
        String accountIdentifier = requestBody.get("accountIdentifier");
        ResponseEntity<?> responseEntity = this.iremboPayIntegrationProvider.createIremboPayInvoice(consommationId);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            log.info("responseEntity: " + responseEntity);
            IremboPayCreateInvoiceResponse iremboPayCreateInvoiceResponse =
                    new ObjectMapper().convertValue(responseEntity.getBody(), IremboPayCreateInvoiceResponse.class);
            if (iremboPayCreateInvoiceResponse != null && iremboPayCreateInvoiceResponse.isSuccess()) {
                log.info("iremboPayCreateInvoiceResponse.isSuccess(): " + iremboPayCreateInvoiceResponse.isSuccess());

                IremboPayCreateInvoiceResponseDataDto dataDto = iremboPayCreateInvoiceResponse.getData();
                IremboPayInitiateMomoPushPaymentDto paymentDto = new IremboPayInitiateMomoPushPaymentDto(
                        accountIdentifier, requestBody.get("paymentProvider"), dataDto.getInvoiceNumber());

                return iremboPayIntegrationProvider.initiateIremboPayMobileMoneyPushPayment(paymentDto);
            } else {
                if (iremboPayCreateInvoiceResponse != null) {
                    log.warn("invoice not created successfully —> " + iremboPayCreateInvoiceResponse.getMessage());
                    throw new APIException(iremboPayCreateInvoiceResponse.getMessage());
                } else {
                    throw new APIException("IremboPayCreateInvoiceResponse is null");
                }
            }
        } else {
            throw new APIException(responseEntity.getStatusCode().getReasonPhrase());
        }
    }

    @RequestMapping(value = "/payment/notification", method = RequestMethod.POST)
    public void receivePaymentNotification(@RequestHeader HttpHeaders headers,
                                           @RequestBody Map<String, Object> requestBody) {
        log.info("IremboPay Payment Notification Received: " + requestBody);
        IremboPayInvoicePaymentResponseDto iremboPayInvoicePaymentResponseDto =
                new ObjectMapper().convertValue(requestBody, IremboPayInvoicePaymentResponseDto.class);

        if (iremboPayInvoicePaymentResponseDto.isSuccess()) {
            IremboPayInvoicePaymentDataDto data = iremboPayInvoicePaymentResponseDto.getData();
            if (data != null && data.getPaymentStatus().equals("PAID")) {
                log.info("IremboPay Payment Notification Received: " + data);
                iremboPayIntegrationProvider.processPaymentNotification(data);
            } else {
                log.warn("IremboPay Payment Notification Received —> data: " + data);
            }
        }
    }
}
