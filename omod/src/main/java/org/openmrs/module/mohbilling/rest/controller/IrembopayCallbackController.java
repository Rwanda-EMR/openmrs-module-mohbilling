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
            writeError(response, IrembopayCallbackErrorResponse.build("INVALID_METHOD", "POST required"));
            return null;
        }

        String body;
        try {
            body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Irembopay callback: failed to read body", e);
            writeError(response, IrembopayCallbackErrorResponse.build("INVALID_BODY", "Invalid body"));
            return null;
        }

        if (body == null || body.trim().isEmpty()) {
            writeError(response, IrembopayCallbackErrorResponse.build("EMPTY_BODY", "Empty body"));
            return null;
        }

        log.info("Irembopay callback: controller handleRequest reached (POST body length=" + body.length() + ")");

        try {
            ObjectMapper mapper = new ObjectMapper();
            IrembopayCallbackRequest delegate = mapper.readValue(body, IrembopayCallbackRequest.class);

            if (delegate.getData() == null || delegate.getData().getInvoiceNumber() == null) {
                writeError(response, IrembopayCallbackErrorResponse.build(
                    "MISSING_REQUIRED_FIELDS", "Missing data or invoice number"));
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
        } catch (Exception e) {
            log.error("Irembopay callback: processing failed", e);
            writeError(response, IrembopayCallbackErrorResponse.fromThrowable(e));
        }
        return null;
    }

    private void writeError(HttpServletResponse response, SimpleObject errorResponse) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        writeJson(response, errorResponse);
    }

    private void writeJson(HttpServletResponse response, SimpleObject data) throws IOException {
        String json = new ObjectMapper().writeValueAsString(data);
        response.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
        response.getOutputStream().flush();
    }
}
