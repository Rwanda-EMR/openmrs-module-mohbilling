<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="mohbilling_tag" tagdir="/WEB-INF/tags/module/mohbilling" %>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<%@ include file="templates/mohBillingDepositHeader.jsp"%>


<div id="search_policy">
	<mohbilling_tag:insurancePolicySearchByInsuranceCardNumber redirectUrl="deposit.form" />
</div>



<%@ include file="/WEB-INF/template/footer.jsp"%>
