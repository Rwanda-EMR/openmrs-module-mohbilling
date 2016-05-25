<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-1.3.2.js" />
<openmrs:require privilege="Manage Third Party" otherwise="/login.htm" redirect="/module/mohbilling/thirdParty.form" />

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<openmrs:require privilege="Manage Recovery" otherwise="/login.htm" redirect="/module/mohbilling/recovery.list" />

<script type="text/javascript">
		var $bill = jQuery.noConflict();
		
		// Checking whether the User wants to delete the data
		function submitData(servId){
			var serv = "#deleteCtrl_"+servId
			var hrefValue = "processRecovery.form?deleteRecovery=true&deleteRecoveryId=" + servId;
			
			if(confirm("<spring:message code='mohbilling.general.delete.confirm'/>"))
				$bill(serv).attr("href", hrefValue);
		}

		$bill(document).ready(function(){
			
		});
</script>

<h2><spring:message code="mohbilling.billing.ManageRecovery"/></h2>

<openmrs:hasPrivilege privilege="Manage Recovery">
	<a href="manageRecovery.form"><spring:message code="mohbilling.billing.ManageRecovery" /></a>
</openmrs:hasPrivilege>

<br/><br/>

<b class="boxHeader"><spring:message code="mohbilling.recovery.list"/></b>
		
<!-- If no Recovery History don't display anything! -->
<c:if test="${!empty recoveryList}">

	<div class="box">
		<table width="99%">
			<tr>
				<th class="columnHeader">#</th>
				<th class="columnHeader"><spring:message code="mohbilling.recovery.period"/></th>
				<th class="columnHeader"><spring:message code="mohbilling.insurance"/></th>
				<th class="columnHeader"><spring:message code="mohbilling.recovery.dueAmount"/></th>
				<th class="columnHeader"><spring:message code="mohbilling.recovery.submission"/></th>
				<th class="columnHeader"><spring:message code="mohbilling.recovery.paymentInfo"/></th>
				<th class="columnHeader"><spring:message code="mohbilling.general.reasons"/></th>
				<th class="columnHeader"><spring:message code="mohbilling.recovery.observation"/></th>
				<th class="columnHeader"><spring:message code="mohbilling.general.status"/></th>
				<th class="columnHeader"></th>
			</tr>
			
			<c:forEach items="${recoveryList}" var="recovery" varStatus="status">
				<tr>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">
						<table>
							<tr>
								<td><b><spring:message code="mohbilling.period.from"/></b></td>
								<td><openmrs:formatDate date="${recovery.startPeriod}" type="string"/></td>
							</tr>
							<tr>
								<td><b><spring:message code="mohbilling.period.to"/></b></td>
								<td><openmrs:formatDate date="${recovery.endPeriod}" type="string"/></td>
							</tr>
						</table>
					</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">
						<table>
							<tr>
								<td class="columnHeader"><spring:message code="mohbilling.general.company"/></td>
							</tr>
							<tr>
								<td>${recovery.insuranceId.name}</td>
							</tr>
							<tr>
								<td class="columnHeader"><spring:message code="mohbilling.billing.thirdParty.label"/></td>
							</tr>
							<tr>
								<td>${recovery.thirdParty.name}</td>
							</tr>
						</table>
					</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${recovery.dueAmount}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">
						<table>
							<tr>
								<td class="columnHeader"><spring:message code="mohbilling.recovery.submissionDate"/></td>
							</tr>
							<tr>
								<td><openmrs:formatDate date="${recovery.submissionDate}" type="string"/></td>
							</tr>
							<tr>
								<td class="columnHeader"><spring:message code="mohbilling.recovery.verificationDate"/></td>
							</tr>
							<tr>
								<td><openmrs:formatDate date="${recovery.verificationDate}" type="string"/></td>
							</tr>
						</table>
					</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">
						<table>
							<tr>
								<td class="columnHeader"><spring:message code="mohbilling.recovery.amountPaid"/></td>
							</tr>
							<tr>
								<td>${recovery.paidAmount}</td>
							</tr>
							<tr>
								<td class="columnHeader"><spring:message code="mohbilling.recovery.paymentDate"/></td>
							</tr>
							<tr>
								<td><openmrs:formatDate date="${recovery.paymentDate}" type="string"/></td>
							</tr>
						</table>
					</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">
						<table>
							<tr>
								<td class="columnHeader"><spring:message code="mohbilling.recovery.partlyPay"/></td>
							</tr>
							<tr>
								<td>${recovery.partlyPayReason}</td>
							</tr>
							<tr>
								<td class="columnHeader"><spring:message code="mohbilling.recovery.noPayment"/></td>
							</tr>
							<tr>
								<td>${recovery.noPaymentReason}</td>
							</tr>
						</table>
					</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${recovery.observation}</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${recovery.status}</td>
					
					<td class="rowValue ${status.count%2!=0?'even':''}">
						<a href="processRecovery.form?editRecovery=true&editRecoveryId=${recovery.recoveryId}">
							<spring:message code='mohbilling.general.edit'/>
						</a> | <a id="deleteCtrl_${recovery.recoveryId}" onclick="submitData(${recovery.recoveryId});" href="#">
							<spring:message code='mohbilling.general.delete'/>
						</a>
					</td>
				</tr>
			</c:forEach>
		</table>
	</div>

</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>