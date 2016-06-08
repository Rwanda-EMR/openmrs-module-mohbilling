<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="mohbilling_tag" tagdir="/WEB-INF/tags/module/mohbilling" %>


<%@ include file="templates/mohBillingLocalHeader.jsp"%>


<%@ include file="templates/mohBillingAdminHeader.jsp"%>



<%@ include file="/WEB-INF/template/footer.jsp"%>


<h2><spring:message code="@MODULE_ID@.billing" /></h2>

<div id="search_policy">
	<mohbilling_tag:insurancePolicySearchByInsuranceCardNumber redirectUrl="admission.form" />
</div>



<%@ include file="/WEB-INF/template/footer.jsp"%>
