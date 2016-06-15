<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/@MODULE_ID@/taglibs/billingtag.tld" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!--  
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
-->

<!-- script to create a pop up windows for unpaid bills -->
<openmrs:htmlInclude file="/moduleResources/mohbilling/pop_style.css" /> 


<%@ include file="templates/mohBillingLocalHeader.jsp"%>


<%@ include file="templates/mohBillingDepositHeader.jsp"%>
<h2>Manage Deposit</h2>
<form action="deposit.form?save=true" method="post">
<b class="boxHeader">Section I >> Owner</b>
<div class="box">
	<table>
		<tr>
		 <td>Patient Name:</td>
		 <td width="300px;"><openmrs_tag:patientField
						formFieldName="insurancePolicyOwner"
						initialValue="${beneficiaryId}" /></td>
		</tr>
		</table>
	</div>
	<br /> 
<input type="hidden" name="insurancePolicyId"
		value="${insurancePolicy.insurancePolicyId}" />
<b class="boxHeader">Section II >> Deposit </b>
<div class="box">
		<table>
			<tr>
				<td>Amount</td>
				<td><input type="text" name="depositAmount"/></td>
			</tr>
			<tr>
				<td>Date</td>
				<td><input type="text" name="depositDate"/></td><td>(dd/mm/yyyy)</td>
			</tr>
			<tr>
				<td>Reason</td>
				<td>
				<!-- list to be set in Global Properties -->
				 <select name="depositReason">
					  <option value="opd">OPD</option>
					  <option value="ipd">IPD</option>
				</select> 
				</td>
			</tr>
			<tr>
			<td>Collector:</td>
			<td colspan="2"><openmrs_tag:userField formFieldName="depositCollector" initialValue="${authUser.userId}"/></td>
			</tr>
		</table>
		<br /> <input type="submit" value="Save Deposit" id="submitButtonId" />
</div>
</form>
<div>
<table>
<tr><th>#</th><th>Beneficiary</th><th>Amount</th><th>Deposit Reason</th><th>Collector</th></tr>
<c:forEach items="${depositList}" var="deposit" varStatus="status">	
		<tr>
			<td>${status.count}</td>
			<td>c></td>			
			<td>c</td>
			<td>c</td>	
			<td>f</td>	
			
		</tr>
	</c:forEach>
</table>
</div>
<%@ include file="/WEB-INF/template/footer.jsp"%>
