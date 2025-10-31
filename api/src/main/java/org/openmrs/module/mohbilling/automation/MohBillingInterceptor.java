/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.mohbilling.automation;

import org.apache.commons.lang.BooleanUtils;
import org.hibernate.EmptyInterceptor;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.event.spi.EventSource;
import org.hibernate.type.Type;
import org.openmrs.OpenmrsObject;
import org.openmrs.Voidable;
import org.openmrs.api.context.Context;
import org.openmrs.util.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Iterator;

/**
 * The goal of this interceptor is to add or modify a patient's billable items as billable data is saved
 * This is useful to do in an interceptor rather than with the Event module or with AOP, as an Interceptor provides
 * a mechanism to ensure the billable items are part of the same transaction as the patient data.  An interceptor
 * also provides a means to determine if the objects being saved are new, edited, or deleted/voided, in order to
 * better determine if existing billable items should be edited or removed, and to guard against duplicate bills.
 */
@Component
public class MohBillingInterceptor extends EmptyInterceptor implements ApplicationContextAware {

	private static final Logger log = LoggerFactory.getLogger(MohBillingInterceptor.class);
	private ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	@Override
	public void afterTransactionBegin(Transaction tx) {
		log.trace("afterTransactionBegin");
		for (MohBillingHandler<?> handler : Context.getRegisteredComponents(MohBillingHandler.class)) {
			log.trace("afterTransactionBegin on {}", handler.getClass().getSimpleName());
			handler.afterTransactionBegin();
		}
	}

	/**
	 * Registers a {@link BeforeTransactionCompletionProcess} if none has been registered for the current transaction.
	 * We only want one process per transaction to execute, but since this is registered during the
	 * pre-flush, and multiple flushes can occur within a transaction, we need to ensure that this has not already been
	 * registered before we register it again.  We do this by tracking this on the ThreadLocal by transaction.
	 */
	@Override
	public void preFlush(Iterator entities) {
		SessionFactory sessionFactory = (SessionFactory) context.getBean("sessionFactory");
		EventSource eventSource = (EventSource) sessionFactory.getCurrentSession();
		eventSource.getActionQueue().registerProcess(sessionImpl -> {
			log.trace("beforeTransactionCompletionProcess");
			for (MohBillingHandler<?> handler : Context.getRegisteredComponents(MohBillingHandler.class)) {
				log.trace("Executing {}.beforeTransactionCompletion", handler.getClass().getSimpleName());
				handler.beforeTransactionCompletion();
			}
        });
	}

	@Override
	public void afterTransactionCompletion(Transaction tx) {
		log.trace("afterTransactionCompletion");
		for (MohBillingHandler<?> handler : Context.getRegisteredComponents(MohBillingHandler.class)) {
			log.trace("Executing {}.afterTransactionCompletion", handler.getClass().getSimpleName());
			handler.afterTransactionCompletion();
		}
	}

	/**
	 * This is called when an entity is created, not when it is updated
	 */
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		log.trace("onSave: {}", entity);
		handleCreatedEntity(entity);
		return false;
	}

	/**
	 * This is called only when an entity is updated, not when it is created
	 * The voided property is a special case that we consider generally as representing a delete/undelete operation
	 */
	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
		log.trace("onFlushDirty: {}", entity);
		boolean wasVoided = false;
		boolean isVoided = false;
		if (entity instanceof Voidable) {
			for (int i=0; i<propertyNames.length; i++) {
				String propertyName = propertyNames[i];
				if (propertyName.equals("voided")) {
					wasVoided = BooleanUtils.isTrue((Boolean) previousState[i]);
					isVoided = BooleanUtils.isTrue((Boolean) currentState[i]);
				}
			}
		}
		if (isVoided) {
			if (!wasVoided) {
				handleDeletedEntity(entity);
			}
		}
		else {
			if (wasVoided) {
				handleCreatedEntity(entity);
			}
			else {
				handleUpdatedEntity(entity);
			}
		}
		return false;
	}

	/**
	 * This is called when an entity is deleted
	 */
	@Override
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		log.trace("onDelete: {}", entity);
		handleDeletedEntity(entity);
	}

	/**
	 * Called when a new entity is created or an existing entity is unvoided
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected void handleCreatedEntity(Object entity) {
		log.trace("handleCreatedEntity: {}", entity);
		for (MohBillingHandler handler : HandlerUtil.getHandlersForType(MohBillingHandler.class, entity.getClass())) {
			log.debug("handleCreatedEntity on {} with {}", handler, entity);
			handler.handleCreatedEntity((OpenmrsObject) entity);
		}
	}

	/**
	 * Called when a new non-voided entity is updated
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected void handleUpdatedEntity(Object entity) {
		log.trace("handleUpdatedEntity: {}", entity);
		for (MohBillingHandler handler : HandlerUtil.getHandlersForType(MohBillingHandler.class, entity.getClass())) {
			log.debug("handleUpdatedEntity on {} with {}", handler, entity);
			handler.handleUpdatedEntity((OpenmrsObject) entity);
		}
	}

	/**
	 * Called when an existing entity is either deleted or voided
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected void handleDeletedEntity(Object entity) {
		log.trace("handleDeletedEntity: {}", entity);
		for (MohBillingHandler handler : HandlerUtil.getHandlersForType(MohBillingHandler.class, entity.getClass())) {
			log.debug("handleDeletedEntity on {} with {}", handler, entity);
			handler.handleDeletedEntity((OpenmrsObject) entity);
		}
	}
}
