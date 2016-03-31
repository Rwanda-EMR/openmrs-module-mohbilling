<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<h2><spring:message code="@MODULE_ID@.billable.service.new" /></h2>

<a href="insurance.list"><spring:message code="@MODULE_ID@.insurance.current" /></a> &nbsp;&nbsp;<a href="billableService.list?insuranceId=${insurance.insuranceId}">${insurance.name} <spring:message code="@MODULE_ID@.insurance.service.category" /></a>
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
	<form action="billableService.form?save=true" method="post">
		<table>			
			<tr>
				<td>Service Category</td>
				<td>
					<select name="billableServiceServiceCategory">
						<c:forEach items="${insurance.categories}" var="serviceCategory">
							<option value="${serviceCategory.serviceCategoryId}" <c:if test="${billableService.serviceCategory.serviceCategoryId==serviceCategory.serviceCategoryId}">selected='selected'</c:if>>${serviceCategory.name}</option>
						</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td>Facility Service</td>
				<td>
					<select name="billableServiceFacilityService">
						<c:forEach items="${facilityServices}" var="facilityService">
							<option value="${facilityService.facilityServicePriceId}" <c:if test="${billableService.facilityServicePrice.facilityServicePriceId==facilityService.facilityServicePriceId}">selected='selected'</c:if>>${facilityService.name}</option>
						</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td>Maxima To Pay</td>
				<td><input autocomplete="off" type="text" name="billableServiceMaximaToPay" size="10" class="numbers" value="${billableService.maximaToPay}"/> Rwf</td>
			</tr>
			<tr>
				<td>Start Date</td>
				<td><input autocomplete="off" type="text" name="billableServiceStartDate" size="11" onclick="showCalendar(this);" value="<openmrs:formatDate type="string" date="${billableService.startDate}"/>"/></td>
			</tr>
			<tr>
				<td>
					<input type="hidden" value="${insurance.insuranceId}" name="insuranceBillableService"/>
					<c:if test="${param.billableServiceId ne null}"><input type="hidden" value="${billableService.serviceId}" name="billableServiceId"/></c:if>
				</td>
				<td><input type="submit" value="Save Billable Service"/></td>
			</tr>
		</table>
	</form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>