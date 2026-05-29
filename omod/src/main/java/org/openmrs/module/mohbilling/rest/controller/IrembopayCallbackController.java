package org.openmrs.module.mohbilling.rest.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mohbilling.rest.resource.IrembopayCallbackRequest;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles POST /openmrs/ws/rest/v1/mohbilling/irembopay/callback directly,
 * so the callback works even when the REST framework routes the same URL to update().
 * Registered with higher priority (order 1) so this controller runs before the REST dispatcher.
 */
public class IrembopayCallbackController implements Controller {

    private static final Log log = LogFactory.getLog(IrembopayCallbackController.class);

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            SimpleObject err = new SimpleObject();
            err.put("success", false);
            SimpleObject error = new SimpleObject();
            error.put("code", "INVALID_METHOD");
            error.put("message", "POST required");
            err.put("error", error);
            writeJson(response, err);
            return null;
        }

        String body;
        try {
            body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Irembopay callback: failed to read body", e);
            response.setStatus(HttpServletResponse.SC_OK);
            SimpleObject err = new SimpleObject();
            err.put("success", false);
            SimpleObject error = new SimpleObject();
            error.put("code", "INVALID_BODY");
            error.put("message", "Invalid body");
            err.put("error", error);
            writeJson(response, err);
            return null;
        }

        if (body == null || body.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_OK);
            SimpleObject err = new SimpleObject();
            err.put("success", false);
            SimpleObject error = new SimpleObject();
            error.put("code", "EMPTY_BODY");
            error.put("message", "Empty body");
            err.put("error", error);
            writeJson(response, err);
            return null;
        }

        log.info("Irembopay callback: controller handleRequest reached (POST body length=" + (body != null ? body.length() : 0) + ")");

        try {
            ObjectMapper mapper = new ObjectMapper();
            IrembopayCallbackRequest delegate = mapper.readValue(body, IrembopayCallbackRequest.class);

            if (delegate.getData() == null || delegate.getData().getInvoiceNumber() == null) {
                response.setStatus(HttpServletResponse.SC_OK);
                SimpleObject err = new SimpleObject();
                err.put("success", false);
                SimpleObject error = new SimpleObject();
                error.put("code", "MISSING_REQUIRED_FIELDS");
                error.put("message", "Missing data or invoice number");
                err.put("error", error);
                writeJson(response, err);
                return null;
            }

            IrembopayCallbackProcessor.process(delegate);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            SimpleObject ok = new SimpleObject();
            ok.put("success", true);
            ok.put("data", delegate);
            writeJson(response, ok);
        } catch (IllegalArgumentException e) {
            log.error("Irembopay callback: validation error", e);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            SimpleObject err = new SimpleObject();
            err.put("success", false);
            SimpleObject error = new SimpleObject();
            error.put("code", "VALIDATION_ERROR");
            error.put("message", e.getMessage());
            err.put("error", error);
            writeJson(response, err);
        } catch (Exception e) {
            log.error("Irembopay callback: error processing request", e);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            SimpleObject err = new SimpleObject();
            err.put("success", false);
            SimpleObject error = new SimpleObject();
            error.put("code", "PROCESSING_ERROR");
            error.put("message", e.getMessage());
            err.put("error", error);
            writeJson(response, err);
        }
        return null;
    }

    private void writeJson(HttpServletResponse response, SimpleObject data) throws IOException {
        String json = new ObjectMapper().writeValueAsString(data);
        response.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
        response.getOutputStream().flush();
    }
}
