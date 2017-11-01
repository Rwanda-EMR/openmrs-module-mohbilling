<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<openmrs:require privilege="Create Insurance Policy"
	otherwise="/login.htm"
	redirect="/module/@MODULE_ID@/insurancePolicy.form" />
<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<style type="text/css">
.greenbox {
	width: 16px;
	height: 16px;
	padding: 1px;
	margin: 4px;
	cursor: pointer;
	background-image: url("add.gif");
	background-repeat: no-repeat;
}

.redbox {
	width: 16px;
	height: 16px;
	margin: 3px;
	padding: 2px;
	cursor: pointer;
	font-weight: bold;
	font-size: medium;
	color: #EB99AD;
}
</style>

<script type="text/javascript">
	var $bill = jQuery.noConflict();

	var fieldGroupCount = 0;
	var count = 1;

	$bill(document).ready(function() {

		$bill('.beneficiariesC').hide();

		$bill('#rateDisplay').hide();

		$bill('#addBeneficiaryId').click(function() {
			var i = 1;
			for (i = 1; i <= 10; i++) {
				if ($bill('#beneficiaryI_' + i).is(':hidden')) {
					$bill('#beneficiaryI_' + i).show(200);
					break;
				}
			}
		});

		$bill('.redbox').click(function() {
			var deleteIdVar = this.id;
			var idArr = new Array();
			idArr = deleteIdVar.split("_");
			var idVar = idArr[1];
			var selectorElmt = 'beneficiaryI_' + idVar;
			$bill('#' + selectorElmt).hide(200);
		});

		$bill('#submitButtonId').click(function() {
			var i = 1;
			for (i = 1; i <= 10; i++) {
				if ($bill('#beneficiaryI_' + i).is(':hidden')) {
					$bill('#beneficiaryI_' + i).remove();
				}
			}
		});

		if ($bill('#hasThirdPart').is(':checked'))
			$bill('#rateDisplay').show();
		
		$bill('#hasThirdPart').change(function() {

			if ($bill('#hasThirdPart').is(':checked'))
				$bill('#rateDisplay').show();
			else
				$bill('#rateDisplay').hide();
		});

	});
</script>

<h2>
	<c:if test="${null ne param.patientId}">
		<spring:message code="mohbilling.insurance.policy.create" />
	</c:if>
	<c:if test="${null ne param.insurancePolicyId}">
		<spring:message code="mohbilling.insurance.policy.edit" />
	</c:if>
</h2>

<form action="insurancePolicy.form?save=true" method="post">
	<c:if test="${null ne param.insurancePolicyId}">
		<input type="hidden" value="${param.insurancePolicyId}" name="insurancePolicyId" />
	</c:if>
	<b class="boxHeader">Section I >> Owner</b>
	<div class="box">
		<table>
			<tr>
				<td>Patient Name</td>
				<td width="300px;"><openmrs_tag:patientField
						formFieldName="insurancePolicyOwner"
						initialValue="${beneficiaryId}" /></td>
			</tr>
		</table>
	</div>
	<br /> 
	 <b class="boxHeader">Section
		II >> Insurance</b>
	<div class="box">
		<table>
			<tr>
				<td>Insurance Name</td>
				<td><select name="insurancePolicyInsurance">
						<option value=""><b>Please select Insurance</b></option>
						<c:forEach items="${insurances}" var="insurance">
							<option value="${insurance.insuranceId}"
								<c:if test="${insurance.insuranceId==insurancePolicy.insurance.insuranceId}">selected='selected'</c:if>>${insurance.name}</option>
						</c:forEach>
				</select></td>
			</tr>
			<tr>
				<td>Insurance Card Number</td>
				<td><input type="text" name="insurancePolicyOwnerCardNumber"
					value="${insurancePolicy.insuranceCardNo}" size="30" /></td>
			</tr>
			<tr>
				<td>Coverage Startdate</td>
				<td><input
					value="<openmrs:formatDate date='${insurancePolicy.coverageStartDate}' type="string"/>"
					type="text" name="insurancePolicyCoverageStartDate" size="11"
					onclick="showCalendar(this);" autocomplete="off" /></td>
			</tr>
			<tr>
				<td>Coverage EndDate</td>
				<td><input
					value="<openmrs:formatDate date='${insurancePolicy.expirationDate}' type="string"/>"
					type="text" name="insurancePolicyExpirationDate" size="11"
					onclick="showCalendar(this);" autocomplete="off" />

					<jsp:useBean id="now" class="java.util.Date" />
                    <fmt:formatDate var="year1" value="${now}" pattern="yyyy-MM-dd" />
                    <fmt:formatDate var="year2" value="${insurancePolicy.expirationDate}" pattern="yyyy-MM-dd" />
                    <c:if test="${year2 lt year1}">
                    <font color="red"><b>Expired</b></font>
                    </c:if>

					</td>
			</tr>

			<tr>
				<td>Has Third part?</td>
				<td><input
					<c:if test="${insurancePolicy.thirdParty != null}">checked='checked'</c:if>
					type="checkbox" name="hasThirdPart" id="hasThirdPart" />
				<td>
					<div id="rateDisplay">
						<table>
							<tr>
								<td>Third Party</td>
								<td><select name="thirdParty">
										<option value="0">---</option>
										<c:forEach items="${thirdParties}" var="party">
											<option value="${party.thirdPartyId}"
												<c:if test="${party.thirdPartyId==insurancePolicy.thirdParty.thirdPartyId}">selected='selected'</c:if>>${party.name}</option>
										</c:forEach>
								</select></td>
							</tr>
						</table>
					</div>
			</tr>

		</table>
	</div>
	<br />




<!-- Adding Beneficiaries -->

<b class="boxHeader">Section III >> Ownership Info</b>
	<div class="box">
	<c:forEach items='${insurancePolicy.beneficiaries}' var='bn'>
                                        <c:set var="company" value="${bn.company}"/>
                                        <c:set var="ownerName" value="${bn.ownerName}"/>
                                        <c:set var="ownerCode" value="${bn.ownerCode}"/>
                                        <c:set var="level" value="${bn.level}"/>
     </c:forEach>
		<table>
					<tr>
                    				<td>Company Name</td>
                    				<td>
                    				<input type="text" name="company" size="30" value="${company}"/></td>
                    </tr>
			<tr>
            				<td>Head Household Name/Insurance Owner</td>
            				<td><input type="text" name="ownerName" size="30" value="${ownerName}"/></td>




            </tr>
            <tr>
                        				<td>Family/Affiliation code</td>
                        				<td><input type="text" name="ownerCode" size="30" value="${ownerCode}"/></td>


            </tr>
            <tr>
                        				<td>Category</td>
                        				<td><select name="level">
                        				<option value="">Select level please</option>
                        				<option value="${level}" Selected >${level}</option>
                        				<option value="1">1</option>
                        				<option value="2">2</option>
                        				<option value="3">3</option>
                        				<option value="4">4</option>
                        				</select></td>
             </tr>
		</table>
	</div>
	<br />


	<br /> <input type="submit" value="Save Insurance Policy"
		id="submitButtonId" />
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
