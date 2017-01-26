<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-1.3.2.js" />
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ taglib prefix="billingtag"
	uri="/WEB-INF/view/module/mohbilling/taglibs/billingtag.tld"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingBillHeader.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<c:if test="${empty paidItems}">
	<b class="boxHeader">Search Refund form by Payment </b>
	<div class="box">
		<form action="" method="get">
			<table>
				<tr>
					<td>Payment identifier</td>
					<td><input type="text" name="paymentId" /></td>
					<td></td>
				</tr>
				<tr>
					<td></td>
					<td><input type="submit" value="Search" /></td>
					<td></td>
				</tr>
			</table>
		</form>
	</div>
</c:if>
<c:if test="${!empty(paidItems)}">

<c:set var="insurancePolicy" value="${insurancePolicy}" />
<c:set var="globalBill" value="${consommation.globalBill}" />
<c:set var="beneficiary" value="${beneficiary}"/>

	<c:import url="mohBillingRefundForm.jsp" />
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>





