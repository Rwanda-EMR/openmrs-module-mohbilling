<ul id="menu">

	<openmrs:hasPrivilege privilege="Search Insurance Policy">
		<li class="<c:if test='<%= request.getRequestURI().contains("mohBillingInsurancePolicy")%>'> active</c:if>">
			<a href="insurancePolicySearch.form"><spring:message
					code="@MODULE_ID@.insurance.policy.manage" /></a>
		</li>
	</openmrs:hasPrivilege>
	
	<openmrs:hasPrivilege privilege="Manage Admission">
	<li class="<c:if test='<%= request.getRequestURI().contains("AdmissionSearch")%>'> active</c:if>">
	<a href="admissionSearch.form">Admission</a>
	</li>
	</openmrs:hasPrivilege>

	<openmrs:hasPrivilege privilege="Manage Patient Bill Calculations">
		<li class="<c:if test='<%= request.getRequestURI().contains("mohBillingBilling")%>'> active</c:if>">
			<!-- <a href="patientSearchBill.form"><spring:message code="@MODULE_ID@.billing.manage" /></a> -->
			<a href="patientSearchBill.form">Bill</a>
		</li>
	</openmrs:hasPrivilege>

	<openmrs:hasPrivilege privilege="Manage deposit">
	<li class="<c:if test='<%= request.getRequestURI().contains("SearchPatientAccount")%>'> active</c:if>">
	<a href="searchPatientAccount.form">Deposit</a>
	</li>
	</openmrs:hasPrivilege>

	<openmrs:hasPrivilege privilege="Manage Billing Reports">
		<li class="<c:if test='<%= request.getRequestURI().contains("Cohort")%>'> active</c:if>">
			<a href="cashierReport.form"><spring:message
					code="@MODULE_ID@.billing.report" /></a>
		</li>
	</openmrs:hasPrivilege>

	<openmrs:hasPrivilege privilege="Billing Configuration - View Billing Admin">
	<li class="<c:if test='<%= request.getRequestURI().contains("DepartmentList")%>'> active</c:if>">
	<a href="departments.list">Billing Admin</a>
	</li>
	</openmrs:hasPrivilege>
</ul>

<!-- Here I would like to set the totals at the bottom -->
<style>
.columnHeader {
	background: none repeat scroll 0 0 #E6E6E6;
	border: 1px solid #D3D3D3;
	color: #555555;
	cursor: pointer;
	font-size: 0.8em;
	font-weight: normal;
	margin: 0;
	padding: 3px 0 3px 5px;
	text-align: left;
}

.rowValue {
	font-size: 0.8em;
	font-weight: normal;
	margin: 0;
	padding: 5px;
	vertical-align: top;
	border-top: 1px solid cadetblue;
}

.rowAmountValue {
	font-size: 0.8em;
	font-weight: normal;
	margin: 0;
	padding: 5px;
	/* Here I would like to set the totals at the bottom*/
	vertical-align: bottom;
	border-top: 1px solid cadetblue;
	border-right: 1px solid cadetblue;
	border-left: 1px solid cadetblue;
}

.rowTotalValue {
	font-size: 0.8em;
	font-weight: normal;
	margin: 0;
	padding: 5px;
	vertical-align: top;
	border-top: 1px solid cadetblue;
	border-right: 1px solid cadetblue;
	border-bottom: 1px solid cadetblue;
	border-left: 1px solid cadetblue;
}

.even {
	background-color: whitesmoke;
}

.numbers {
	text-align: right;
	background-color: whitesmoke;
}

.amount {
	text-align: right;
	background-color: #C9DAFF;
	border: 2px solid #000000;
	font-weight: bold;
}

.right {
	text-align: right;
}

.center {
	text-align: center;
}

.searchRow:hover {
	background: #F0E68C;
	cursor: pointer;
}
</style>