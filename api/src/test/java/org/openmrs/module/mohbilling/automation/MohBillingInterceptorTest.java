package org.openmrs.module.mohbilling.automation;

import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class MohBillingInterceptorTest extends BaseModuleContextSensitiveTest {

    @Autowired
    MohBillingInterceptor mohBillingInterceptor;

    @Test
    public void shouldLoadInterceptor() {
        assertThat(mohBillingInterceptor, notNullValue());
    }
}
