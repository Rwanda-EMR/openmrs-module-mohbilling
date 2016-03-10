package org.openmrs.module.mohbilling.businesslogic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.hibernate.mapping.Array;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.module.mohbilling.model.BillPayment;
import org.openmrs.module.mohbilling.model.BillStatus;
import org.openmrs.module.mohbilling.model.Consommation;
import org.openmrs.module.mohbilling.model.Insurance;
import org.openmrs.module.mohbilling.model.InsuranceRate;
import org.openmrs.module.mohbilling.model.Invoice;
import org.openmrs.module.mohbilling.model.PatientBill;
import org.openmrs.module.mohbilling.model.PatientInvoice;
import org.openmrs.module.mohbilling.model.PatientServiceBill;
import org.openmrs.module.mohbilling.service.BillingService;



/**
 * Helper class to support the Patient Bill domain
 * 
 * The parent class in this domain is PatientBill, and the child classes are
 * BillPayment, and PatientServiceBill.
 * 
 * 
 * @author dthomas
 * 
 */
public class PatientBillUtil {

	/**
	 * Offers the BillingService to be use to talk to the DB
	 * 
	 * @return the BillingService
	 */
	private static BillingService getService() {

		return Context.getService(BillingService.class);
	}

	/**
	 * @param insurance
	 * @param date
	 * @param unitPrice
	 * @param quantity
	 * @return
	 */
	public static BigDecimal calculateTotal(Insurance insurance, Date date,
			BigDecimal unitPrice, BigDecimal quantity) {

		MathContext mc = new MathContext(BigDecimal.ROUND_DOWN);
		BigDecimal rate = BigDecimal.valueOf(insurance.getRateOnDate(date)
				.getRate());

//		BigDecimal qty = (BigDecimal.valueOf(quantity));
		BigDecimal totalAmount = unitPrice.multiply(quantity, mc);

		return (insurance == null) ? totalAmount : totalAmount.multiply(rate,
				mc);
	}

