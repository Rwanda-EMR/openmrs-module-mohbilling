package org.openmrs.module.mohbilling.mariam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.openmrs.module.mohbilling.model.PatientBill;

public class Hibernatequery {
	private static SessionFactory sessionFactory;

	public static void main(String[] args) {
		List<PatientBill> bills = new ArrayList<PatientBill>();
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(PatientBill.class);
		
		Date date = new Date();
		
		 if (date != null) {
			   crit.add(Expression.ge("createdDate", date));
	     }

		 System.out.println(Expression.ge("createdDate", date));
		
		bills=crit.list();
		
		for (PatientBill bill : bills) {
			System.out.println(bill.getPatientBillId());
			System.out.println(bill.getBeneficiary());
		}
	}

}
