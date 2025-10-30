/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.mohbilling.automation;

import org.openmrs.OpenmrsObject;

/**
 * The goal of this interface is to allow registering components that can respond to create/delete/change events
 * for a particular type of OpenmrsObject and modify the patient's bill accordingly
 */
public interface MohBillingHandler<T extends OpenmrsObject> {

	/**
	 * Called after the start of a transaction.  Useful if the handler needs to aggregate operations within a tx
	 */
	void afterTransactionBegin();

	/**
	 * Called after the completion of a transaction.  Useful if the handler needs to aggregate operations within a tx
	 */
	void afterTransactionCompletion();

	/**
	 * Called when a new entity is created or an existing entity is unvoided
	 */
	void handleCreatedEntity(T entity);

	/**
	 * Called when a new non-voided entity is updated
	 */
	void handleUpdatedEntity(T entity);

	/**
	 * Called when an existing entity is either deleted or voided
	 */
	void handleDeletedEntity(T entity);
}