	/**
	 * This should return all Bills corresponding to a given Patient on a given
	 * date
	 * 
	 * @param patient
	 * @param date
	 * @return
	 */
	public static List<PatientBill> getBillsByPatientOnDate(Patient patient,
			Date date) {

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills()) {
			if (pb.getBeneficiary().getPatient().getPatientId().intValue() == patient
					.getPatientId().intValue()) {
				for (PatientServiceBill psb : pb.getBillItems()) {
					if (psb.getServiceDate().compareTo(date) == 0) {
						bills.add(pb);
						break;
					}
				}
			}
		}
		return bills;
	}

	/**
	 * This should return all Bills corresponding to a given Beneficiary on a
	 * given date
	 * 
	 * @param beneficiary
	 * @param date
	 *            the date on which the Bill
	 * @return bill the list of all matching PatientBills
	 */
	public static List<PatientBill> getBillsByBeneficiaryOnDate(
			Beneficiary beneficiary, Date date) {

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills()) {
			if (pb.getBeneficiary().getBeneficiaryId().intValue() == beneficiary
					.getBeneficiaryId().intValue()) {
				for (PatientServiceBill psb : pb.getBillItems()) {
					if (psb.getServiceDate().compareTo(date) == 0) {
						bills.add(pb);
					}
				}
			}
		}
		return bills;
	}

	/**
	 * This should return all Bills corresponding to a given Patient
	 * 
	 * @param patient
	 * @return
	 */
	public static List<PatientBill> getBillsByPatient(Patient patient) {

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills()) {
			if (pb.getBeneficiary().getPatient() == patient) {
				bills.add(pb);
			}
		}
		return bills;
	}

	/**
	 * This should return all Bills corresponding to a given Beneficiary
	 * 
	 * @param beneficiary
	 * @return
	 */
	public static List<PatientBill> getBillsByBeneficiary(
			Beneficiary beneficiary) {

		return getService().getBillsByBeneficiary(beneficiary);
	}

	/**
	 * This should return all paid Bills on a given date (isPaid == true),
	 * otherwise it returns unpaid (isPaid==false)
	 * 
	 * @param isPaid
	 * @return
	 */
	public static List<PatientBill> getPaidBills(Boolean isPaid, Date date) {

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills()) {
			if (pb.getIsPaid() == true
					&& pb.getCreatedDate().compareTo(date) == 0) {
				bills.add(pb);
			}
		}
		return bills;
	}

	/**
	 * 
	 * This should return all paid Bills on,before,before a given date or
	 * between two dates (isPaid == true), otherwise it returns unpaid
	 * (isPaid==false)
	 * 
	 * @param isPaid
	 * @param date
	 * @return
	 */
	public static List<PatientBill> getPatientBillsInDates(Date startDate,
			Date endDate) {

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills()) {
			if (startDate != null && endDate == null)
				if (pb.getCreatedDate().compareTo(startDate) >= 0)
					bills.add(pb);
			if (startDate == null && endDate != null)
				if (pb.getCreatedDate().compareTo(endDate) <= 0)
					bills.add(pb);
			if (startDate != null && endDate != null)
				if (pb.getCreatedDate().compareTo(startDate) >= 0
						&& pb.getCreatedDate().compareTo(endDate) <= 0)
					bills.add(pb);
		}
		return bills;
	}

	/**
	 * This should return all paid Bills corresponding to a given Beneficiary
	 * (isPaid == true), otherwise it returns unpaid (isPaid==false)
	 * 
	 * @param beneficiary
	 * @param isPaid
	 * @return
	 */
	public static List<PatientBill> getPaidBillsByBeneficiary(
			Beneficiary beneficiary, Boolean isPaid) {

		List<PatientBill> bills = new ArrayList<PatientBill>();

		for (PatientBill pb : getService().getAllPatientBills()) {
			if (pb.getIsPaid() == isPaid
					&& pb.getBeneficiary().equals(beneficiary)) {
				bills.add(pb);
			}
		}
		return bills;
	}

	/**
	 * This should change the PatientBill to printed status (printed == true)
	 * 
	 * @param bill
	 */
	public static void printBill(PatientBill bill) {

		bill.setPrinted(true);
		getService().savePatientBill(bill);
	}

	/**
	 * this calculates the total amount of a Patient Bill (taking into
	 * consideration the List of PatientServiceBill and "unitPrice", "quantity"
	 * and "rate" corresponding to each)
	 * 
	 * @param bill
	 * @param insurance
	 */
	public static void calculateBill(PatientBill bill, Insurance insurance) {

		bill.setAmount(calculateTotalBill(insurance, bill));

		getService().savePatientBill(bill);
	}

	/**
	 * this returns the Total amount due (same as above)
	 * 
	 * @param insurance
	 * @param bill
	 * @return
	 */
	public static BigDecimal calculateTotalBill(Insurance insurance,
			PatientBill bill) {

		BigDecimal amount = new BigDecimal(0);
		MathContext mc = new MathContext(BigDecimal.ROUND_DOWN);

		// The valid Rate for the entered Insurance company
		InsuranceRate validRate = insurance.getRateOnDate(new Date());

		for (PatientServiceBill psb : bill.getBillItems()) {

			amount.add(
					psb.getUnitPrice().multiply((psb.getQuantity()), mc), mc);
		}

		// This returned amount is the one the patient pays (It may change to
		// the total amount without applying the Insurance rate)
		return amount.multiply(BigDecimal.valueOf(validRate.getRate()), mc);
	}

	/**
	 * Creates a PatientBill object and saves it in the DB
	 * 
	 * @param bill
	 *            the PatientBill to be saved
	 * @return bill the PatientBill that has been saved
	 */
	public static PatientBill savePatientBill(PatientBill bill) {

		if (bill != null) {
			getService().savePatientBill(bill);
			return bill;
		}

		return null;
	}

	/**
	 * Creates a PatientServiceBill object and saves it in the DB through
	 * PatientBill which is its parent
	 * 
	 * @param psb
	 *            the PatientServiceBill to be saved
	 * @return psb the PatientServiceBill that has been saved
	 */
	public static PatientServiceBill createPatientServiceBill(
			PatientServiceBill psb) {

		PatientBill bill = new PatientBill();

		if (psb != null) {
			bill = psb.getPatientBill();
			bill.addBillItem(psb);
			getService().savePatientBill(bill);
			return psb;
		}

		return null;
	}

	/**
	 * Creates a BillPayment object and saves it in the DB through PatientBill
	 * which is its parent
	 * 
	 * @param payment
	 *            the BillPayment to be saved
	 * @return payment the BillPayment that has been saved
	 */
	public static BillPayment createBillPayment(BillPayment payment) {

		PatientBill bill = new PatientBill();

		if (payment != null) {
			payment.setVoided(false);
			payment.setCreatedDate(new Date());
			payment.setCreator(Context.getAuthenticatedUser());
			bill = payment.getPatientBill();
			bill.addBillPayment(payment);
			getService().savePatientBill(bill);
			return payment;
		}

		return null;
	}

	/**
	 * The list of services that a patient received and paid during a certain
	 * period >> going to the Data manager/ Accountant/ HC Head
	 * 
	 * @param patient
	 *            the patient to be matched as the one who received care
	 * @param startDate
	 *            the Start date to be considered as the min boundary
	 * @param endDate
	 *            the End date to be considered as the max boundary
	 * @param isPaid
	 *            the Value that determines whether the services are un/paid, if
	 *            null (no Value is provided) it will match without considering
	 *            this Value <code>isPaid</code>
	 * @return bills the lis of un/paid bills on a certain period
	 */
	public static List<PatientBill> getBillsByPatient(Patient patient,
			Date startDate, Date endDate, Boolean isPaid) {

		List<PatientBill> bills = new ArrayList<PatientBill>();

		if (getService().getAllPatientBills() != null)
			for (PatientBill pb : getService().getAllPatientBills())
				if (!pb.isVoided())
					if (pb.getBeneficiary().getPatient().getPatientId()
							.intValue() == patient.getPatientId().intValue()
							&& pb != null && pb.getIsPaid() == isPaid) {

						for (PatientServiceBill psb : pb.getBillItems())
							if (!psb.isVoided()
									&& psb.getServiceDate()
											.compareTo(startDate) >= 0
									&& psb.getServiceDate().compareTo(endDate) <= 0)
								bills.add(pb);
					} else if (pb.getBeneficiary().getPatient().getPatientId()
							.intValue() == patient.getPatientId().intValue()
							&& pb != null) {
						for (PatientServiceBill psb : pb.getBillItems())
							if (!psb.isVoided()
									&& psb.getServiceDate()
											.compareTo(startDate) >= 0
									&& psb.getServiceDate().compareTo(endDate) <= 0) {
								System.out
										.println(" i m getting here rwertwertewrt wertwert ewrtwert wertwert");
								bills.add(pb);
							}

					} else
						bills = null;

		return bills;
	}

	/**
	 * Refunds Patient Bill: When the patient pays for some service, it may
	 * happen that the service is not available at the moment, so this requires
	 * that s/he comes back to the billing desk and ask for refund. This will be
	 * processed by removing those Billable Service from the list of the
	 * services s/he got, and the corresponding amount will be deducted from the
	 * general Total Bill.
	 * 
	 * @param bill
	 *            the one to be refunded
	 * @param services
	 *            the ones to be removed
	 * @return the refunded/updated bill
	 */
	public static PatientBill refundPatientBill(PatientBill bill,
			List<PatientServiceBill> services) {

		for (PatientServiceBill psb : services) {
			bill.removeBillItem(psb);
		}

		return bill;
	}

	/**
	 * Gets the PatientBill by billId
	 * 
	 * @param billId
	 *            the one to be matched
	 * @return the PatientBill that matches the provided billId
	 */
	public static PatientBill getPatientBillById(Integer billId) {

		return getService().getPatientBill(billId);
	}

	/**
	 * Gets Bills by a given Period
	 * 
	 * @param startDate
	 *            the starting period
	 * @param endDate
	 *            the ending period
	 * @return bills as a list of all matched bills
	 */
	public static List<PatientBill> getBillsByPeriod(Date startDate,
			Date endDate) {

		List<PatientBill> bills = null;

		if (startDate != null && endDate != null) {

			bills = new ArrayList<PatientBill>();

			for (PatientBill bill : getService().getAllPatientBills()) {
				if (bill.getCreatedDate().compareTo(startDate) >= 0
						&& bill.getCreatedDate().compareTo(endDate) <= 0) {
					bills.add(bill);
				}
			}
		}

		return bills;
	}
	
	public static PatientBill getPatientBill(Patient patient,Date startDate,
			Date endDate) {

		PatientBill pb = null;

		if (startDate != null && endDate != null) {
			for (PatientBill bill : getPatientBillsInDates(startDate, endDate)) {
				if (bill.getBeneficiary().getPatient()==patient && bill.getCreatedDate().compareTo(startDate) >= 0	&& bill.getCreatedDate().compareTo(endDate) <= 0) {
					pb=bill;
				}
			}
		}

		return pb;
	}
	
	public static void markBillAsPaid(PatientBill bill) {

		PatientBill pb = getService().getPatientBill(bill.getPatientBillId());
		double amountNotPaid = 0d;
		double amountPaid = pb.getAmountPaid().doubleValue();
		double insuranceRate = pb.getBeneficiary().getInsurancePolicy()
				.getInsurance().getCurrentRate().getRate();
		double patientRate = (100f - insuranceRate) / 100f;
		double amountDueByPatient = (pb.getAmount().doubleValue() * patientRate);

		if (pb.getBeneficiary().getInsurancePolicy().getThirdParty() == null)
			
			amountNotPaid = amountDueByPatient - amountPaid;
		else {

			double thirdPartRate = pb.getBeneficiary().getInsurancePolicy()
					.getThirdParty().getRate().doubleValue();

			double amountPaidByThirdPart = pb.getAmount().doubleValue()
					* (thirdPartRate / 100);

			amountNotPaid = amountDueByPatient
					- (amountPaidByThirdPart + amountPaid);
			amountDueByPatient -= amountPaidByThirdPart;

		}

		double rest = amountPaid-amountDueByPatient;
		System.out.println("mmmmmmmmmmmmmmmmmmmmmmmmmmmmrest "+rest);
		/** Marking the BILL as FULLY PAID */
		if (amountPaid >= amountDueByPatient || rest <=1) {
			pb.setIsPaid(true);
			pb.setStatus(BillStatus.FULLY_PAID.getDescription());
		}

		/** Marking the BILL as NOT PAID at all */
		if ( amountNotPaid == amountDueByPatient){
			pb.setStatus(BillStatus.UNPAID.getDescription());
		}
		/** Marking the BILL as PARTLY PAID */
		if (amountNotPaid > 1d && amountNotPaid < amountDueByPatient)
			pb.setStatus(BillStatus.PARTLY_PAID.getDescription());
		
		//System.out.println("llllllllllllllllllllllllllllllllllllRest "+amountNotPaid+"statussssssssss "+pb.getStatus());
		getService().savePatientBill(pb);
	}
	
	public static PatientInvoice getPatientInvoice(PatientBill pb,Insurance insurance ){
		
		Set<PatientServiceBill> billItems =pb.getBillItems(); 
		
		LinkedHashMap< String, Invoice> invoiceMap = new LinkedHashMap<String,Invoice>();
		Double currentRate = pb.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate().doubleValue();	
		LinkedHashMap< String, Double> categGroupedMap = new LinkedHashMap<String,Double>();
		
		LinkedHashMap<String,List<String>> map =getRecoveryCategiesMap();
		Double gdTotal =0.0;	
	for (String categGrouped : map.keySet()) {
		List<String > serviceCategories =map.get(categGrouped);
		
		Double total =0.0;	
		List<Consommation> consommations =new ArrayList<Consommation>();
		Invoice invoice = new Invoice();
		for (String sviceCatgory : serviceCategories) {			
			
			
			Double subTotal =0.0;
			
			for (PatientServiceBill item : billItems) {		
				String category =item.getService().getFacilityServicePrice().getCategory();
				
				if (category.startsWith(sviceCatgory)) {					
					Consommation consomm = new Consommation();
					// Double  quantity = (Double)item.getQuantity();
					
					String libelle = item.getService().getFacilityServicePrice().getName();	
					consomm.setRecordDate(item.getServiceDate());
					consomm.setLibelle(libelle);
					consomm.setUnitCost(item.getUnitPrice().doubleValue());
					consomm.setQuantity(item.getQuantity());
					consomm.setCost(item.getQuantity().doubleValue()*item.getUnitPrice().doubleValue());
					consomm.setInsuranceCost(item.getQuantity().doubleValue()*item.getUnitPrice().doubleValue()*currentRate/100);
					consomm.setPatientCost(item.getQuantity().doubleValue()*item.getUnitPrice().doubleValue()*(100-currentRate)/100);						
					consommations.add(consomm);					
					//Double unitPrice=item.getUnitPrice().doubleValue();
					//Double cost =item.getQuantity()*item.getUnitPrice().doubleValue()*currentRate/100;	
					Double cost =item.getQuantity().doubleValue()*item.getUnitPrice().doubleValue();
					subTotal+=cost;	
					
				}	
			}
			
			invoice.setCreatedDate(pb.getCreatedDate());
			invoice.setConsommationList(consommations);			
			total+=subTotal;            
			}
		invoice.setSubTotal(ReportsUtil.roundTwoDecimals(total));
		//if(invoice.getSubTotal()!=0)

		//get all service categories
		if(!categGrouped.equals("AUTRES"))
		invoiceMap.put(categGrouped, invoice);
		
		//filter ambulance amounts from formalite
		else{
			List<Consommation> autresConso = invoice.getConsommationList();
			List<Consommation> ambulanceConsom = new ArrayList<Consommation>();
			List<Consommation> formaliteConsom=new ArrayList<Consommation>();
			Invoice ambulanceInvoice  = new Invoice();
			Invoice formaliteInvoice = new Invoice();
			Double subTotAmbul = 0.0,subTotalFormalites=0.0;
			for (Consommation c : autresConso) {
				if(c.getLibelle().startsWith("Ambul")){
					ambulanceConsom.add(c);
					subTotAmbul+=c.getCost();
				}
				else{
					formaliteConsom.add(c);
					subTotalFormalites+=c.getCost();
				}
			}
			ambulanceInvoice.setConsommationList(ambulanceConsom);
			ambulanceInvoice.setCreatedDate(pb.getCreatedDate());
			ambulanceInvoice.setSubTotal(subTotAmbul);
			
			formaliteInvoice.setConsommationList(formaliteConsom);
			formaliteInvoice.setCreatedDate(pb.getCreatedDate());
			formaliteInvoice.setSubTotal(subTotalFormalites);
			
			invoiceMap.put("AMBULANCE", ambulanceInvoice);
			invoiceMap.put("AUTRES", formaliteInvoice);
		}
	
		
		
		gdTotal+=total;		
		
		//categGroupedMap.put(categGrouped, ReportsUtil.roundTwoDecimals(total));		
	
	}
	//create patient invoice  to  be displayed on the interface

	PatientInvoice patientInvoice = new PatientInvoice();
	
	patientInvoice.setPatientBill(pb);
	patientInvoice.setInvoiceMap(invoiceMap);
	patientInvoice.setTotalAmount( ReportsUtil.roundTwoDecimals(gdTotal));
	patientInvoice.setPatientCost(ReportsUtil.roundTwoDecimals(gdTotal*(100-currentRate)/100));
	patientInvoice.setInsuranceCost( ReportsUtil.roundTwoDecimals(gdTotal*currentRate/100));
	
	//add each invoice linked to catehory service to list of invoice		
	/*categGroupedMap.put("Montant100%", ReportsUtil.roundTwoDecimals(gdTotal));
	categGroupedMap.put("T.M10%", ReportsUtil.roundTwoDecimals(gdTotal*(100-currentRate)/100));
	categGroupedMap.put("totalMS", ReportsUtil.roundTwoDecimals(gdTotal*currentRate/100));*/
	
	return  patientInvoice;
		
	}
	
 public static LinkedHashMap<String,List<String>> getRecoveryCategiesMap(){
	LinkedHashMap<String,List<String>> map = new LinkedHashMap<String, List<String>>();
	List<String> consult = Arrays.asList("CONSULTATION");
	List<String> labo = Arrays.asList("LABORATOIRE");
	List<String> imagery = Arrays.asList("ECHOGRAPHIE", "RADIOLOGIE");
	List<String> medicActs = Arrays.asList("STOMATOLOGIE", "CHIRURGIE","SOINS INTENSIFS","GYNECO - OBSTETRIQUE","ORL","DERMATOLOGIE", "SOINS INFIRMIERS","MATERNITE","OPHTALMOLOGIE","KINESITHERAPIE","MEDECINE INTERNE","NEUROLOGIE");
	List<String> medic = Arrays.asList("MEDICAMENTS");
	List<String> consommables = Arrays.asList("CONSOMMABLES");
	List<String> ambul = Arrays.asList("AMBULANCE");
	List<String> autres = Arrays.asList("FORMALITES ADMINISTRATIVES","OXYGENOTHERAPIE");
	List<String> hosp = Arrays.asList("HOSPITALISATION");
	
	map.put("CONSULTATION", consult);
	map.put("LABORATOIRE", labo);
	map.put("IMAGERIE", imagery);
	map.put("ACTS", medicActs);
	map.put("MEDICAMENTS", medic);
	map.put("CONSOMMABLES", consommables);
	map.put("AMBULANCE", ambul);
	map.put("AUTRES", autres);
	map.put("HOSPITALISATION", hosp);
	return map;
}
 
 public static Set<PatientBill> getRefundedBill(Date startDate, Date endDate, User collector){
	
	return  getService().getRefundedBills(startDate, endDate, collector);
}
 
