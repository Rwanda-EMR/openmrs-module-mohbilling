<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="View Provider Appointments" otherwise="/login.htm" redirect="/module/mohappointment/providerDashboard.form"/>

<!-- This taglib is imported for the DateFormatting Sake! -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!-- END of "TAGLIB import" -->

<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/style/appointment.css" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/style/listing.css" />

<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery.bigframe.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/ui/ui.core.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/ui/ui.dialog.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/ui/ui.draggable.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/ui/ui.resizable.js" />

<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/theme/ui.all.css" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/theme/demo.css" />

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<h2 style="display: inline;"><spring:message code="@MODULE_ID@.appointment"/></h2> : <span class="boldTitle"><spring:message code="@MODULE_ID@.appointment.today"/></span>
<br/><br/>

<div class="searchParameterBox">
	<table>
		<tr>
			<td><b><spring:message code="@MODULE_ID@.general.provider"/></b></td>
			<td> : ${authenticatedUser.personName}</td>
			<td>&nbsp;&nbsp;&nbsp;&nbsp;<b><spring:message code="@MODULE_ID@.general.location"/></b></td>
			<td> : ${authenticatedUserLoc}</td>
			<td>&nbsp;&nbsp;&nbsp;&nbsp;<b><spring:message code="@MODULE_ID@.general.appointmentdate"/></b></td>
			<td> : <fmt:formatDate value="${todayDate}" pattern="dd-MMM-yyyy" /></td>
		</tr>
	</table>
</div>
<br/>

<form action="providerDashboard.form" method="get">
<div class="searchParameterBox" style="display: ${display_filter};">
	<table>
		<tr>
			<td><b><spring:message code="@MODULE_ID@.general.filter_message"/></b></td>
			<td>
				<select name="services_by_provider">
					<option value="">--</option>
					<c:forEach items="${services}" var="service">
						<option value="${service.serviceId}" <c:if test="${service.serviceId==param.services_by_provider}">selected='selected'</c:if>>${service.name}</option>
					</c:forEach>
				</select>
			</td>
			<td><input type="submit" name="select_service" value="Filter"/></td>
		</tr>
	</table>
</div>
</form>

<div style="searchParameterBox">
	<div class="list_container" style="width: 99%">
		<div class="list_title">
			<div class="list_title_msg"><spring:message code="@MODULE_ID@.state.waiting"/></div>
			<div class="list_title_bts">
				
			</div>
			<div style="clear:both;"></div>
		</div>
		<table class="list_data">
			<tr>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.general.number"/></th>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.general.identifier"/></th>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.general.names"/></th>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.general.gender"/></th>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.general.reasonofappointment"/></th>
				<th class="columnHeader"></th>
			</tr>
			<c:if test="${empty waitingAppointments}">
				<tr>
					<td colspan="5" style="text-align: center;"><spring:message code="@MODULE_ID@.general.empty"/></td>
				</tr>
			</c:if>
			<c:forEach items="${waitingAppointments}" var="wAppointment" varStatus="status">
				<tr>
					<td class="rowValue ${status.count%2!=0?'even':''}">${((param.page-1)*pageSize)+status.count}.</td>
					<td class="rowValue ${status.count%2!=0?'even':''}">${wAppointment.patient.patientIdentifier}</td>
					<td class="rowValue ${status.count%2!=0?'even':''}">
					
					
					<!-- Here is where we should decide whether the link redirects to Lab, Pharmacy or Others -->
						<a href="<openmrs:contextPath/>${wAppointment.patientUrl}?patientId=${wAppointment.patient.patientId}&serviceId=${wAppointment.service.concept.conceptId}&appointmentId=${wAppointment.appointmentId}">${wAppointment.patient.personName}</a>
					</td>
					
					
					<td class="rowValue ${status.count%2!=0?'even':''}">${(wAppointment.patient.gender=='M')?'<img src="../../images/male.gif"/>':'<img src="../../images/female.gif"/>'}</td>
					<td class="rowValue ${status.count%2!=0?'even':''}">${wAppointment.service.concept.name.name}</td>	
					<td class="rowValue ${status.count%2!=0?'even':''}"><input onclick="showResumeDialog('${wAppointment.patient.patientId}');" type="button" value="<spring:message code="@MODULE_ID@.general.viewpatientsummary"/>"/></td>					
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
<br/><br/>

<div style="searchParameterBox">
	<div class="list_container" style="width: 99%">
		<div class="list_title">
			<div class="list_title_msg"><spring:message code="@MODULE_ID@.state.upcoming"/></div>
			<div class="list_title_bts">
			
				
					
			</div>
			<div style="clear:both;"></div>
		</div>
		<table class="list_data">
			<tr>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.general.number"/></th>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.general.identifier"/></th>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.general.names"/></th>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.general.gender"/></th>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.general.appointmentdate"/></th>
				<th class="columnHeader"><spring:message code="@MODULE_ID@.general.reasonofappointment"/></th>
			</tr>
			<c:if test="${empty upcomingAppointments}">
				<tr>
					<td colspan="4" style="text-align: center;"><spring:message code="@MODULE_ID@.general.empty"/></td>
				</tr>
			</c:if>
			<c:forEach items="${upcomingAppointments}" var="uAppointment" varStatus="status">
				<tr>
					<td class="rowValue ${status.count%2!=0?'even':''}">${((param.page-1)*pageSize)+status.count}.</td>
					<td class="rowValue ${status.count%2!=0?'even':''}">${uAppointment.patient.patientIdentifier}</td>
					<td class="rowValue ${status.count%2!=0?'even':''}"><a href="<openmrs:contextPath/>/patientDashboard.form?patientId=${uAppointment.patient.patientId}">${uAppointment.patient.personName}</a></td>
					<td class="rowValue ${status.count%2!=0?'even':''}">${(uAppointment.patient.gender=='M')?'<img src="../../images/male.gif"/>':'<img src="../../images/female.gif"/>'}</td>
					<td class="rowValue ${status.count%2!=0?'even':''}"><fmt:formatDate value="${uAppointment.appointmentDate}" pattern="dd-MMM-yyyy" /></td>
					<td class="rowValue ${status.count%2!=0?'even':''}">${uAppointment.reason.valueCoded.name}</td>	
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

<div id="divDlg"></div>
<div id="dlgCtnt" style="display: none;"></div>

<script>

	function showResumeDialog(pId){
		distroyResultDiv();
		showDialog(pId);
	}
	
	function showDialog(pId){
		$("#divDlg").html("<div id='dialog' style='font-size: 0.9em;' title='<spring:message code='@MODULE_ID@.general.patientsummary'/>'><p><div id='result'>"+pId+"</div></p></div>");
		
		$("#dialog").dialog({
			zIndex: 980,
			bgiframe: true,
			height: 400,
			width: 708,
			modal: true
		});

		var url="patientSummary.form?patientId="+pId;
		$.get(url, function(data) {
			  $('#result').html(data);
		});	
		
	}
	
	function distroyResultDiv(){
		while(document.getElementById("dialog")){
			var DIVtoRemove = document.getElementById("dialog");
			DIVtoRemove.parentNode.removeChild(DIVtoRemove);
		}
	}

	$(document).ready(function(){
	});
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>