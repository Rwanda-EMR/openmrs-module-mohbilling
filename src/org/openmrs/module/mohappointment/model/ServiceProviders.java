/**
 * 
 */
package org.openmrs.module.mohappointment.model;

import java.util.Date;

import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.context.Context;

/**
 * @author Kamonyo
 * 
 */
public class ServiceProviders {

	private Integer serviceProviderId;
	private Date startDate;
	private boolean voided;
	private Date voidedDate;
	private String voidedReason;
	private User voidedBy;
	private Person provider;
	private Services service;
	private String names;
	private Date createdDate;
	private User creator;

	/**
	 * (non-Javadoc)
	 * 
	 * @seeorg.openmrs.module.mohappointment.model.IServiceProviders#
	 * getServiceProviderId()
	 */
	public Integer getServiceProviderId() {
		return serviceProviderId;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @seeorg.openmrs.module.mohappointment.model.IServiceProviders#
	 * setServiceProviderId(java.lang.Integer)
	 */
	public void setServiceProviderId(Integer serviceProviderId) {
		this.serviceProviderId = serviceProviderId;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IServiceProviders#getStartDate()
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IServiceProviders#setStartDate
	 * (java.util.Date)
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.mohappointment.model.IServiceProviders#isVoided()
	 */
	public boolean isVoided() {
		return voided;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IServiceProviders#setVoided(boolean
	 * )
	 */
	public void setVoided(boolean voided) {
		this.voided = voided;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IServiceProviders#getVoidedDate()
	 */
	public Date getVoidedDate() {
		return voidedDate;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IServiceProviders#setVoidedDate
	 * (java.util.Date)
	 */
	public void setVoidedDate(Date voidedDate) {
		this.voidedDate = voidedDate;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IServiceProviders#getVoidedReason
	 * ()
	 */
	public String getVoidedReason() {
		return voidedReason;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IServiceProviders#setVoidedReason
	 * (java.lang.String)
	 */
	public void setVoidedReason(String voidedReason) {
		this.voidedReason = voidedReason;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IServiceProviders#getVoidedBy()
	 */
	public User getVoidedBy() {
		return voidedBy;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IServiceProviders#setVoidedBy
	 * (org.openmrs.User)
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	/**
	 * @return the createdDate
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * @param createdDate
	 *            the createdDate to set
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * @return the creator
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            the creator to set
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IServiceProviders#getProvider()
	 */
	public Person getProvider() {
		return provider;
	}

	/**
	 * @param names
	 *            the names to set
	 */
	public void setNames(String names) {
		this.names = names;
	}

	/**
	 * @return
	 */
	public String getNames() {
		System.out.println("_______________Names are just null here : "
				+ this.names);
		if (Context.getUserService().getUser(getProvider().getPersonId())
				.getFamilyName() != null)
			this.names = Context.getUserService().getUser(
					getProvider().getPersonId()).getFamilyName()
					+ Context.getUserService().getUser(
							getProvider().getPersonId()).getGivenName();
		System.out
				.println("_______________I am supposed to see something here : "
						+ this.names);
		if (this.names != null)
			return names;
		else
			return "-";
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IServiceProviders#setProvider
	 * (org.openmrs.Person)
	 */
	public void setProvider(Person provider) {
		this.provider = provider;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IServiceProviders#getService()
	 */
	public Services getService() {
		return service;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmrs.module.mohappointment.model.IServiceProviders#setService(
	 * org.openmrs.module.mohappointment.model.Services)
	 */
	public void setService(Services service) {
		this.service = service;
	}

	// ********** Java overriden methods **********

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		ServiceProviders serviceProviders = (ServiceProviders) obj;
		if (serviceProviders != null)
			if (serviceProviders.getServiceProviderId() == this.serviceProviderId
					&& serviceProviders.hashCode() == this.hashCode()) {
				return true;
			}

		return false;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.serviceProviderId;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "\n - ServiceProvider ID: " + this.serviceProviderId
				+ "\n - Provider (Person) Names: "
				+ this.provider.getPersonName().getFullName()
				+ "\n - Services name: " + this.service.getName()
				+ "\n - Start date: " + this.startDate.toString()
				+ "\n - Voided: " + this.voided;
	}

}
