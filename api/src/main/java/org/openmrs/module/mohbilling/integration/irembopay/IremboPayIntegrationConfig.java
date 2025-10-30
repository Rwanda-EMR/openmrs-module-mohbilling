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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.ConfigUtil;
import org.springframework.stereotype.Component;

@Component
public class IremboPayIntegrationConfig {

    protected Log log = LogFactory.getLog(getClass());

    private static final String IREMBOPAY_INTEGRATION_PREFIX = "billing.iremboPay.";
    private static final String IREMBOPAY_INTEGRATION_BASEURL = IREMBOPAY_INTEGRATION_PREFIX + "baseUrl";
    private static final String IREMBOPAY_INTEGRATION_SECREKEY = IREMBOPAY_INTEGRATION_PREFIX + "secretKey";
    private static final String IREMBOPAY_INTEGRATION_APIVERSION = IREMBOPAY_INTEGRATION_PREFIX + "apiVersion";
    private static final String IREMBOPAY_INTEGRATION_PRODUCTCODE = IREMBOPAY_INTEGRATION_PREFIX + "productCode";
    private static final String IREMBOPAY_INTEGRATION_PAYMENTACCOUNTIDENTIFIER = IREMBOPAY_INTEGRATION_PREFIX + "paymentAccountIdentifier";
    private static final String HEALTH_FACILITY_SHORTCODE = "billing.healthFacilityShortCode";

    public IremboPayIntegrationConfig() {
    }

    public String getIremboPayEndpoint() {
        return ConfigUtil.getProperty(IREMBOPAY_INTEGRATION_BASEURL);
    }

    public String getIremboPaySecretKey() {
        return ConfigUtil.getProperty(IREMBOPAY_INTEGRATION_SECREKEY);
    }

    public String getIremboPayApiVersion() {
        return ConfigUtil.getProperty(IREMBOPAY_INTEGRATION_APIVERSION);
    }

    public String getIremboPayProductCode() {
        return ConfigUtil.getProperty(IREMBOPAY_INTEGRATION_PRODUCTCODE);
    }

    public String getPaymentAccountIdentifier() {
        return ConfigUtil.getProperty(IREMBOPAY_INTEGRATION_PAYMENTACCOUNTIDENTIFIER);
    }

    public String getHealthFacilityShortCode() {
        return ConfigUtil.getProperty(HEALTH_FACILITY_SHORTCODE);
    }
}
