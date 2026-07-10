package org.openmrs.module.mohbilling.rest.resource;

import java.util.Collections;
import java.util.Date;

import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * REST resource for Irembo Pay under /openmrs/ws/rest/v1/mohbilling/irembopay.
 * GET .../irembopay/status returns the status payload (status is the uniqueId).
 */
@Resource(
    name = RestConstants.VERSION_1 + "/mohbilling/irembopay",
    supportedClass = IrembopayStatusResource.StatusPayload.class,
    supportedOpenmrsVersions = { "2.0 - 2.*" }
)
public class IrembopayStatusResource extends DelegatingCrudResource<IrembopayStatusResource.StatusPayload> {

    /**
     * Simple payload for the status response.
     */
    public static class StatusPayload {
        private String status = "ok";
        private String module = "mohbilling";
        private String endpoint = "irembopay";
        private String timestamp;

        public StatusPayload() {
            this.timestamp = new Date().toString();
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getModule() {
            return module;
        }

        public void setModule(String module) {
            this.module = module;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }

    @Override
    public StatusPayload getByUniqueId(String uniqueId) {
        // GET .../irembopay/status is dispatched with uniqueId = "status"
        if ("status".equals(uniqueId)) {
            return new StatusPayload();
        }
        return null;
    }

    @Override
    public StatusPayload newDelegate() {
        return new StatusPayload();
    }

    @Override
    public StatusPayload save(StatusPayload delegate) {
        throw new UnsupportedOperationException("Status is read-only");
    }

    /**
     * POST .../irembopay/callback is routed to this resource with uuid "callback".
     * Delegate to IrembopayCallbackResource so the callback body is handled there.
     */
    @Override
    public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
        if ("callback".equals(uuid)) {
            IrembopayCallbackResource callbackResource = (IrembopayCallbackResource) Context.getService(RestService.class)
                .getResourceByName(RestConstants.VERSION_1 + "/mohbilling/irembopay/callback");
            IrembopayCallbackRequest delegate = callbackResource.convert(propertiesToUpdate);
            delegate = callbackResource.save(delegate);
            return ConversionUtil.convertToRepresentation(delegate, context.getRepresentation());
        }
        return super.update(uuid, propertiesToUpdate, context);
    }

    @Override
    protected void delete(StatusPayload delegate, String reason, RequestContext context) throws ResponseException {
        throw new UnsupportedOperationException("Status is read-only");
    }

    @Override
    public void purge(StatusPayload delegate, RequestContext context) throws ResponseException {
        throw new UnsupportedOperationException("Status is read-only");
    }

    @Override
    protected String getUniqueId(StatusPayload delegate) {
        return "status";
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("status");
        description.addProperty("module");
        description.addProperty("endpoint");
        description.addProperty("timestamp");
        if (rep instanceof DefaultRepresentation) {
            description.addSelfLink();
        }
        return description;
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        return new AlreadyPaged<>(context, Collections.singletonList(new StatusPayload()), false);
    }
}
