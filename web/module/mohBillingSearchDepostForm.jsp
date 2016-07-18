<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="mohbilling_tag" tagdir="/WEB-INF/tags/module/mohbilling" %>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<%@ include file="templates/mohBillingDepositHeader.jsp"%>


<div>
	<openmrs:portlet id="findPatient" url="findPatient" parameters="size=full|postURL=deposit.form|showIncludeVoided=false|viewType=shortEdit|hideAddNewPatient=true" />
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>
