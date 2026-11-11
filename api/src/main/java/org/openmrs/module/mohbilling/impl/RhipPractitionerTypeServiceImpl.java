package org.openmrs.module.mohbilling.impl;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.db.RhipPractitionerTypeDAO;
import org.openmrs.module.mohbilling.model.RhipPractitionerType;
import org.openmrs.module.mohbilling.service.RhipPractitionerTypeService;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RhipPractitionerTypeServiceImpl implements RhipPractitionerTypeService {

	private RhipPractitionerTypeDAO rhipPractitionerTypeDAO;

	public void setRhipPractitionerTypeDAO(RhipPractitionerTypeDAO rhipPractitionerTypeDAO) {
		this.rhipPractitionerTypeDAO = rhipPractitionerTypeDAO;
	}

	@Override
	public RhipPractitionerType saveOrUpdate(RhipPractitionerType practitionerType) throws APIException {
		if (practitionerType == null) {
			return null;
		}
		if (practitionerType.getDateCreated() == null) {
			practitionerType.setDateCreated(new Date());
		}
		if (practitionerType.getCreator() == null && Context.getAuthenticatedUser() != null) {
			practitionerType.setCreator(Context.getAuthenticatedUser());
		}
		if (StringUtils.isBlank(practitionerType.getUuid())) {
			String candidate = practitionerType.getRhipId();
			practitionerType.setUuid(StringUtils.isBlank(candidate) ? UUID.randomUUID().toString() : candidate.trim());
		}
		return rhipPractitionerTypeDAO.saveOrUpdate(practitionerType);
	}

	@Override
	public RhipPractitionerType getByRhipId(String rhipId) throws APIException {
		return rhipPractitionerTypeDAO.getByRhipId(rhipId);
	}

	@Override
	public String findSubCategoryRhipIdByName(String name) throws APIException {
		return rhipPractitionerTypeDAO.findSubCategoryRhipIdByName(name);
	}

	@Override
	public List<RhipPractitionerType> getByType(String type) throws APIException {
		return rhipPractitionerTypeDAO.getByType(type);
	}
}

