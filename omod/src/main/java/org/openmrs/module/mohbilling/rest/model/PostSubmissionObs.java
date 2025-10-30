package org.openmrs.module.mohbilling.rest.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PostSubmissionObs {
    private List<ObsIdentifier> obs = new ArrayList<>();
    private String insurancePolicyNumber;

    public static class ObsIdentifier {
        private String uuid;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }
    }

    public List<ObsIdentifier> getObs() {
        return obs;
    }

    public void setObs(List<ObsIdentifier> obs) {
        this.obs = obs;
    }

    public String getInsurancePolicyNumber() {
        return insurancePolicyNumber;
    }

    public void setInsurancePolicyNumber(String insurancePolicyNumber) {
        this.insurancePolicyNumber = insurancePolicyNumber;
    }
}
