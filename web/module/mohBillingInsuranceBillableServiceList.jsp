<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<h2><spring:message code="@MODULE_ID@.billable.service.by.insurance" /></h2>

<a href="billableService.form?insuranceId=${insurance.insuranceId}"><spring:message code="@MODULE_ID@.billable.service.add" /></a>
<br/><br/>

<b class="boxHeader"><spring:message code="@MODULE_ID@.insurance.company.title" /></b>
<div class="box">
	<table width="100%">
		<tr>
			<td>Insurance Name</td>
			<td> : <b>${insurance.name}</b></td>
			<td>Related Concept</td>
			<td> : <b>${insurance.concept.name}</b></td>
		</tr>
		<tr>
			<td>Insurance Rate</td>
			<td> : <b>${insurance.currentRate.rate} %</b></td>
			<td>Flat Fee</td>
			<td> : <b>${insurance.currentRate.flatFee} Rwf</b></td>
		</tr>
	</table>
</div>
<br/>

<b class="boxHeader"><spring:message code="@MODULE_ID@.billable.service" /></b>
<div class="box">
	<table width="99%">
		<tr>
			<td class="columnHeader"></td>
			<td class="columnHeader">Name</td>
			<td class="columnHeader">Short Name</td>
			<td class="columnHeader">Concept</td>
			<td class="columnHeader">Service Category</td>
			<td class="columnHeader">Full Price</td>
			<td class="columnHeader">Maxima To Pay</td>
			<td class="columnHeader"></td>
		</tr>
		<c:if test="${empty billableServices}"><tr><td colspan="8"><center>No Billable Services for this Insurance found !</center></td></tr></c:if>
		<c:forEach items="${billableServices}" var="bs" varStatus="status">
			<tr>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}.</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${bs.facilityServicePrice.name}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${bs.facilityServicePrice.shortName}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${bs.facilityServicePrice.concept.name}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${bs.serviceCategory.name}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${bs.facilityServicePrice.fullPrice} Rwf</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${bs.maximaToPay} Rwf</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}"><a href="billableService.form?insuranceId=${insurance.insuranceId}&billableServiceId=${bs.serviceId}">Edit</a></td>
			</tr>
		</c:forEach>
	</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>