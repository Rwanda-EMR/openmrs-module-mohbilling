/**
 * 
 */
package org.openmrs.module.mohbilling.web.dwr;

import org.openmrs.module.mohbilling.model.Beneficiary;
import org.openmrs.web.dwr.PersonListItem;

/**
 * @author Yves GAKUBA
 * 
 */
public class BeneficiaryListItem extends PersonListItem {

	private Integer insurancePolicyId;
	private String insuranceCardNumber;
	private String insurancePolicyNumber;

	public BeneficiaryListItem() {

	}

	public BeneficiaryListItem(Beneficiary ben) {
		super(ben.getPatient());

		this.insurancePolicyId = ben.getInsurancePolicy()
				.getInsurancePolicyId();
		this.insurancePolicyNumber = ben.getInsurancePolicy()
				.getInsuranceCardNo();
		this.insuranceCardNumber = ben.getPolicyIdNumber();

	}

	/**
	 * @return the insurancePolicyNumber
	 */
	public String getInsurancePolicyNumber() {
		return insurancePolicyNumber;
	}

	/**
	 * @param insurancePolicyNumber
	 *            the insurancePolicyNumber to set
	 */
	public void setInsurancePolicyNumber(String insurancePolicyNumber) {
		this.insurancePolicyNumber = insurancePolicyNumber;
	}

	/**
	 * @return the insurancePolicyId
	 */
	public Integer getInsurancePolicyId() {
		return insurancePolicyId;
	}

	/**
	 * @param insurancePolicyId
	 *            the insurancePolicyId to set
	 */
	public void setInsurancePolicyId(Integer insurancePolicyId) {
		this.insurancePolicyId = insurancePolicyId;
	}

	/**
	 * @return the insuranceCardNumber
	 */
	public String getInsuranceCardNumber() {
		return insuranceCardNumber;
	}

	/**
	 * @param insuranceCardNumber
	 *            the insuranceCardNumber to set
	 */
	public void setInsuranceCardNumber(String insuranceCardNumber) {
		this.insuranceCardNumber = insuranceCardNumber;
	}

}
