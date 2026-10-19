<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/view/module/mohbilling/templates/header.jsp"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<openmrs:require privilege="Billing Proforma - View" otherwise="/login.htm" redirect="/module/@MODULE_ID@/proformas.list" />
<%@ include file="templates/mohBillingBillHeader.jsp"%>

<h2>Proforma Invoice <c:out value="${proforma.identifier}" /></h2>

<style>
	.proforma-info td { padding: 3px 12px 3px 0; }
	.proforma-items .num, .proforma-items .total { text-align: right; }
	.proforma-items .total { font-weight: bold; }
	.proforma-cancelled { color: #a94442; font-weight: bold; }
	.proforma-banner { background: #fcf8e3; border: 1px solid #faebcc; color: #8a6d3b; padding: 6px; margin: 8px 0; }
</style>

<p>
	<a href="proformas.list">&laquo; Back to list</a> |
	<a href="proformas.list?proformaId=${proforma.proformaId}&print=true">Print PDF</a>
	<openmrs:hasPrivilege privilege="Billing Proforma - Create">
		| <a href="proforma.form?patientId=${proforma.patient.patientId}&insuranceId=${proforma.insurance.insuranceId}">New proforma for this patient</a>
	</openmrs:hasPrivilege>
</p>

<div class="proforma-banner">Estimate only: this is not a receipt, a bill or an insurance claim.</div>

<b class="boxHeader">Details</b>
<div class="box">
	<table class="proforma-info">
		<tr><td>Status</td><td class="${proforma.cancelled ? 'proforma-cancelled' : ''}"><c:out value="${proforma.status}" />
			<c:if test="${proforma.cancelled}"> on <fmt:formatDate value="${proforma.cancelledDate}" pattern="dd/MM/yyyy HH:mm" />
				by <c:out value="${proforma.cancelledBy.personName}" />: <c:out value="${proforma.cancelReason}" /></c:if></td></tr>
		<tr><td>Patient</td><td><c:out value="${proforma.patient.personName}" /> (<c:out value="${proforma.patient.patientIdentifier.identifier}" />)</td></tr>
		<tr><td>Insurance</td><td><c:out value="${proforma.insurance.name}" />
			<c:choose>
				<c:when test="${not empty proforma.flatFee && proforma.flatFee > 0}">(patient flat fee <fmt:formatNumber value="${proforma.flatFee}" pattern="#,##0.00" />)</c:when>
				<c:otherwise>(insurance pays ${empty proforma.insuranceRate ? 0 : proforma.insuranceRate}%)</c:otherwise>
			</c:choose></td></tr>
		<c:if test="${not empty proforma.department}"><tr><td>Department</td><td><c:out value="${proforma.department.name}" /></td></tr></c:if>
		<tr><td>Addressed to</td><td><c:out value="${proforma.addressee}" /></td></tr>
		<tr><td>Issued</td><td><fmt:formatDate value="${proforma.createdDate}" pattern="dd/MM/yyyy HH:mm" /> by <c:out value="${proforma.creator.personName}" /></td></tr>
		<tr><td>Valid until</td><td><fmt:formatDate value="${proforma.validUntil}" pattern="dd/MM/yyyy" /><c:if test="${proforma.expired}"> <b>(expired)</b></c:if></td></tr>
		<c:if test="${not empty proforma.notes}"><tr><td>Notes</td><td><c:out value="${proforma.notes}" /></td></tr></c:if>
	</table>
</div>

<br />
<b class="boxHeader">Services</b>
<div class="box">
	<table class="list proforma-items" width="100%">
		<tr>
			<th class="columnHeader">#</th>
			<th class="columnHeader">Category</th>
			<th class="columnHeader">Service</th>
			<th class="columnHeader">Unit price</th>
			<th class="columnHeader">Quantity</th>
			<th class="columnHeader">Amount</th>
		</tr>
		<c:forEach items="${proforma.items}" var="item" varStatus="st">
			<tr>
				<td class="rowValue">${st.index + 1}</td>
				<td class="rowValue"><c:out value="${item.categoryName}" /></td>
				<td class="rowValue"><c:out value="${item.serviceName}" /></td>
				<td class="rowValue num"><fmt:formatNumber value="${item.unitPrice}" pattern="#,##0.00" /></td>
				<td class="rowValue num"><fmt:formatNumber value="${item.quantity}" pattern="#,##0.##" /></td>
				<td class="rowValue num"><fmt:formatNumber value="${item.lineTotal}" pattern="#,##0.00" /></td>
			</tr>
		</c:forEach>
		<tr><td colspan="5" class="rowValue total">TOTAL (100%)</td><td class="rowValue total"><fmt:formatNumber value="${proforma.totalAmount}" pattern="#,##0.00" /></td></tr>
		<tr><td colspan="5" class="rowValue total">INSURANCE SHARE</td><td class="rowValue total"><fmt:formatNumber value="${proforma.insuranceAmount}" pattern="#,##0.00" /></td></tr>
		<tr><td colspan="5" class="rowValue total">PATIENT SHARE</td><td class="rowValue total"><fmt:formatNumber value="${proforma.patientAmount}" pattern="#,##0.00" /></td></tr>
	</table>
</div>

<c:if test="${not proforma.cancelled}">
<openmrs:hasPrivilege privilege="Billing Proforma - Cancel">
<br />
<b class="boxHeader">Cancel proforma</b>
<div class="box">
	<form method="post" action="proformas.list?proformaId=${proforma.proformaId}"
		  onsubmit="return confirm('Cancel this proforma? This cannot be undone.');">
		<input type="hidden" name="action" value="cancel" />
		Reason * <input type="text" name="cancelReason" maxlength="255" size="50" required="required" />
		<input type="submit" value="Cancel proforma" />
	</form>
</div>
</openmrs:hasPrivilege>
</c:if>

<%@ include file="/WEB-INF/view/module/mohbilling/templates/footer.jsp"%>
