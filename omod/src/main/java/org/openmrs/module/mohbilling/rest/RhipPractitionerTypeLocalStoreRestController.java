package org.openmrs.module.mohbilling.rest;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.RhipPractitionerType;
import org.openmrs.module.mohbilling.service.RhipPractitionerTypeService;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class RhipPractitionerTypeLocalStoreRestController {

	private final RhipPractitionerTypeService rhipPractitionerTypeService;

	public RhipPractitionerTypeLocalStoreRestController(@Autowired RhipPractitionerTypeService rhipPractitionerTypeService) {
		this.rhipPractitionerTypeService = rhipPractitionerTypeService;
	}

	@RequestMapping(value = "/rest/v1/mohbilling/rhip/practitioner/types/local", method = RequestMethod.GET)
	@ResponseBody
	public Object list(@RequestParam(value = "type", required = false) String type) throws ResponseException {
		String effectiveType = StringUtils.isBlank(type) ? "SUB_CATEGORY" : type.trim();
		return rhipPractitionerTypeService.getByType(effectiveType);
	}

	/**
	 * Upserts a local practitioner type record (manual seed when RHIP /practitioner/types is unreachable).
	 *
	 * Example body:
	 * { "rhipId": "<uuid>", "name": "NURSE A1", "type": "SUB_CATEGORY", "categoryRhipId": "<uuid>", "categoryName": "NURSE" }
	 */
	@RequestMapping(value = "/rest/v1/mohbilling/rhip/practitioner/types/local", method = RequestMethod.POST)
	@ResponseBody
	public Object upsert(@RequestBody Map<String, Object> body) throws ResponseException {
		String rhipId = value(body, "rhipId");
		String name = value(body, "name");
		String type = value(body, "type");
		String categoryRhipId = value(body, "categoryRhipId");
		String categoryName = value(body, "categoryName");

		if (StringUtils.isBlank(rhipId) || StringUtils.isBlank(name) || StringUtils.isBlank(type)) {
			throw new IllegalArgumentException("rhipId, name and type are required");
		}

		RhipPractitionerType existing = rhipPractitionerTypeService.getByRhipId(rhipId.trim());
		RhipPractitionerType record = existing == null ? new RhipPractitionerType() : existing;

		record.setRhipId(rhipId.trim());
		record.setName(name.trim());
		record.setType(type.trim());
		record.setCategoryRhipId(StringUtils.isBlank(categoryRhipId) ? null : categoryRhipId.trim());
		record.setCategoryName(StringUtils.isBlank(categoryName) ? null : categoryName.trim());
		record.setVoided(Boolean.FALSE);

		if (existing == null) {
			record.setCreator(Context.getAuthenticatedUser());
			record.setDateCreated(new Date());
		} else {
			record.setChangedBy(Context.getAuthenticatedUser());
			record.setDateChanged(new Date());
		}

		return rhipPractitionerTypeService.saveOrUpdate(record);
	}

	private String value(Map<String, Object> body, String key) {
		if (body == null || key == null) {
			return null;
		}
		Object raw = body.get(key);
		if (raw == null) {
			return null;
		}
		String text = raw.toString();
		return StringUtils.isBlank(text) ? null : text.trim();
	}
}

