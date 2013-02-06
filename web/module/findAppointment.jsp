<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="Search Appointments" otherwise="/login.htm" redirect="/module/mohappointment/findAppointment.form"/>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery.bigframe.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/ui/ui.core.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/ui/ui.dialog.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/ui/ui.draggable.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/ui/ui.resizable.js" />

<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/theme/ui.all.css" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/theme/demo.css" />

<h2><spring:message code="@MODULE_ID@.appointment.find"/></h2>

<script src='<%= request.getContextPath()%>/dwr/interface/MOH_Appointment_DWRUtil.js'></script>

<script type="text/javascript">
	function patientListInTable(item,id){
			if (item.value != null && item.value.length > 2){
				MOH_Appointment_DWRUtil.getPatientListInTable(item.value,id, function(ret){
	
					var box = document.getElementById("resultOfSearch");
					box.innerHTML = ret;
				}); 
			}
		 }
	
	function personValues(personId,personName,id){
		alert("These are values sent : "+personId+" "+personName+" "+id);
	}
	
	function initializeAppointment(appointmentId,ptName,provName,appDate,reason,bt){
		$("#form_changeAppointmentState").html("");
		if(bt==1){
			$("#form_changeAppointmentState").html("<table>"
					+"<tr><td></td><td><input type='hidden' name='appointmentId' value='"+appointmentId+"'/></td></tr>"
					+"<tr><td>Patient Name</td><td> : <b>"+ptName+"</b></td></tr>"
					+"<tr><td>Appointment Date</td><td> : <b>"+appDate+"</b></td></tr>"
					+"<tr><td>Provider</td><td> : <b>"+provName+"</b></td></tr>"
					+"<tr><td>Reason</td><td> : <b>"+reason+"</b></td></tr>"
					+"<tr><td>Appointment State</td><td><select name='appointmentState'>"
					+"<option value='4'>WAITING</option>"
					+"<option value='5'>INADVANCE</option>"
					+"</select></td></tr>"
					+"<tr><td></td><td><input type='submit' value='Save'/></td></tr>"
				+"</table>");
		}else{
			$("#form_changeAppointmentState").html("<table>"
					+"<tr><td><input type='hidden' name='appointmentId' value='"+appointmentId+"'/></td><td><input type='hidden' name='appointmentState' value='8'/></td></tr>"
					+"<tr><td>Patient Name</td><td> : <b>"+ptName+"</b></td></tr>"
					+"<tr><td>Appointment Date</td><td> : <b>"+appDate+"</b></td></tr>"
					+"<tr><td>Provider</td><td> : <b>"+provName+"</b></td></tr>"
					+"<tr><td>Reason</td><td> : <b>"+reason+"</b></td></tr>"
					+"<tr><td>Postponed to</td><td>   <input type='text' name='postponedDate' autocomplete='off' size='11' onclick='showCalendar(this);'/></td></tr>"
					+"<tr><td></td><td><input type='submit' value='Save'/></td></tr>"
				+"</table>");
		}
	}
	
	function showDialog(appointmentId,ptName,provName,appDate,reason,bt){
		initializeAppointment(appointmentId,ptName,provName,appDate,reason,bt);
		$("#divDlg").html("<div id='dialog' style='font-size: 0.9em;' title='<spring:message code='@MODULE_ID@.appointment.state.change'/>'><p><div id='result'>"+$('#dlgCtnt').html()+"</div></p></div>");
		$("#dialog").dialog({
			zIndex: 980,
			bgiframe: true,
			height: 200,
			width: 530,
			modal: true
		});	
	}
</script>

<b class="boxHeader"><spring:message code="@MODULE_ID@.appointment.find"/></b>
<div class="box">
	<table>
		<tr>
			<td>Patient Name/Patient Identifier</td>
			<td><input type="text" style="width:25em" autocomplete="off" value="" onKeyUp='javascript:patientListInTable(this,1);' name='n_1' id='n_1'/></td>
		</tr>
	</table>
	
	<div id='resultOfSearch' style="background: whitesmoke; max-height: 400px; font-size:1em;"></div>
		
</div>

<div id="divDlg"></div>
<div id="dlgCtnt" style="display: none;">
	<form action="findAppointment.form?savechanges=true" method="post">
	
		<div id="form_changeAppointmentState"></div>
		
	</form>
</div>

<script type="text/javascript">
	$(document).ready(function(){
		$("#n_1").focus();
	});
</script>


<%@ include file="/WEB-INF/template/footer.jsp"%>