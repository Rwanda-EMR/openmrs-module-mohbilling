<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/style/listing.css" />

<div class="searchParameterBox box">
	<div class="list_container" style="width: 99%">
		<div class="list_title">
			<div class="list_title_msg"><spring:message code="@MODULE_ID@.appointment.patient"/></div>
			<div class="list_title_bts">
			
				<!-- <form style="display: inline;" action="#" method="post">
					<input type="submit" class="list_exportBt" value="<spring:message code="@MODULE_ID@.general.export"/>"/>
				</form> -->	
					
			</div>
			<div style="clear:both;"></div>
		</div>
		<table class="list_data">
			<tr>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.general.appointmentdate"/></th>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.general.number"/></th>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.general.provider"/></th>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.general.reasonofappointment"/></th>
				<!-- <th class="columnHeader"><spring:message code="@MODULE_ID@.general.clinicalareatosee"/></th> -->
				<th class="columnHeader"><spring:message code="@MODULE_ID@.general.location"/></th>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.general.state"/></th>
				<th class="columnHeader"></th>
			</tr>
			<c:if test="${empty appointments}">
				<tr>
					<td colspan="7" style="text-align: center;"><spring:message code="@MODULE_ID@.general.empty"/></td>
				</tr>
			</c:if>
			<c:set value="0" var="index"/>
			<c:forEach items="${appointments}" var="appointment" varStatus="status">
				<tr>
					<c:choose>
					  <c:when test="${appointment.appointmentDate == currentDate}">
					   	<td class="rowValue" <c:if test="${index%2!=0}">style="background-color: whitesmoke;"</c:if>><c:if test="${appointment.appointmentDate!=currentDate}"><openmrs:formatDate date="${appointment.appointmentDate}" type="medium"/><c:set value="${appointment.appointmentDate}" var="currentDate"/></c:if></td>
					  </c:when>
					  <c:otherwise>
					  	<c:set value="${index+1}" var="index"/>
					   	<td class="rowValue" style="border-top: 1px solid cadetblue; <c:if test="${index%2!=0}">background-color: whitesmoke;</c:if>"><c:if test="${appointment.appointmentDate!=currentDate}"><openmrs:formatDate date="${appointment.appointmentDate}" type="medium"/><c:set value="${appointment.appointmentDate}" var="currentDate"/></c:if></td>
					  </c:otherwise>
					</c:choose>
					<td class="rowValue ${status.count%2!=0?'even':''}">${((param.page-1)*pageSize)+status.count}.</td>
					<td class="rowValue ${status.count%2!=0?'even':''}">${appointment.provider.personName}</td>
					<td class="rowValue ${status.count%2!=0?'even':''}">${appointment.service.concept.name.name}</td>
					<!-- <td class="rowValue ${status.count%2!=0?'even':''}">-</td> -->
					<td class="rowValue ${status.count%2!=0?'even':''}">${appointment.location}</td>
					<td class="rowValue ${status.count%2!=0?'even':''}">${appointment.appointmentState.description}</td>
					<td class="rowValue ${status.count%2!=0?'even':''}">
					<a href="<openmrs:contextPath/>/patientDashboard.form?patientId=${appointment.patient.patientId}&attended=true&appointmentId=${appointment.appointmentId}">
					<spring:message code="@MODULE_ID@.general.attended"/></a> &nbsp; 
					<a href="<openmrs:contextPath/>/patientDashboard.form?patientId=${appointment.patient.patientId}&cancel=true&appointmentId=${appointment.appointmentId}"><spring:message code="@MODULE_ID@.general.cancel"/></a></td>
				</tr>
			</c:forEach>
		</table>
		<div class="list_footer">
			<div class="list_footer_info">&nbsp;&nbsp;&nbsp;</div>
			<div class="list_footer_pages">		
				&nbsp;&nbsp;&nbsp;	
			</div>
			<div style="clear: both"></div>
		</div>
	</div>

</div>