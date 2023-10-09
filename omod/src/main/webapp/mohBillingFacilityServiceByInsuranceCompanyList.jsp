<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<openmrs:require privilege="View Bulk Update" otherwise="/login.htm" redirect="/module/@MODULE_ID@/facilityServiceByInsuranceCompany.list" />


<h2><spring:message code="@MODULE_ID@.facility.service.by.insurance" /></h2>

<b class="boxHeader"><spring:message code="@MODULE_ID@.facility.service" /></b>
<div class="box">
	<table width="100%">
		<tr>
			<td>Name</td>
			<td> : <b>${facilityService.name}</b></td>
			<td>Short Name</td>
			<td> : <b>${facilityService.shortName}</b></td>
			<td>Related Concept</td>
			<td> : <b>${facilityService.concept.name}</b></td>
		</tr>
		<tr>
			<td>Location</td>
			<td> : <b>${facilityService.location.name}</b></td>
			<td>Full Price</td>
			<td> : <b>${facilityService.fullPrice} Rwf</b></td>
			<td></td>
			<td></td>
		</tr>
	</table>
</div>
<br/>

<b class="boxHeader"><spring:message code="Billables by insurance Form" /></b>
<div class="box">
	<form method="post" action="facilityServiceByInsuranceCompany.list?facilityServiceId=${param.facilityServiceId}">
		<table width="100%">
			<tr>
				<td>Start Date <input type="hidden" name="facilityServiceId" value="${param.facilityServiceId}" />
				 : <input autocomplete="off" type="text" name="startDate" size="11" onclick="showCalendar(this);"/></td>
			</tr>
			<tr>
            				<td>Which Item(s): <select name="whichitem"><option value="thisitem">This Item</option> <option value="allitems">All Items</option></select> </td>
            			</tr>
			<tr>
				<td><input type="submit" value="Bulk Update" /></td>
			</tr>
		</table>
	</form>
</div>
<br/>

<b class="boxHeader"><spring:message code="@MODULE_ID@.insurance.on.service" /></b>
<div class="box">
	<table width="99%">
		<tr>
			<th class="columnHeader"></th>
			<th class="columnHeader">Insurance</td>
			<th class="columnHeader">Rate</td>
			<th class="columnHeader">Flat Fee</td>
			<th class="columnHeader">Maxima To Pay</td>
			<th class="columnHeader">Service Category</td>
			<th class="columnHeader"></td>
		</tr>
		<c:if test="${empty billableServices}"><tr><td colspan="7"><center>No Insurance on this Facility Service found !</center></td></tr></c:if>
		<c:forEach items="${billableServices}" var="billableService" varStatus="status">
			<tr>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}. </td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${billableService.insurance.name}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${billableService.insurance.currentRate.rate}%</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${billableService.insurance.currentRate.flatFee} Rwf</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${billableService.maximaToPay} Rwf</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${billableService.serviceCategory.name}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}"><a href="billableService.form?insuranceId=${billableService.insurance.insuranceId}&billableServiceId=${billableService.serviceId}">Edit</a></td>
			</tr>
		</c:forEach>
	</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>