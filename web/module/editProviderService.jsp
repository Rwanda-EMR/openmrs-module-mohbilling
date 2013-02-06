<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Edit Provider Service" otherwise="/login.htm" redirect="/module/mohappointment/serviceProvider.list"/>

<script type="text/javascript">
	var $j = jQuery.noConflict();
	
	$j(document).ready(function(){
		$j("#btEdit").click(function(){
			if(validateFormFields()){
				if(confirm("<spring:message code='@MODULE_ID@.general.save.confirm'/>"))
					this.form.submit();
			}
		});
		
		$j("#btDelete").click(function(){
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

<div id="errorDiv"></div><br/>

<form action="editServiceProvider.form?edit=true" method="get" class="box">

	<table>
		<tr>
			<td><b><spring:message code="@MODULE_ID@.general.provider"/></b></td>
			<td><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
			<td><openmrs_tag:userField roles="Provider" formFieldName="provider" initialValue="${user}" /></td>
			<td valign="top"><span id="providerError"></span></td>
		</tr>
		<tr> 
			<td><b><spring:message code="@MODULE_ID@.general.service"/></b></td>
			<td><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
			<td><select name="service" id="service">
					<option value="">--</option>
					<c:forEach items="${services}" var="service">
						<option value="${service.serviceId}" <c:if test='${serviceProvider.service.serviceId==service.serviceId}'>selected='selected'</c:if>>${service.name}</option>
					</c:forEach>
				</select>
			</td>
			<td valign="top"><span id="serviceError"></span></td>
		</tr>
		<tr>
			<td><b><spring:message code="@MODULE_ID@.general.startdate"/></b></td>
			<td><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
			<td><input value="${serviceProvider.startDate}" type="text" name="startDate" id="startDate" size="11" onclick="showCalendar(this);"/></td>
			<td valign="top"><span id="startDateError"></span></td>
		</tr>
		<tr>
			<td></td>
			<td></td>
			<td><input type="button" id="btEdit" value="<spring:message code='@MODULE_ID@.general.edit'/>"></td>
			<td><input type="button" id="btDelete" value="<spring:message code='@MODULE_ID@.general.delete'/>"></td>
		</tr>
	</table>

</form>
