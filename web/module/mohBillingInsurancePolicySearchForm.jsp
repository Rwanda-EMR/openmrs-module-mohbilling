<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ taglib prefix="mohbilling_tag" tagdir="/WEB-INF/tags/module/mohbilling" %>

<h2><spring:message code="@MODULE_ID@.insurance.policy.searchcreateedit" /></h2>
<openmrs:require privilege="Search Insurance Policy" otherwise="/login.htm" redirect="/module/@MODULE_ID@/insurancePolicySearch.form" />

<mohbilling_tag:insurancePolicySearchByInsuranceCardNumber redirectUrl="insurancePolicy.form" />

<br/>
<spring:message code="@MODULE_ID@.general.or"/>
<br/><br/>

<openmrs:portlet id="findPatient" url="findPatient" parameters="size=full|postURL=insurancePolicy.form|showIncludeVoided=false|viewType=shortEdit|hideAddNewPatient=true" />

<%@ include file="/WEB-INF/template/footer.jsp"%>
