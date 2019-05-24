<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/headerFull.jsp"%>
<openmrs:require privilege="Approve Bill" otherwise="/login.htm" redirect="/module/mohbilling/patientBillPayment.form" />