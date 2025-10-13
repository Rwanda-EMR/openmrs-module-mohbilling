/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.mohbilling.integration.irembopay.dto;

public class IremboPayInvoicePaymentResponseDto {
    private boolean success;
    private IremboPayInvoicePaymentDataDto data;

    public IremboPayInvoicePaymentResponseDto() {
        // no-arg constructor for Jackson
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public IremboPayInvoicePaymentDataDto getData() {
        return data;
    }

    public void setData(IremboPayInvoicePaymentDataDto data) {
        this.data = data;
    }
}
