<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/view/module/mohbilling/templates/header.jsp"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingAdminHeader.jsp"%><br />
<openmrs:require privilege="Manage Global Properties" otherwise="/login.htm" redirect="/module/mohbilling/billingConfig.form" />

<openmrs:htmlInclude file="/dwr/interface/DWRAdministrationService.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<a href="billingConfig.form?updateInsurance=true">Update Insurance on Global bill</a>


<openmrs:portlet url="globalProperties" parameters="title=${title}|propertyPrefix=mohbilling.|excludePrefix=mohbilling.started|hidePrefix=true"/>

<%@ include file="/WEB-INF/view/module/mohbilling/templates/footer.jsp"%>
