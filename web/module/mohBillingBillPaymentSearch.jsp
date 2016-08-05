<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/mohbilling/taglibs/billingtag.tld" %>
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingBillHeader.jsp"%>


<b class="boxHeader">Search Bill Payment </b>
<div class="box" style="height: 60px;">
	<div style="float: left; height: inherit;">
		<form action="" method="get">
			<table>
				<tr>
					<td>Payment identifier</td>
					<td><input type="text" name="paymentId" /></td>
					<td></td>
				</tr>
				<tr>
					<td></td>
					<td><input type="submit" value="Search Payment" /></td>
					<td></td>
				</tr>
			</table>
		</form>
	</div>	
</div>
<br/>


<b class="boxHeader">Search Refund by Bill  Payment </b>
<div class="box" style="height: 60px;">
	<div style="float: left; height: inherit;">
		<form action="" method="get">
			<table>
				<tr>
					<td>Payment identifier</td>
					<td><input type="text" name="paymentId" /></td>
					<td></td>
				</tr>
				<tr>
					<td></td>
					<td><input type="submit" value="Search Refund" /></td>
					<td></td>
				</tr>
			</table>
		</form>
	</div>	
</div>
<br/>
<c:if test="${!empty(paidItems)}">

<c:set var="paidItems" value="${paidItems}" />
<c:set var="payment" value="${payment}" />
<c:set var="consommation" value="${consommation}"/>

	<c:import url="mohBillingPaidServiceBillList.jsp" />
</c:if>
<%@ include file="/WEB-INF/template/footer.jsp"%>