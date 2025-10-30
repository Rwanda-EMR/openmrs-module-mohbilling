/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.mohbilling.integration.irembopay.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.module.mohbilling.integration.irembopay.dto.CustomerDto;
import org.openmrs.module.mohbilling.integration.irembopay.dto.IremboPayInvoiceDto;
import org.openmrs.module.mohbilling.integration.irembopay.dto.PaymentItemDto;
import org.openmrs.module.mohbilling.model.Consommation;

public class IremboPayUtils {

    protected static Log log = LogFactory.getLog(IremboPayUtils.class);

    public static IremboPayInvoiceDto consommationToIremboPayInvoiceDto(Consommation consommation, String transactionId,
                                                                        String paymentAccountIdentifier, String productCode) {
        IremboPayInvoiceDto iremboPayInvoiceDto = new IremboPayInvoiceDto();
        iremboPayInvoiceDto.setTransactionId(transactionId);

        List<PaymentItemDto> paymentItems = new ArrayList<>();
        paymentItems.add(new PaymentItemDto(consommation.getPatientBill().getAmount().intValue(), 1, productCode));
        iremboPayInvoiceDto.setPaymentItems(paymentItems);
        iremboPayInvoiceDto.setPaymentAccountIdentifier(paymentAccountIdentifier);

        Patient patient = consommation.getGlobalBill().getAdmission().getInsurancePolicy().getOwner();
        CustomerDto customerDto = new CustomerDto("", "", patient.getPersonName().getFullName());
        iremboPayInvoiceDto.setCustomer(customerDto);

        log.info("consommationToIremboPayInvoiceDto: " + iremboPayInvoiceDto);
        return iremboPayInvoiceDto;
    }
}
