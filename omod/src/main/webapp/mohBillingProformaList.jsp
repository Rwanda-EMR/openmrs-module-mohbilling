<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/view/module/mohbilling/templates/header.jsp"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<openmrs:require privilege="Billing Proforma - View" otherwise="/login.htm" redirect="/module/@MODULE_ID@/proformas.list" />
<%@ include file="templates/mohBillingBillHeader.jsp"%>

<h2>Proforma Invoices</h2>

<style>
	.proforma-filter td { padding: 4px 8px 4px 0; }
	.proforma-list .num { text-align: right; }
	.proforma-cancelled { color: #a94442; font-weight: bold; }
	.proforma-expired { color: #8a6d3b; }
</style>

<openmrs:hasPrivilege privilege="Billing Proforma - Create">
	<p><a href="proforma.form">+ New proforma invoice</a></p>
</openmrs:hasPrivilege>

<b class="boxHeader">Search</b>
<div class="box proforma-filter">
	<form method="get" action="proformas.list">
		<table>
			<tr>
				<td>Proforma N&deg;</td>
				<td><input type="text" name="identifier" value="<c:out value='${identifier}' />" /></td>
				<td>Patient</td>
				<td><openmrs_tag:patientField formFieldName="patientId" initialValue="${patient.patientId}" /></td>
			</tr>
			<tr>
				<td>Insurance</td>
				<td>
					<select name="insuranceId">
						<option value="">-- All --</option>
						<c:forEach items="${insurances}" var="ins">
							<option value="${ins.insuranceId}" <c:if test="${ins.insuranceId == insuranceId}">selected="selected"</c:if>><c:out value="${ins.name}" /></option>
						</c:forEach>
					</select>
				</td>
				<td>Status</td>
				<td>
					<select name="status">
						<option value="">-- All --</option>
						<option value="ISSUED" <c:if test="${status == 'ISSUED'}">selected="selected"</c:if>>Issued</option>
						<option value="CANCELLED" <c:if test="${status == 'CANCELLED'}">selected="selected"</c:if>>Cancelled</option>
					</select>
				</td>
			</tr>
			<tr>
				<td>Created from</td>
				<td><input type="date" name="startDate" value="<c:out value='${startDate}' />" /></td>
				<td>to</td>
				<td><input type="date" name="endDate" value="<c:out value='${endDate}' />" /></td>
			</tr>
			<tr>
				<td></td>
				<td colspan="3"><input type="submit" value="Search" /> <a href="proformas.list">Reset</a></td>
			</tr>
		</table>
	</form>
</div>

<br />
<b class="boxHeader">Results (${fn:length(proformas)}<c:if test="${fn:length(proformas) >= maxResults}">, showing the latest ${maxResults} - refine your search</c:if>)</b>
<div class="box">
	<table class="list proforma-list" width="100%">
		<tr>
			<th class="columnHeader">Proforma N&deg;</th>
			<th class="columnHeader">Date</th>
			<th class="columnHeader">Patient</th>
			<th class="columnHeader">Insurance</th>
			<th class="columnHeader">Addressed to</th>
			<th class="columnHeader">Total</th>
			<th class="columnHeader">Insurance share</th>
			<th class="columnHeader">Patient share</th>
			<th class="columnHeader">Valid until</th>
			<th class="columnHeader">Status</th>
			<th class="columnHeader"></th>
		</tr>
		<c:forEach items="${proformas}" var="p" varStatus="st">
			<tr class="${st.index % 2 == 0 ? 'evenRow' : 'oddRow'}">
				<td class="rowValue"><a href="proformas.list?proformaId=${p.proformaId}"><c:out value="${p.identifier}" /></a></td>
				<td class="rowValue"><fmt:formatDate value="${p.createdDate}" pattern="dd/MM/yyyy" /></td>
				<td class="rowValue"><c:out value="${p.patient.personName}" /></td>
				<td class="rowValue"><c:out value="${p.insurance.name}" /></td>
				<td class="rowValue"><c:out value="${p.addressee}" /></td>
				<td class="rowValue num"><fmt:formatNumber value="${p.totalAmount}" pattern="#,##0.00" /></td>
				<td class="rowValue num"><fmt:formatNumber value="${p.insuranceAmount}" pattern="#,##0.00" /></td>
				<td class="rowValue num"><fmt:formatNumber value="${p.patientAmount}" pattern="#,##0.00" /></td>
				<td class="rowValue ${p.expired ? 'proforma-expired' : ''}"><fmt:formatDate value="${p.validUntil}" pattern="dd/MM/yyyy" /><c:if test="${p.expired}"> (expired)</c:if></td>
				<td class="rowValue ${p.cancelled ? 'proforma-cancelled' : ''}"><c:out value="${p.status}" /></td>
				<td class="rowValue"><a href="proformas.list?proformaId=${p.proformaId}&print=true">PDF</a></td>
			</tr>
		</c:forEach>
		<c:if test="${empty proformas}">
			<tr><td colspan="11" class="rowValue">No proforma found.</td></tr>
		</c:if>
	</table>
</div>

<%@ include file="/WEB-INF/view/module/mohbilling/templates/footer.jsp"%>
