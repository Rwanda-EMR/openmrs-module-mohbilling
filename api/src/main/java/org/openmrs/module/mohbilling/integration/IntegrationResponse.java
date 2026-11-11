package org.openmrs.module.mohbilling.integration;

/**
 * Represents a generic response to hitting an integration endpoint.
 */
public class IntegrationResponse {
	private boolean enabled;
	private boolean endpointAccessible;
	private Integer responseCode;
	private Object responseEntity;
	private String errorMessage;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEndpointAccessible() {
		return endpointAccessible;
	}

	public void setEndpointAccessible(boolean endpointAccessible) {
		this.endpointAccessible = endpointAccessible;
	}

	public Integer getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(Integer responseCode) {
		this.responseCode = responseCode;
	}

	public Object getResponseEntity() {
		return responseEntity;
	}

	public void setResponseEntity(Object responseEntity) {
		this.responseEntity = responseEntity;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
