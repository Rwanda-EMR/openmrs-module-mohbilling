/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.mohbilling.integration.irembopay;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.integration.irembopay.dto.IremboPayInitiateMomoPushPaymentDto;
import org.openmrs.module.mohbilling.integration.irembopay.dto.IremboPayInvoiceDto;
import org.openmrs.module.mohbilling.integration.irembopay.dto.IremboPayInvoicePaymentDataDto;
import org.openmrs.module.mohbilling.integration.irembopay.utils.IremboPayUtils;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.PaidServiceBill;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@Component
public class IremboPayIntegrationProvider {

    protected Log log = LogFactory.getLog(getClass());

    private final IremboPayIntegrationConfig iremboPayIntegrationConfig;

    private final IremboPayHttpProvider iremboPayHttpProvider;

    public IremboPayIntegrationProvider(@Autowired IremboPayIntegrationConfig iremboPayIntegrationConfig,
                                        @Autowired IremboPayHttpProvider iremboPayHttpProvider) {
        this.iremboPayIntegrationConfig = iremboPayIntegrationConfig;
        this.iremboPayHttpProvider = iremboPayHttpProvider;
    }

    public ResponseEntity<?> createIremboPayInvoice(Integer consommationId) {
        String iremboPayInvoiceUrl = this.iremboPayIntegrationConfig.getIremboPayEndpoint() + "/invoices";
        Consommation consommation = Context.getService(BillingService.class).getConsommation(consommationId);
        String transactionId = this.iremboPayIntegrationConfig.getHealthFacilityShortCode() != null ?
                this.iremboPayIntegrationConfig.getHealthFacilityShortCode() + "-" + consommationId : consommationId.toString();

        IremboPayInvoiceDto iremboPayInvoiceDto = IremboPayUtils.consommationToIremboPayInvoiceDto(consommation, transactionId,
                this.iremboPayIntegrationConfig.getPaymentAccountIdentifier(),
                this.iremboPayIntegrationConfig.getIremboPayProductCode());

        try {
            return this.iremboPayHttpProvider.postRequest(iremboPayInvoiceDto, iremboPayInvoiceUrl);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                try {
                    JsonNode errorBody = new ObjectMapper().readTree(ex.getResponseBodyAsString());
                    log.error("IremboPay API error response: " + errorBody.toString());

                    if (errorBody.has("errors") && errorBody.get("errors").isArray()) {
                        for (JsonNode errorNode : errorBody.get("errors")) {
                            if (errorNode.has("code") && "DUPLICATE_TRANSACTION_ID".equals(errorNode.get("code").asText())) {
                                log.warn("Duplicate transaction: Transaction Id already exists");
                                return this.iremboPayHttpProvider.getRequest(iremboPayInvoiceUrl + "/" + transactionId);
                            }
                        }
                    }
                } catch (Exception parseEx) {
                    throw new APIException("Error parsing error response", parseEx);
                }
            }
            throw new APIException("API error: " + ex.getMessage(), ex);
        } catch (RestClientException ex) {
            throw new APIException("API error: " + ex.getMessage(), ex);
        }
    }

    public ResponseEntity<?> initiateIremboPayMobileMoneyPushPayment(IremboPayInitiateMomoPushPaymentDto iremboPayInitiateMomoPushPaymentDto) {
        String iremboPayInitiatePushPaymentUrl = this.iremboPayIntegrationConfig.getIremboPayEndpoint()
                + "/transactions/initiate";

        return this.iremboPayHttpProvider.postRequest(iremboPayInitiateMomoPushPaymentDto,
                iremboPayInitiatePushPaymentUrl);
    }

    public void processPaymentNotification(IremboPayInvoicePaymentDataDto data) {
        BillPayment billPayment = new BillPayment();
        billPayment.setAmountPaid(BigDecimal.valueOf(data.getAmount()));

        String transactionId = data.getTransactionId();
        if (this.iremboPayIntegrationConfig.getHealthFacilityShortCode() != null && transactionId.contains("-")) {
            transactionId = transactionId.split("-")[1];
        }
        Consommation consommation = Context.getService(BillingService.class).getConsommation(Integer.parseInt(transactionId));
        billPayment.setPatientBill(consommation.getPatientBill());
        billPayment.setCreatedDate(new Date());
        billPayment.setCreator(Context.getAuthenticatedUser());
        billPayment.setCollector(Context.getAuthenticatedUser());
        billPayment.setDateReceived(data.getPaidAt());

        for (PatientServiceBill patientServiceBill : consommation.getBillItems()) {
            PaidServiceBill paidServiceBill = new PaidServiceBill();
            paidServiceBill.setBillItem(patientServiceBill);
            paidServiceBill.setPaidQty(patientServiceBill.getQuantity());
            paidServiceBill.setCreatedDate(new Date());
            paidServiceBill.setCreator(Context.getAuthenticatedUser());
            billPayment.addPaidItem(paidServiceBill);
        }

        Context.getService(BillingService.class).saveBillPayment(billPayment);

        PatientBill patientBill = billPayment.getPatientBill();
        BigDecimal patientBillAmount = patientBill.getAmount();

        if (patientBillAmount.compareTo(patientBill.getAmountPaid()) == 0 ||
                patientBillAmount.compareTo(billPayment.getAmountPaid()) == 0) {
            patientBill.setIsPaid(true);
            Context.getService(BillingService.class).savePatientBill(patientBill);
        }
    }
}
