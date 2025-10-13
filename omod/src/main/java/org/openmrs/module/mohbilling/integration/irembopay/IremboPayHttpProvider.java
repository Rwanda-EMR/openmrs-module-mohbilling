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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class IremboPayHttpProvider {

    protected final Log log = LogFactory.getLog(getClass());

    private final IremboPayIntegrationConfig iremboPayIntegrationConfig;

    public IremboPayHttpProvider(@Autowired IremboPayIntegrationConfig iremboPayIntegrationConfig) {
        this.iremboPayIntegrationConfig = iremboPayIntegrationConfig;
    }

    public ResponseEntity<?> postRequest(Object payload, String url) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("irembopay-secretKey", this.iremboPayIntegrationConfig.getIremboPaySecretKey());
        headers.add("X-API-Version", this.iremboPayIntegrationConfig.getIremboPayApiVersion());

        HttpEntity<Object> request = new HttpEntity<>(payload, headers);

        return restTemplate.exchange(url, HttpMethod.POST, request, Object.class);
    }

    public ResponseEntity<?> getRequest(String url) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("irembopay-secretKey", this.iremboPayIntegrationConfig.getIremboPaySecretKey());
        headers.add("X-API-Version", this.iremboPayIntegrationConfig.getIremboPayApiVersion());
        HttpEntity<String> request = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, request, Object.class);
    }
}
