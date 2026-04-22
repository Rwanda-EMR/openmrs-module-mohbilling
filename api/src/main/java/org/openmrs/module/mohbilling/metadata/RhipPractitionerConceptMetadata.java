package org.openmrs.module.mohbilling.metadata;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.api.DuplicateConceptNameException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Installs Concepts that can be used to drive selectable RHIP practitioner-related fields
 * (practitionerType, documentType, contractType, practitionerSubCategoryType).
 *
 * This is intentionally lightweight and idempotent: it only creates concepts when missing.
 */
public class RhipPractitionerConceptMetadata {

	private static final Log log = LogFactory.getLog(RhipPractitionerConceptMetadata.class);

	public static final String RHIP_PRACTITIONER_TYPE_CONCEPT_UUID = "2a0f9d50-1fd3-4b24-9b8d-4e7a1c95b2f5";
	public static final String RHIP_DOCUMENT_TYPE_CONCEPT_UUID = "b2b8e1a1-e2a7-4d26-8c9e-3a1c196ff69b";
	public static final String RHIP_CONTRACT_TYPE_CONCEPT_UUID = "f2f0c3a7-95c9-4c5a-b3c6-cd6a7e7b2b0a";
	public static final String RHIP_PRACTITIONER_SUBCATEGORY_CONCEPT_UUID = "9b3c2d2e-7aa9-4c56-8a13-8d1c26b1f3df";

	private static final String ANSWER_PRACTITIONER_LOCAL_UUID = "27e43e0a-39ad-4b61-a1c8-9d7c8d0f5d1a";
	private static final String ANSWER_PRACTITIONER_FOREIGN_UUID = "86f4d29f-6d0e-4f98-9d3f-7c0f4e5e7d5e";

	private static final String ANSWER_DOC_NATIONAL_ID_UUID = "d6f7c2b1-1f62-44e8-b1df-9a9a25a2c46e";
	private static final String ANSWER_DOC_PASSPORT_UUID = "a0d6b10b-4b64-4f0a-a1c2-2e1e3d45caa3";
	// Backwards compatibility with existing deployments that may have stored "NID" in provider attributes.
	private static final String ANSWER_DOC_NID_UUID = "4a49f2d2-70aa-4dd3-8a76-2b3c6f4f9fe8";

	private static final String ANSWER_CONTRACT_FULL_TIME_UUID = "6dbe6e9e-7a8c-46de-9e5a-2f7f3b6d5f1c";
	private static final String ANSWER_CONTRACT_PART_TIME_UUID = "8b5d4e2b-1b0e-4cf4-bc9c-3e9f9a1e2d3a";

	private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

	private RhipPractitionerConceptMetadata() {
	}

	public static void ensureInstalled() {
		ConceptService conceptService = Context.getConceptService();
		if (conceptService == null) {
			log.warn("ConceptService is not available; skipping RHIP practitioner concept metadata install");
			return;
		}

		Concept local = ensureTextConcept(conceptService, ANSWER_PRACTITIONER_LOCAL_UUID, "RHIP Practitioner Type - LOCAL",
				"RHIP practitioner type");
		Concept foreign = ensureTextConcept(conceptService, ANSWER_PRACTITIONER_FOREIGN_UUID, "RHIP Practitioner Type - FOREIGN",
				"RHIP practitioner type");

		Concept nationalId = ensureTextConcept(conceptService, ANSWER_DOC_NATIONAL_ID_UUID, "RHIP Document Type - NATIONAL_ID",
				"RHIP document type");
		Concept passport = ensureTextConcept(conceptService, ANSWER_DOC_PASSPORT_UUID, "RHIP Document Type - PASSPORT",
				"RHIP document type");
		Concept nid = ensureTextConcept(conceptService, ANSWER_DOC_NID_UUID, "RHIP Document Type - NID",
				"RHIP document type (legacy)");

		Concept fullTime = ensureTextConcept(conceptService, ANSWER_CONTRACT_FULL_TIME_UUID, "RHIP Contract Type - FULL_TIME",
				"RHIP contract type");
		Concept partTime = ensureTextConcept(conceptService, ANSWER_CONTRACT_PART_TIME_UUID, "RHIP Contract Type - PART_TIME",
				"RHIP contract type");

		ensureCodedQuestion(conceptService,
				RHIP_PRACTITIONER_TYPE_CONCEPT_UUID,
				"RHIP Practitioner Type",
				"Selectable RHIP practitioner type values used for practitioner registration (LOCAL/FOREIGN).",
				Arrays.asList(local, foreign));

		ensureCodedQuestion(conceptService,
				RHIP_DOCUMENT_TYPE_CONCEPT_UUID,
				"RHIP Practitioner Document Type",
				"Selectable RHIP document type values used for practitioner registration (e.g., NATIONAL_ID, PASSPORT).",
				Arrays.asList(nationalId, passport, nid));

		ensureCodedQuestion(conceptService,
				RHIP_CONTRACT_TYPE_CONCEPT_UUID,
				"RHIP Practitioner Contract Type",
				"Selectable RHIP contract type values used for practitioner registration (FULL_TIME/PART_TIME).",
				Arrays.asList(fullTime, partTime));

		ensureCodedQuestion(conceptService,
				RHIP_PRACTITIONER_SUBCATEGORY_CONCEPT_UUID,
				"RHIP Practitioner Sub-Category Type",
				"Selectable RHIP practitioner sub-category values. These are typically obtained from the RHIP practitioner types endpoint.",
				Collections.emptyList());
	}

