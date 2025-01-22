package org.openmrs.module.mohbilling.rest.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PostSubmissionObs {
    private List<ObsIdentifier> obs = new ArrayList<>();

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
}
