package org.openmrs.module.mohbilling.db;

import org.openmrs.module.mohbilling.model.RhipPractitionerType;

import java.util.List;

public interface RhipPractitionerTypeDAO {

	RhipPractitionerType saveOrUpdate(RhipPractitionerType practitionerType);

	RhipPractitionerType getByRhipId(String rhipId);

	String findSubCategoryRhipIdByName(String name);

	List<RhipPractitionerType> getByType(String type);
}

