<form action="advancedSearch.form?page=1" method="post">
	<div class="searchParameterBox box">
		<div style="float: left; width: 45px;">
			<table>
				<tr>
					<td><b><spring:message code="@MODULE_ID@.general.provider"/></b></td>
					<td><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
					<td><openmrs_tag:userField roles="Provider" formFieldName="provider" initialValue="${param.provider}" /></td>
				</tr>
				<tr>
					<td><b><spring:message code="@MODULE_ID@.general.location"/></b></td>
					<td><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
					<td><openmrs_tag:locationField formFieldName="location" initialValue="${param.location}" /></td>
				</tr>
				<tr>
					<td><b><spring:message code="@MODULE_ID@.general.period"/></b></td>
					<td></td>
					<td>
						<table>
							<tr>
								<td><b><spring:message code="@MODULE_ID@.general.from"/></b></td>
								<td><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
								<td><input value="${param.dateFrom}" type="text" name="dateFrom" size="11" onclick="showCalendar(this);"/></td>
								<td>&nbsp;&nbsp;&nbsp;<b><spring:message code="@MODULE_ID@.general.to"/></b></td>
								<td><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
								<td><input value="${param.dateTo}" type="text" name="dateTo" size="11" onclick="showCalendar(this);"/></td>
							</tr>					
						</table>
					</td>
				</tr>
				<tr>
					<td><b><spring:message code="@MODULE_ID@.general.patient"/></b></td>
					<td><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
					<td><openmrs_tag:patientField formFieldName="patient" initialValue="${param.patient}" /></td>
				</tr>
			</table>
		</div>
		
		<div style="float: right; width: 45%;">
			<table>
				<tr>
					<td><b><spring:message code="@MODULE_ID@.general.reasonofappointment"/></b></td>
					<td><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
					<td><select name="reasonofappointment">
							<option value="">--</option>
							<c:forEach items="${reasonForAppointmentOptions}" var="appointmentReason">
								<option value="${appointmentReason.key}" <c:if test="${appointmentReason.key==param.reasonofappointment}">selected='selected'</c:if>>${appointmentReason.value}</option>
							</c:forEach>
						</select>
					</td>
				</tr>
				<tr>
					<td><b><spring:message code="@MODULE_ID@.general.stateofappointment"/></b></td>
					<td><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
					<td><select name="stateofappointment">
							<option value="">--</option>
							<c:forEach items="${appointmentStates}" var="appState">
								<option value="${appState.appointmentStateId}" <c:if test="${appState.appointmentStateId==param.stateofappointment}">selected='selected'</c:if>>${appState.description}</option>
							</c:forEach>
						</select>
					</td>
				</tr>
				<tr>
					<td><b><spring:message code="@MODULE_ID@.general.clinicalareatosee"/></b></td>
					<td><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
					<td><select name="clinicalareatosee">
							<option value="">--</option>
							<c:forEach items="${areasToSee}" var="area">
								<option value="${area.serviceId}" <c:if test="${area.serviceId==param.clinicalareatosee}">selected='selected'</c:if>>${area.name}</option>
							</c:forEach>
						</select>
					</td>
				</tr>
				<tr>
					<td><b><spring:message code="@MODULE_ID@.general.appointmentoverduemorethanxdays"/></b></td>
					<td><img border="0" src="<openmrs:contextPath/>/moduleResources/@MODULE_ID@/images/help.gif" title="?"/></td>
					<td><input type="text" name="appointmentoverduemorethanxdays" size="5px" value="${param.appointmentoverduemorethanxdays}"/></td>
				</tr>
			</table>
		</div>
		
		<div style="clear: both;"></div>
	</div>
	
	<div class="divBox">
		<input type="submit" value="<spring:message code='@MODULE_ID@.search'/>"/>
	</div>
	
</form>