// public static PatientInvoice getRevenueFromOtherService(PatientBill pb,String other){
//	 List<Consommation> consommations = new ArrayList<Consommation>();
//	 Consommation conso = new Consommation();
//	 Double currentRate = pb.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate().doubleValue();	
//	 Invoice invoice = new Invoice();
//	 Double subTotal=0.0,total = 0.0,gdTotal=0.0;
//	 PatientInvoice patientInvoice = new PatientInvoice();
//		for (PatientServiceBill item : pb.getBillItems()) {
//			if(item.getService().getFacilityServicePrice().getName().startsWith(other)){
//			//set new consommation
//			String libelle = item.getService().getFacilityServicePrice().getName();	
//			conso.setRecordDate(item.getServiceDate());
//			conso.setLibelle(libelle);
//			conso.setUnitCost(item.getUnitPrice().doubleValue());
//			conso.setQuantity(item.getQuantity());
//			conso.setCost(item.getQuantity().doubleValue()*item.getUnitPrice().doubleValue());
//			conso.setInsuranceCost(item.getQuantity().doubleValue()*item.getUnitPrice().doubleValue()*currentRate/100);
//			conso.setPatientCost(item.getQuantity().doubleValue()*item.getUnitPrice().doubleValue()*(100-currentRate)/100);						
//								
//			//add to cons list
//			consommations.add(conso);
//			Double cost =item.getQuantity().doubleValue()*item.getUnitPrice().doubleValue();
//			//set the subtotal
//			subTotal+=cost;	
//
//		}
//	   }
//		//Invoice,set the created date
//		invoice.setCreatedDate(new Date());
//		invoice.setConsommationList(consommations);		
//		total+=subTotal;   
//		//cons list
//		
//		invoice.setSubTotal(total);
//		//if(invoice.getSubTotal()!=0)
//		LinkedHashMap< String, Invoice> invoiceMap = new LinkedHashMap<String,Invoice>();
//		invoiceMap.put("Amb", invoice);
//		
//		gdTotal+=total;
//
//		//create patient invoice
//		patientInvoice.setPatientBill(pb);
//		patientInvoice.setInvoiceMap(invoiceMap);
//		patientInvoice.setTotalAmount( ReportsUtil.roundTwoDecimals(gdTotal));
//		patientInvoice.setPatientCost(ReportsUtil.roundTwoDecimals(gdTotal*(100-30)/100));
//		patientInvoice.setInsuranceCost( ReportsUtil.roundTwoDecimals(gdTotal*30/100));
//		
//	 return patientInvoice;
// }
}
