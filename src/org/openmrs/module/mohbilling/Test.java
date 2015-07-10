package org.openmrs.module.mohbilling;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openmrs.api.context.Context;

public class Test {
	public static void main(String[] args) throws ParseException {
//		Test test = new Test();
//		
//		Kina k1 = test.new Kina();
//		Kina k2 = test.new Kina();
//		
//		
//		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS");
		
		String time = "13:23:10";
		String str = "2014/11/27";
		
		String dateStr = str + " " + time;
		
		Date da = dateFormat.parse(dateStr.replace('/', '-'));
		
//		System.out.println(new Date());
		System.out.println(dateFormat.format(da));

//		System.out.println(Context.getLocale());



	}
	
	public class Kina {
		private Date created_date;

		public Date getCreated_date() {
			return created_date;
		}

		public void setCreated_date(Date created_date) {
			this.created_date = created_date;
		}
	}
}
