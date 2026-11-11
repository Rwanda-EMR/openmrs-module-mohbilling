package org.openmrs.module.mohbilling.irembo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateToJson {
    public Date date;

    public DateToJson(Date date) {
        this.date = date;
    }

    public static Date deserialize(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            dateFormat.parse(dateString);
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String serialize() {
        // Create a date object

        // Define the date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        // Optionally set the time zone if you need to represent the date in a specific time zone
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2"));

        return dateFormat.format(date);
    }
}

