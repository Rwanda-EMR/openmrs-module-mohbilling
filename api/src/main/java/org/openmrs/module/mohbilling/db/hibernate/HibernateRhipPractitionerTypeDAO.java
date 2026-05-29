package org.openmrs.module.mohbilling.db.hibernate;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.openmrs.module.mohbilling.db.RhipPractitionerTypeDAO;
import org.openmrs.module.mohbilling.model.RhipPractitionerType;

import java.util.Collections;
import java.util.List;

public class HibernateRhipPractitionerTypeDAO implements RhipPractitionerTypeDAO {

	private SessionFactory sessionFactory;

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public RhipPractitionerType saveOrUpdate(RhipPractitionerType practitionerType) {
		if (practitionerType == null) {
			return null;
		}
		sessionFactory.getCurrentSession().saveOrUpdate(practitionerType);
		return practitionerType;
	}

	@Override
	public RhipPractitionerType getByRhipId(String rhipId) {
		if (StringUtils.isBlank(rhipId)) {
			return null;
		}
		return (RhipPractitionerType) sessionFactory.getCurrentSession()
				.createQuery("from RhipPractitionerType where voided = false and rhipId = :rhipId")
				.setString("rhipId", rhipId.trim())
				.uniqueResult();
	}

	@Override
	public String findSubCategoryRhipIdByName(String name) {
		if (StringUtils.isBlank(name)) {
			return null;
		}
		RhipPractitionerType ret = (RhipPractitionerType) sessionFactory.getCurrentSession()
				.createQuery("from RhipPractitionerType where voided = false and lower(name) = :name and type = :type")
				.setString("name", name.trim().toLowerCase())
				.setString("type", "SUB_CATEGORY")
				.setMaxResults(1)
				.uniqueResult();
		return ret == null ? null : ret.getRhipId();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<RhipPractitionerType> getByType(String type) {
		if (StringUtils.isBlank(type)) {
			return Collections.emptyList();
		}
		return (List<RhipPractitionerType>) sessionFactory.getCurrentSession()
				.createQuery("from RhipPractitionerType where voided = false and type = :type order by name asc")
				.setString("type", type.trim())
				.list();
	}
}

