<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="Manage Services and Providers" otherwise="/login.htm" redirect="/module/mohappointment/service.form"/>

<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />

<%@ include file="templates/serviceProviderHeader.jsp"%>

<h2><spring:message code="@MODULE_ID@.appointment.service.form"/></h2>
<br/>

<form action="service.form?save=true" method="post" class="box" id="formService">
	<div id="errorDiv"></div><br/>
	<table>
		<tr>
			<td valign="top"><b><spring:message code="@MODULE_ID@.general.service.name"/></b></td>
			<td valign="top"><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
			<td valign="top"><input type="text" size="35" name="name" id="name"/></td>
			<td valign="top"><span id="nameError"></span></td>
		</tr>
		<tr>
			<td valign="top"><b><spring:message code="@MODULE_ID@.general.service.description"/></b></td>
			<td valign="top"><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
			<td valign="top"><textarea name="description" id="description" rows="3" cols="40"></textarea></td>
			<td valign="top"><span id="descriptionError"></span></td>
		</tr>
		<tr>
			<td valign="top"><b><spring:message code="@MODULE_ID@.general.service.concept"/></b></td>
			<td valign="top"><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
			<!-- <td valign="top"><openmrs_tag:conceptField formFieldName="serviceRelatedConcept"/></td> -->
			<td valign="top">
				<select name="serviceRelatedConcept">
					<option value="">--</option>
					<c:forEach items="${medicalServices}" var="service">
						<option value="${service.key}" >${service.value}</option>
					</c:forEach>
				</select>
			</td>
			<td valign="top"><span id="conceptError"></span></td>
		</tr>
		<tr>
			<td></td>
			<td>
				<c:if test="${param.serviceId ne null}">
					<input type="hidden" name="serviceId" value="${param.serviceId}"/>
				</c:if>
			</td>
			<td valign="top"><input type="button" id="btSave" value="<spring:message code='@MODULE_ID@.general.save'/>"></td>
			<td></td>
		</tr>
	</table>
</form>

<script>
	var $k= jQuery.noConflict();
	$k(document).ready(function(){
		$k("#btSave").click(function(){
			if(validateFormFields()){
				if(confirm("<spring:message code='@MODULE_ID@.general.save.confirm'/>"))
					this.form.submit();
			}
		});
	});

	function validateFormFields(){
		var valid=true;
		if(document.getElementsByName("serviceRelatedConcept")[0].value==''){
			$k("#conceptError").html("*");
			$k("#conceptError").addClass("error");
			valid=false;
		} else {
			$k("#conceptError").html("");
			$k("#conceptError").removeClass("error");
		}
		
		if($k("#name").val()==''){
			$k("#nameError").html("*");
			$k("#nameError").addClass("error");
			valid=false;
		} else {
			$k("#nameError").html("");
			$k("#nameError").removeClass("error");
		}

		if($k("#description").val()==''){
			$k("#descriptionError").html("*");
			$k("#descriptionError").addClass("error");
			valid=false;
		} else {
			$k("#descriptionError").html("");
			$k("#descriptionError").removeClass("error");
		}

		if(!valid){
			$k("#errorDiv").html("<spring:message code='@MODULE_ID@.general.fillbeforesubmit'/>");
			$k("#errorDiv").addClass("error");
		} else {
			$k("#errorDiv").html("");
			$k("#errorDiv").removeClass("error");
		}
		
		return valid;
	}
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>