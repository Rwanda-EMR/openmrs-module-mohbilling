<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<openmrs:require privilege="Manage Recovery" otherwise="/login.htm" redirect="/module/@MODULE_ID@/manageRecovery.form" />

<script language="javascript" type="text/javascript">
	var $bill = jQuery.noConflict();
	
	<openmrs:formatDate date="${facilityService.startDate}" type="string"/>
	
</script>

<h2><spring:message code="@MODULE_ID@.billing.ManageRecovery"/></h2>

<openmrs:hasPrivilege privilege="Manage Recovery">
	<a href="recovery.list"><spring:message code="@MODULE_ID@.recovery.list" /></a>
</openmrs:hasPrivilege>

<br/><br/>

<b class="boxHeader"><spring:message code="@MODULE_ID@.recovery.followup"/></b>
<div class="box">
	<form action="manageRecovery.form" method="post" name="followup_recovery_form" id="followup_recovery_form">

	<input type="hidden" name="recoveryId" value="${recovery.recoveryId}"/>
	<!-- This part is for the primary information that composes a Recovery process (it's required!) -->
	<div class="box" id="primary_info">
		<table>
			<tr>
				<th><spring:message code="@MODULE_ID@.period.when"/></th>
				<th><spring:message code="@MODULE_ID@.insurance"/></th>
				<th><spring:message code="@MODULE_ID@.billing.thirdParty.label"/></th>
				<th><spring:message code="@MODULE_ID@.recovery.dueAmount"/></th>
				<th><spring:message code="@MODULE_ID@.recovery.status"/></th>
			</tr>
			<tr>
				<td>
					<table>
						<tr>
							<td><spring:message code="@MODULE_ID@.period.after"/></td>
							<td><input type="text" size="11" value="<openmrs:formatDate date="${recovery.startPeriod}" type="string"/>" name="startDate" onclick="showCalendar(this)" /></td>
						</tr>
						<tr>
							<td><spring:message code="@MODULE_ID@.period.before"/></td>
							<td><input type="text" size="11" value="<openmrs:formatDate date="${recovery.endPeriod}" type="string"/>" name="endDate" onclick="showCalendar(this)" /></td>
						</tr>
					</table>
				</td>
				<td>
					<select name="insurance">
						<option selected="selected" value="${recovery.insuranceId.insuranceId}">
							<c:choose>
								<c:when test="${insuranceId != null}"> ${recovery.insurance.name}</c:when>

								<c:otherwise>--select--</c:otherwise>
							</c:choose>
						</option>
						<c:forEach items="${allInsurances}" var="ins">
							<option value="${ins.insuranceId}" <c:if test='${recovery.insuranceId.insuranceId==ins.insuranceId}'>selected='selected'</c:if>>${ins.name}</option>
						</c:forEach>
					</select>
				</td>
				<td>
					<select name="thirdParty">
						<option selected="selected" value="${recovery.thirdParty.thirdPartyId}">
							<c:choose>
								<c:when test="${thirdPartyId != null}"> ${third.name}</c:when>

								<c:otherwise>--select--</c:otherwise>
							</c:choose>
						</option>
						<c:forEach items="${allThirdParties}" var="third">
							<option value="${third.thirdPartyId}" <c:if test='${recovery.thirdParty.thirdPartyId==third.thirdPartyId}'>selected='selected'</c:if>>${third.name}</option>
						</c:forEach>
					</select>
				</td>
				<td><span><b>${recovery.dueAmount}</b></span></td>
				<td><h3>${recovery.status}</h3></td>
			</tr>
		</table>
	</div>
	
	<br/>

	<!-- This part is for the secondary information : Submission and Verification Dates (2nd Step!) -->
	<!--  test="${empty recovery.submissionDate}" -->
		<b class="boxHeader" id="submission_verification_title"><spring:message code="@MODULE_ID@.recovery.submission"/></b>
		<div class="box" id="submission_verification_part">
			<div class="box" id="submission_part">
				<table width="100%">
					<tr>
						<td><spring:message code="@MODULE_ID@.recovery.submissionDate"/></td>
					</tr>
					<tr>
						<td><input type="text" size="11" value="<openmrs:formatDate date="${recovery.submissionDate}" type="string"/>" name="submissionDate" onclick="showCalendar(this)"/></td>
					</tr>
				</table>
			</div>
				
			<br/>
			
			<!-- test="${empty recovery.verificationDate}" -->
			
				<div class="box" id="verification_part">
					<table width="100%">
						<tr>
							<td><spring:message code="@MODULE_ID@.recovery.verificationDate"/></td>
						</tr>
						<tr>
							<td><input type="text" size="11" value="<openmrs:formatDate date="${recovery.verificationDate}" type="string"/>" name="verificationDate" onclick="showCalendar(this)"/></td>
						</tr>
					</table>			
				</div>
			<!-- <c:if test="${empty recovery.verificationDate}"></c:if> -->
		</div>
	 <!-- <c:if test="${empty recovery.submissionDate}" ></c:if> -->
	
	<br/>
	
	<!-- This part is for the payment information : the very last step... -->
	<!-- "${empty recovery.paymentDate}" -->

		<b class="boxHeader" id="payment_info_title"><spring:message code="@MODULE_ID@.recovery.paymentInfo"/></b>
		<div class="box" id="payment_info_part">
			<table width="100%">
				<tr>
					<th><spring:message code="@MODULE_ID@.recovery.amountPaid"/></th>
					<th><spring:message code="@MODULE_ID@.recovery.paymentDate"/></th>
					<th><spring:message code="@MODULE_ID@.recovery.partlyPayReason"/></th>
					<th><spring:message code="@MODULE_ID@.recovery.noPaymentReason"/></th>
					<th><spring:message code="@MODULE_ID@.recovery.observation"/></th>
				</tr>
				<tr>
					<td><input type="text" size="30" value="${recovery.paidAmount}" name="paidAmount"/></td>
					<td><input type="text" size="11" value="<openmrs:formatDate date="${recovery.paymentDate}" type="string"/>" name="paymentDate" onclick="showCalendar(this)"/></td>
					<td><textarea cols="30" rows="3" name="partlyPayReason">${recovery.partlyPayReason}</textarea></td>
					<td><textarea cols="30" rows="3" name="noPaymentReason">${recovery.noPaymentReason}</textarea></td>
					<td><textarea cols="30" rows="3" name="observation">${recovery.observation}</textarea></td>
				</tr>
			</table>
		</div>
	
	<!-- <c:if test="${!empty recovery.paymentDate}"></c:if> -->

		<br/>

		<input type="submit" name="recordInfo" value="<spring:message code='@MODULE_ID@.general.recordInfo'/>" />

	</form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>
