<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<openmrs:require privilege="Add Facility service" otherwise="/login.htm" redirect="/module/@MODULE_ID@/facilityService.form" />


<h2>New Facility Service</h2>

<a href="facilityService.list"><spring:message code="@MODULE_ID@.facility.service.current" /></a>
<br/><br/>

<div class="box">
	<form action="facilityService.form?save=true" method="post">
		<table>
			<tr>
				<td>Name</td>
				<td><input autocomplete="off" type="text" name="facilityServiceName" size="60" value="${facilityService.name}"/></td>
			</tr>
			<tr>
				<td>Related Concept</td>
				<td><openmrs_tag:conceptField formFieldName="facilityServiceRelatedConcept" initialValue="${facilityService.concept.conceptId}" /></td>
			</tr>
			<tr>
				<td>Short Name</td>
				<td><input autocomplete="off" type="text" name="facilityServiceShortName" size="20" value="${facilityService.shortName}"/></td>
			</tr>
			<tr>
				<td>Description</td>
				<td><textarea cols="30" rows="3" name="facilityServiceDescription">${facilityService.description}</textarea></td>
			</tr>
			<tr>
				<td>Full Price</td>
				<td><input autocomplete="off" type="text" name="facilityServiceFullPrice" size="8" class="numbers" value="${facilityService.fullPrice}"/> Rwf</td>
			</tr>
			<tr>
				<td>Start Date</td>
				<td><input autocomplete="off" type="text" name="facilityServiceStartDate" size="11" onclick="showCalendar(this);" value="<openmrs:formatDate date="${facilityService.startDate}" type="string"/>"/></td>
			</tr>
			<tr>
				<td>Location</td>
				<td>
					<select name="facilityServiceLocation">
						<c:forEach items="${locations}" var="location">
							<option value="${location.locationId}" <c:if test='${facilityService.location.locationId==location.locationId}'>selected='selected'</c:if>>${location.name}</option>
						</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td>
					<c:if test="${param.facilityServiceId ne null}">
						<input type="hidden" name="facilityServiceId" value="${param.facilityServiceId}"/>
					</c:if>
				</td>
				<td><input type="submit" value="Save Facility Service"/></td>
			</tr>
		</table>
	</form>
</div>
<br/>

<c:if test="${param.facilityServiceId ne null}">
	<b class="boxHeader">Retire this Facility Service</b>
	<div class="box">
		<form action="facilityService.form?retire=true" method="post">
			<table>
				<tr>
					<td>Retire Reason</td>
					<td><input autocomplete="off" type="text" name="facilityServiceRetireReason" size="60"/></td>
				</tr>
				<tr>
					<td><input type="hidden" name="facilityServiceId" value="${facilityService.facilityServicePriceId}"/></td>
					<td><input type="submit" value="Retire Facility Service"/></td>
				</tr>
			</table>
		</form>
	</div>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>