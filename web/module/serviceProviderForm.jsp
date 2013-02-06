<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="Manage Services and Providers" otherwise="/login.htm" redirect="/module/mohappointment/serviceProvider.form"/>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />

<%@ include file="templates/serviceProviderHeader.jsp"%>

<h2><spring:message code="@MODULE_ID@.appointment.service.provider.form"/></h2>
<br/>

<script type="text/javascript">
	var $j = jQuery.noConflict();
</script>

<!-- <b class="boxHeader"><spring:message code="@MODULE_ID@.appointment.service.provider.current"/></b> -->
<form action="serviceProvider.form?save=true" method="post" class="box">
	<div id="errorDiv"></div><br/>
	<table>
		<tr>
			<td><b><spring:message code="@MODULE_ID@.general.provider"/></b></td>
			<td><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
			<td><openmrs_tag:userField roles="Provider" formFieldName="provider"/></td>
			<td valign="top"><span id="providerError"></span></td>
		</tr>
		<tr>
			<td><b><spring:message code="@MODULE_ID@.general.service"/></b></td>
			<td><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
			<td><select name="service" id="service">
					<option value="">--</option>
					<c:forEach items="${services}" var="service">
						<option value="${service.serviceId}">${service.name}</option>
					</c:forEach>
				</select>
			</td>
			<td valign="top"><span id="serviceError"></span></td>
		</tr>
		<tr>
			<td><b><spring:message code="@MODULE_ID@.general.startdate"/></b></td>
			<td><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
			<td><input type="text" name="startDate" id="startDate" size="11" onclick="showCalendar(this);"/></td>
			<td valign="top"><span id="startDateError"></span></td>
		</tr>
		<tr>
			<td></td>
			<td></td>
			<td><input type="button" id="btSave" value="<spring:message code='@MODULE_ID@.general.save'/>"></td>
			<td></td>
		</tr>
	</table>
</form>

<script>
	$j(document).ready(function(){
		$j("#btSave").click(function(){
			if(validateFormFields()){
				if(confirm("<spring:message code='@MODULE_ID@.general.save.confirm'/>"))
					this.form.submit();
			}
		});
	});

	function validateFormFields(){
		var valid=true;
		if(document.getElementsByName("provider")[0].value==''){
			$j("#providerError").html("*");
			$j("#providerError").addClass("error");
			valid=false;
		} else {
			$j("#providerError").html("");
			$j("#providerError").removeClass("error");
		}

		if(document.getElementById("service").value==''){
			$j("#serviceError").html("*");
			$j("#serviceError").addClass("error");
			valid=false;
		} else {
			$j("#serviceError").html("");
			$j("#serviceError").removeClass("error");
		}

		if($j("#startDate").val()==''){
			$j("#startDateError").html("*");
			$j("#startDateError").addClass("error");
			valid=false;
		} else {
			$j("#startDateError").html("");
			$j("#startDateError").removeClass("error");
		}

		if(!valid){
			$j("#errorDiv").html("<spring:message code='@MODULE_ID@.general.fillbeforesubmit'/>");
			$j("#errorDiv").addClass("error");
		} else {
			$j("#errorDiv").html("");
			$j("#errorDiv").removeClass("error");
		}
		
		return valid;
	}
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>