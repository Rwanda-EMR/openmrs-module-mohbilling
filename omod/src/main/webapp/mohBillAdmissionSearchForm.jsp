<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/view/module/mohbilling/templates/header.jsp"%>
<openmrs:require privilege="Manage Admission" otherwise="/login.htm" redirect="/module/@MODULE_ID@/admissionSearch.form" />

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ taglib prefix="mohbilling_tag" tagdir="/WEB-INF/tags/module/mohbilling" %>

<h2>Patient admission :Search/Create</h2>


<mohbilling_tag:insurancePolicySearchByInsuranceCardNumber redirectUrl="admission.form" />

<br/>



<%@ include file="/WEB-INF/view/module/mohbilling/templates/footer.jsp"%>
