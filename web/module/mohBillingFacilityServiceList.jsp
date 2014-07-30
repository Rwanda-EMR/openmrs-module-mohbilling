<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<openmrs:require privilege="View Facility service" otherwise="/login.htm" redirect="/module/@MODULE_ID@/facilityService.list" />

<h2><spring:message code="@MODULE_ID@.facility.service.manage" /></h2>
<openmrs:hasPrivilege privilege="Add Facility service">
<a href="facilityService.form"><spring:message code="@MODULE_ID@.facility.service.add" /></a> | 
<a style="color: red;" href="facilityService.list?addCategoryToFacility=UPDATE"><spring:message code="@MODULE_ID@.facility.service.addCategory" /></a>
</openmrs:hasPrivilege>

<br/><br/>

<b class="boxHeader"><spring:message code="@MODULE_ID@.facility.service.current" /></b>
<div class="box">
	<table width="99%">
		<tr>
			<th class="columnHeader"></th>
			<th class="columnHeader">Name</td>
			<th class="columnHeader">Category</td>
			<th class="columnHeader">Description</td>
			<th class="columnHeader">Related Concept</td>
			<th class="columnHeader">Full Price</td>
			<!-- <th class="columnHeader">Start Date</td>-->
			<!-- <th class="columnHeader">Location</td>-->
			<th class="columnHeader"></td>
		</tr>
		<c:if test="${empty facilityServices}"><tr><td colspan="9"><center>No Facility Services found !</center></td></tr></c:if>
		<c:forEach items="${facilityServices}" var="facilityService" varStatus="status">
			<tr>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}. </td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}"><a href="facilityService.form?facilityServiceId=${facilityService.facilityServicePriceId}">${facilityService.name}</a></td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">
					${facilityService.category}
					<c:if test="${empty facilityService.category}">
						<a style="color: red;" href="facilityService.list?facilityServiceId=${facilityService.facilityServicePriceId}">ADD CATEGORY</a>
					</c:if>
				</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${facilityService.description}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${facilityService.concept.name}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: blue;">${facilityService.fullPrice}</b> Rwf</td>
				<!-- <td class="rowValue ${(status.count%2!=0)?'even':''}"><openmrs:formatDate date="${facilityService.startDate}" type="medium"/></td>-->
				<!-- <td class="rowValue ${(status.count%2!=0)?'even':''}">${facilityService.location.name}</td>-->
				<td class="rowValue ${(status.count%2!=0)?'even':''}">
					<openmrs:hasPrivilege privilege="View Facility service by insurance campanies">
						<a href="facilityServiceByInsuranceCompany.list?facilityServiceId=${facilityService.facilityServicePriceId}">Details</a>
					</openmrs:hasPrivilege>
				</td>
			</tr>
		</c:forEach>
	</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>