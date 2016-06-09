<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ taglib prefix="mohbilling_tag" tagdir="/WEB-INF/tags/module/mohbilling" %>

<b>Patient admission :Search/Create</b>b>


<mohbilling_tag:insurancePolicySearchByInsuranceCardNumber redirectUrl="admission.form" />

<br/>



<%@ include file="/WEB-INF/template/footer.jsp"%>