	private static Concept ensureTextConcept(ConceptService conceptService, String uuid, String name, String description) {
		Concept existing = getConceptByUuidOrName(conceptService, uuid, name);
		if (existing != null) {
			log.debug("Concept already exists: " + uuid + " (" + name + ")");
			return existing;
		}

		ConceptDatatype textDatatype = conceptService.getConceptDatatypeByName("Text");
		ConceptClass miscClass = conceptService.getConceptClassByName("Misc");
		if (textDatatype == null || miscClass == null) {
			log.warn("Unable to create concept '" + name + "': missing ConceptDatatype 'Text' or ConceptClass 'Misc'");
			return null;
		}

		Concept concept = new Concept();
		concept.setUuid(uuid);
		concept.setDatatype(textDatatype);
		concept.setConceptClass(miscClass);
		addName(concept, name);
		addDescription(concept, description);
		try {
			Concept saved = conceptService.saveConcept(concept);
			log.info("Created concept: " + uuid + " (" + name + ")");
			return saved;
		}
		catch (DuplicateConceptNameException e) {
			Concept byName = conceptService.getConceptByName(name.trim());
			if (byName != null) {
				log.info("Reusing existing concept by name after duplicate-name error: " + name);
				return byName;
			}
			throw e;
		}
	}

	private static Concept ensureCodedQuestion(ConceptService conceptService, String uuid, String name, String description,
	                                          List<Concept> answers) {
		Concept existing = getConceptByUuidOrName(conceptService, uuid, name);
		if (existing != null) {
			log.debug("Concept already exists: " + uuid + " (" + name + ")");
			return existing;
		}

		ConceptDatatype codedDatatype = conceptService.getConceptDatatypeByName("Coded");
		ConceptClass miscClass = conceptService.getConceptClassByName("Misc");
		if (codedDatatype == null || miscClass == null) {
			log.warn("Unable to create concept '" + name + "': missing ConceptDatatype 'Coded' or ConceptClass 'Misc'");
			return null;
		}

		Concept concept = new Concept();
		concept.setUuid(uuid);
		concept.setDatatype(codedDatatype);
		concept.setConceptClass(miscClass);
		addName(concept, name);
		addDescription(concept, description);

		for (Concept answer : answers) {
			if (answer == null) {
				continue;
			}
			concept.addAnswer(new ConceptAnswer(answer));
		}

		try {
			Concept saved = conceptService.saveConcept(concept);
			log.info("Created concept: " + uuid + " (" + name + ")");
			return saved;
		}
		catch (DuplicateConceptNameException e) {
			Concept byName = conceptService.getConceptByName(name.trim());
			if (byName != null) {
				log.info("Reusing existing concept by name after duplicate-name error: " + name);
				return byName;
			}
			throw e;
		}
	}

	private static Concept getConceptByUuidOrName(ConceptService conceptService, String uuid, String name) {
		if (conceptService == null) {
			return null;
		}
		if (StringUtils.isNotBlank(uuid)) {
			Concept byUuid = conceptService.getConceptByUuid(uuid.trim());
			if (byUuid != null) {
				return byUuid;
			}
		}
		if (StringUtils.isNotBlank(name)) {
			return conceptService.getConceptByName(name.trim());
		}
		return null;
	}

	private static void addName(Concept concept, String name) {
		if (concept == null || StringUtils.isBlank(name)) {
			return;
		}
		ConceptName conceptName = new ConceptName(name.trim(), DEFAULT_LOCALE);
		concept.addName(conceptName);
	}

	private static void addDescription(Concept concept, String description) {
		if (concept == null || StringUtils.isBlank(description)) {
			return;
		}
		ConceptDescription conceptDescription = new ConceptDescription(description.trim(), DEFAULT_LOCALE);
		concept.addDescription(conceptDescription);
	}
}
