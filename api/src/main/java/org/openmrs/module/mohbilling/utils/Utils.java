package org.openmrs.module.mohbilling.utils;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);
    private static final SimpleDateFormat QUERY_DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat FORM_PARAMETER_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    public static List<Obs> getLastNObservations(java.lang.Integer n, Person who, Concept question,
                                                 boolean includeVoided) {
        return Context.getObsService().getObservations(Arrays.asList(who), null, Arrays.asList(question), null, null,
                null, null, n, null, null, null, includeVoided);
    }

    public static String formatDateForQuery(Date date, boolean isStart) {
        return QUERY_DEFAULT_DATE_FORMAT.format(date) + (isStart ? " 00:00:00" : " 23:59:59");
    }

    public static Date formatInputStringToDate(String date) {
        try {
            FORM_PARAMETER_DATE_FORMAT.setLenient(false);
            return FORM_PARAMETER_DATE_FORMAT.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
}
