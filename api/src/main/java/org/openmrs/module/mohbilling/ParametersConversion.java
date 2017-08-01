/**
 * 
 */
package org.openmrs.module.mohbilling;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author emr
 *
 */
public class ParametersConversion {
	
	/**
	 * The meth
	 * @param request
	 * @param name
	 * @return
	 */
	public static Date getStartDate(String startDateStr,String startHourStr,String startMinute){
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		
		String startTimeStr = startHourStr + ":" + startMinute + ":00";
		
		Date startDate=null;
		try {
			if(!startHourStr.equals("") && startMinute!=null)
			startDate = sdf.parse(startDateStr.split("/")[2] + "-"
					+ startDateStr.split("/")[1] + "-"
					+ startDateStr.split("/")[0] + " " + startTimeStr);
			else{
				startTimeStr="";
				startDate = sdf1.parse(startDateStr.split("/")[2] + "-"
						+ startDateStr.split("/")[1] + "-"
						+ startDateStr.split("/")[0]
						+startTimeStr
								);
			}
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		return startDate;
		
	}
	public static  Date getEndDate(String endDateStr,String endHourStr,String endMinuteStr){

		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		
		//String endTimeStr = endHourStr + ":" + endMinuteStr + ":59";
		String endTimeStr = "23:59:59";


		Date endDate =null;
		try {
			endDate = sdf.parse(endDateStr.split("/")[2] + "-" + endDateStr.split("/")[1] + "-" + endDateStr.split("/")[0] + " " + endTimeStr);
		}catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*try {
			if(!endHourStr.equals("") && endMinuteStr!=null){
			endDate = sdf.parse(endDateStr.split("/")[2] + "-"
					+ endDateStr.split("/")[1] + "-" + endDateStr.split("/")[0]
					+ " " + endTimeStr);
			}
			else{
				endTimeStr = "";
				endDate = sdf1.parse(endDateStr.split("/")[2] + "-"
						+ endDateStr.split("/")[1] + "-" + endDateStr.split("/")[0]
						+" "+endTimeStr);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		return endDate;
		
	}
	public static Date getConvertStringToDate(String dateStr){
		DateFormat df = new SimpleDateFormat(dateStr);
		Date date = null;
		if (dateStr != null && dateStr.length() != 0) {
			try {
				date =   df.parse(dateStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return date;
	}

}
