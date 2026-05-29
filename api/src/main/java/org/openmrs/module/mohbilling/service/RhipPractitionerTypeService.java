package org.openmrs.module.mohbilling.service;

import org.openmrs.api.APIException;
import org.openmrs.module.mohbilling.model.RhipPractitionerType;

import java.util.List;

public interface RhipPractitionerTypeService {

	RhipPractitionerType saveOrUpdate(RhipPractitionerType practitionerType) throws APIException;

	RhipPractitionerType getByRhipId(String rhipId) throws APIException;

	String findSubCategoryRhipIdByName(String name) throws APIException;

	List<RhipPractitionerType> getByType(String type) throws APIException;
}

