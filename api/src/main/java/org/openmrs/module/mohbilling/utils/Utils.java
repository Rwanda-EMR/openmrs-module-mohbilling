package org.openmrs.module.mohbilling.utils;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.context.Context;

public class Utils {

	
	protected static final Log log = LogFactory.getLog(Utils.class);	
	public static List<Obs> getLastNObservations(java.lang.Integer n, Person who, Concept question, boolean includeVoided) {
		return Context.getObsService().getObservations(Arrays.asList( who), null, Arrays.asList(question), null, null, null, null, n, null, null, null, includeVoided);
	}
}
