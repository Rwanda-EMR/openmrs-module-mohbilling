package org.openmrs.module.mohbilling.advice;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TreeSet;

public class MohBillingUsageStatsUtils {
	
	 /**
	 * get the  beginning of the day having been given  a date
	 *  
	 * @param date
	 * @return the date(the beginning of the date)
	 */
	public  static String getStartOfDay(Date date) {
		String pattern = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);	
		    
		    Calendar calendar = Calendar.getInstance();
		 
		    Calendar cal = new GregorianCalendar();
			 cal.setTime(date);
		    int year = cal.get(Calendar.YEAR);
		    int month = cal.get(Calendar.MONTH);
		    int day = cal.get(Calendar.DATE);
		    cal.set(year, month, day, 0, 0);
		    return simpleDateFormat.format( cal.getTime());		    
		   
		    
		}

		/**
		 *get the  end  of the day having been given  a date
		 * 
		 * @param date
		 * @return the Date(the end of the day)
		 */
		public  static  String getEndOfDay(Date date) {
			String pattern = "yyyy-MM-dd HH:mm:ss";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			
			 Calendar cal = new GregorianCalendar();
			 cal.setTime(date);
		    int year = cal.get(Calendar.YEAR);
		    int month = cal.get(Calendar.MONTH);
		    int day = cal.get(Calendar.DATE);
		    cal.set(year, month, day, 23, 59);
		    return simpleDateFormat.format( cal.getTime());
		}
		
		public static TreeSet<Date> getDaysBetweenDates(Date startdate, Date endDate)
		{	
			 Calendar cal = new GregorianCalendar();
			 
			    cal.setTime(endDate);
		        cal.add(Calendar.DATE, 1); //minus number would decrement the days
		        Date nextEndDate =cal.getTime();
			
			//==============================
			
			 //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	       
		    TreeSet<Date> dates = new TreeSet<Date>();	   
		    cal.setTime(startdate);

		    while (cal.getTime().before(endDate))
		    {
		     	
		        Date result = cal.getTime();	     	        
		      
		        dates.add(result);
		        cal.add(Calendar.DATE, 1);
		       // formatter.format(day);
		     
		    }
		    return dates;
		}

}
