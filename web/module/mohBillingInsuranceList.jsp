<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<openmrs:require privilege="View Current Insurances" otherwise="/login.htm" redirect="/module/@MODULE_ID@/insurance.list" />
<h2><spring:message code="@MODULE_ID@.insurance.manage" /></h2>
<openmrs:hasPrivilege privilege="Add Insurance">
<a href="insurance.form"><spring:message code="@MODULE_ID@.insurance.add" /></a> </openmrs:hasPrivilege>
<br/><br/>

<b class="boxHeader"><spring:message code="@MODULE_ID@.insurance.current" /></b>
<div class="box">
	<table width="99%">
		<tr>
			<th class="columnHeader"></th>
			<openmrs:hasPrivilege privilege="Edit Insurance"><th class="columnHeader">Name</td></openmrs:hasPrivilege>
			<th class="columnHeader">Related Concept</td>
			<th class="columnHeader">Category</td>
			<th class="columnHeader">Address</td>
			<th class="columnHeader">Phone</td>
			<th class="columnHeader">Flat Fee (Rwf)</td>
			<th class="columnHeader">Rate</td>
			<th class="columnHeader">Service Category</td>
			<th class="columnHeader">Billable Service</td>
		</tr>
		<c:if test="${empty insurances}"><tr><td colspan="9"><center>No Insurances found !</center></td></tr></c:if>
		<c:forEach items="${insurances}" var="insurance" varStatus="status">
			<tr>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
				<openmrs:hasPrivilege privilege="Edit Insurance"><td class="rowValue ${(status.count%2!=0)?'even':''}"><a href="insurance.form?insuranceId=${insurance.insuranceId}">${insurance.name}</a></td></openmrs:hasPrivilege>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${insurance.concept.name}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${insurance.category}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${insurance.address}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${insurance.phone}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${(insurance.currentRate.flatFee ne null)?insurance.currentRate.flatFee:'N/A'}</td>
				<openmrs:hasPrivilege privilege="Edit Insurance Rate"><td class="rowValue ${(status.count%2!=0)?'even':''}">
					<c:if test="${insurance.currentRate.rate!=null}">(${insurance.currentRate.rate}%)&nbsp;<a href="insurance.form?insuranceId=${insurance.insuranceId}">Edit</a></c:if>
					<c:if test="${insurance.currentRate.rate==null}">(-)&nbsp;<a href="insurance.form?insuranceId=${insurance.insuranceId}">Add</a></c:if>
				</td></openmrs:hasPrivilege>
				<openmrs:hasPrivilege privilege="Add New Insurance Service Category"><td class="rowValue ${(status.count%2!=0)?'even':''}">(${insurance.numberOfCategories}) &nbsp; <a href="insuranceServiceCategory.list?insuranceId=${insurance.insuranceId}">Add</a></td></openmrs:hasPrivilege>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">
					<openmrs:hasPrivilege privilege="Add Billable Service"><a href="billableService.form?insuranceId=${insurance.insuranceId}">Add</a> /</openmrs:hasPrivilege> <openmrs:hasPrivilege privilege="View Billable Service by Insurance"><a href="billableService.list?insuranceId=${insurance.insuranceId}">View</a></openmrs:hasPrivilege>
				</td>
			</tr>
		</c:forEach>
	</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>