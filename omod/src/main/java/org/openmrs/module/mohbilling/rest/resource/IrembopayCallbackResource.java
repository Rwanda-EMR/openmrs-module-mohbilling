package org.openmrs.module.mohbilling.rest.resource;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mohbilling.rest.controller.IrembopayCallbackProcessor;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST resource for Irembo Pay callback at
 * POST /openmrs/ws/rest/v1/mohbilling/irembopay/callback
 * <p>
 * Accepts a JSON body with success, data (amount, invoiceNumber, transactionId,
 * customer, paymentItems, etc.). Handling logic to be added later.
 * <p>
 * Overrides update() because the REST framework may route POST to update (with
 * "callback" or null as uuid); we treat it as create and call save(delegate).
 */
@Resource(
    name = RestConstants.VERSION_1 + "/mohbilling/irembopay/callback",
    supportedClass = IrembopayCallbackRequest.class,
    supportedOpenmrsVersions = { "2.0 - 2.*" }
)
public class IrembopayCallbackResource extends DelegatingCrudResource<IrembopayCallbackRequest> {

    private static final Log log = LogFactory.getLog(IrembopayCallbackResource.class);

    @Override
    public IrembopayCallbackRequest getByUniqueId(String uniqueId) {
        return null;
    }

    @Override
    public IrembopayCallbackRequest newDelegate() {
        return new IrembopayCallbackRequest();
    }

    @Override
    public IrembopayCallbackRequest save(IrembopayCallbackRequest delegate) throws ResponseException {
        log.info("Irembopay callback: REST resource save() called");
        try {
            IrembopayCallbackProcessor.process(delegate);
            // Success - ensure success flag is set
            delegate.setSuccess(true);
        } catch (IllegalArgumentException e) {
            log.error("Irembopay callback: validation failed");
            log.error(e.getMessage());
            // Return HTTP 200 with success=false and error message
            delegate.setSuccess(false);
            delegate.setError(e.getMessage());
        }
        return delegate;
    }

    /**
     * Override so POST to .../callback is handled as create when the framework
     * routes it to update (getByUniqueId returns null). Convert body and save.
     */
    @Override
    public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
        IrembopayCallbackRequest delegate = convert(propertiesToUpdate);
        delegate = save(delegate);
        return ConversionUtil.convertToRepresentation(delegate, context.getRepresentation());
    }

    @Override
    protected void delete(IrembopayCallbackRequest delegate, String reason, RequestContext context) throws ResponseException {
        throw new UnsupportedOperationException("Callback resource does not support DELETE");
    }

    @Override
    public void purge(IrembopayCallbackRequest delegate, RequestContext context) throws ResponseException {
        throw new UnsupportedOperationException("Callback resource does not support PURGE");
    }

    @Override
    protected String getUniqueId(IrembopayCallbackRequest delegate) {
        return delegate.getData() != null && delegate.getData().getTransactionId() != null
            ? delegate.getData().getTransactionId()
            : null;
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription d = new DelegatingResourceDescription();
        d.addProperty("success");
        d.addProperty("data");
        d.addProperty("error");
        return d;
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() {
        DelegatingResourceDescription d = new DelegatingResourceDescription();
        d.addProperty("success");
        d.addProperty("data");
        d.addProperty("error");
        return d;
    }

    /**
     * Custom setter so the REST framework can set "data" from a nested Map (JSON object).
     * ConversionUtil cannot convert Map to IrembopayCallbackData, so we do it with Jackson.
     */
    @PropertySetter("data")
    public void setDataProperty(IrembopayCallbackRequest delegate, Object value) {
        if (value == null) {
            delegate.setData(null);
            return;
        }
        if (value instanceof IrembopayCallbackRequest.IrembopayCallbackData) {
            delegate.setData((IrembopayCallbackRequest.IrembopayCallbackData) value);
            return;
        }
        if (value instanceof Map) {
            try {
                @SuppressWarnings("unchecked")
                IrembopayCallbackRequest.IrembopayCallbackData data = new ObjectMapper().convertValue(
                    value, IrembopayCallbackRequest.IrembopayCallbackData.class);
                delegate.setData(data);
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not convert data to IrembopayCallbackData", e);
            }
            return;
        }
        throw new IllegalArgumentException("data must be Map or IrembopayCallbackData, got " + (value != null ? value.getClass() : "null"));
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        return new NeedsPaging<>(Collections.emptyList(), context);
    }
}
