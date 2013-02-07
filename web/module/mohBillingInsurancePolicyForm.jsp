<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />
<openmrs:require privilege="Create Insurance Policy" otherwise="/login.htm" redirect="/module/@MODULE_ID@/insurancePolicy.form" />
<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<style type="text/css">
	.greenbox
	{
		width:16px; 
		height:16px;
		padding: 1px; 
		margin:4px;
		cursor: pointer;
		background-image: url("add.gif");
		background-repeat: no-repeat;
	}
	
	.redbox
	{
		width:16px; 
		height:16px;
		margin:3px;
		padding:2px;
		cursor: pointer;
		font-weight: bold;
		font-size: medium;
		color: #EB99AD;
	}
</style>

<script type="text/javascript">
	var $j = jQuery.noConflict();
	
	var fieldGroupCount = 0;
	var count = 1;
	$j(document).ready(function() {
		$j('.beneficiariesC').hide();
		$j('#addBeneficiaryId').click(function() {
			var i = 1;
			for(i = 1; i<=10; i++) {
				if ( $j('#beneficiaryI_'+i).is(':hidden') ) {
					   $j('#beneficiaryI_'+i).show(200);
					   break;
				}
			}
		});
	
		$j('.redbox').click(function() {
			var deleteIdVar = this.id;
			var idArr = new Array();
			idArr = deleteIdVar.split("_");
			var idVar = idArr[1];
			var selectorElmt = 'beneficiaryI_'+idVar;
			$j('#'+selectorElmt).hide(200);
		});
	
		$j('#submitButtonId').click(function() {
			var i = 1;
			for(i = 1; i <= 10; i++) {
				if($j('#beneficiaryI_'+i).is(':hidden')) {
					$j('#beneficiaryI_'+i).remove();
				}
			}
		});
	
	});

</script>

<h2>
	<c:if test="${null ne param.patientId}"><spring:message code="@MODULE_ID@.insurance.policy.create" /></c:if>
	<c:if test="${null ne param.insurancePolicyId}"><spring:message code="@MODULE_ID@.insurance.policy.edit" /></c:if>
</h2>

<form action="insurancePolicy.form?save=true" method="post">
	
	<b class="boxHeader">Section I >> Owner</b>
	<div class="box">
		<table>
			<tr>
				<td>Patient Name</td>
				<td width="300px;"><openmrs_tag:patientField formFieldName="insurancePolicyOwner" initialValue="${beneficiaryId}"/></td>
			</tr>
		</table>
	</div>
	<br />
	
	<b class="boxHeader">Section II >> Insurance</b>
	<div class="box">
		<table>
			<tr>
				<td>Insurance Name</td>
				<td><select name="insurancePolicyInsurance">
					<c:forEach items="${insurances}" var="insurance">
						<option value="${insurance.insuranceId}" <c:if test="${insurance.insuranceId==insurancePolicy.insurance.insuranceId}">selected='selected'</c:if>>${insurance.name}</option>
					</c:forEach>
				</select></td>
			</tr>
			<tr>
				<td>Insurance Card Number</td>
				<td><input type="text" name="insurancePolicyOwnerCardNumber" value="${insurancePolicy.insuranceCardNo}" size="30" /></td>
			</tr>
			<tr>
				<td>Coverage Startdate</td>
				<td><input value="<openmrs:formatDate date='${insurancePolicy.coverageStartDate}' type="string"/>" type="text" name="insurancePolicyCoverageStartDate" size="11" onclick="showCalendar(this);" autocomplete="off"/></td>
			</tr>
			<tr>
				<td>Expiration Date</td>
				<td><input value="<openmrs:formatDate date='${insurancePolicy.expirationDate}' type="string"/>" type="text" name="insurancePolicyExpirationDate" size="11" onclick="showCalendar(this);" autocomplete="off"/></td>
			</tr>
		</table>
	</div>
	<br />
	
	<!-- Adding Beneficiaries -->
	<b class="boxHeader">Section III >> Other Beneficiaries</b>
	<div class="box">
		<div id="beneficiaryDiv">
			<table>
				<c:forEach items="1,2,3,4,5,6,7,8,9,10" var="j">
					<tr class="beneficiariesC" id="beneficiaryI_${j}">
						<td>Beneficiary ${j}</td>
						<td width="300px"><openmrs_tag:patientField formFieldName="insurancePolicyBeneficiary_${j}" initialValue=""/></td>
						<td>Policy Id No</td>
						<td><input type="text" value="" name="insurancePolicyBeneficiaryCardNumber_${j}" /></td>
						<td>
							<span class="redbox" id="delete_${j}">
								<img src="${pageContext.request.contextPath}/images/delete.gif" />
							</span>
						</td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<p id="addBeneficiaryId" class="greenbox"><img src="${pageContext.request.contextPath}/images/add.gif" /></p>
	</div>
	<!-- /Adding Beneficiaries -->
	
	<!-- updating Beneficiaries -->
	<b class="boxHeader">Section III >> Other Beneficiaries</b>
	<div class="box">
		<div id="beneficiaryDiv">
			<table>
				<c:forEach items="${insurancePolicy.beneficiaries}" var="beneficiary">
					<tr class="beneficiariesC" id="beneficiaryI_">
						<td>Beneficiary</td>
						<td width="300px"><openmrs_tag:patientField formFieldName="insurancePolicyBeneficiary_" initialValue="${beneficiary.patient.patientId}"/></td>
						<td>Policy Id No</td>
						<td><input type="text" value="${beneficiary.policyIdNumber}" name="insurancePolicyBeneficiaryCardNumber_" /></td>
						<td>
							<span class="redbox" id="delete_">
								<img src="${pageContext.request.contextPath}/images/delete.gif" />
							</span>
						</td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<p id="addBeneficiaryId" class="greenbox"><img src="${pageContext.request.contextPath}/images/add.gif" /></p>
	</div>
	<!-- /updating Beneficiaries -->

	<br/>
	<input type="submit" value="Save Insurance Policy" id="submitButtonId" />
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
