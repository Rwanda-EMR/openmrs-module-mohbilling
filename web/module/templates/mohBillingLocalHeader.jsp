<ul id="menu">
		<li class="first">
			<a href="<openmrs:contextPath/>/admin/index.htm"><spring:message code="admin.title.short"/></a>
		</li>
		<openmrs:hasPrivilege privilege="View Facility service">
		<li class="<c:if test='<%= request.getRequestURI().contains("mohBillingFacilityService")%>'> active</c:if>">
			<a href="facilityService.list"><spring:message code="@MODULE_ID@.facility.service.manage"/></a>
		</li>
		</openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="View Current Insurances">
		<li class="<c:if test='<%= request.getRequestURI().contains("mohBillingInsurance") && !request.getRequestURI().contains("mohBillingInsurancePolicy")%>'> active</c:if>">
			<a href="insurance.list"><spring:message code="@MODULE_ID@.insurance.manage"/></a>
		</li>
		</openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="Search Insurance Policy"><li class="<c:if test='<%= request.getRequestURI().contains("mohBillingInsurancePolicy")%>'> active</c:if>">
			<a href="insurancePolicySearch.form"><spring:message code="@MODULE_ID@.insurance.policy.manage"/></a>
		</li>
		</openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="Manage Patient Bill Calculations">
		<li class="<c:if test='<%= request.getRequestURI().contains("mohBillingBilling")%>'> active</c:if>">
			<a href="patientSearchBill.form"><spring:message code="@MODULE_ID@.billing.manage"/></a>
		</li></openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="Check Patient Bill Payment">
		<li class="<c:if test='<%= request.getRequestURI().contains("mohBillingCheck")%>'> active</c:if>">
			<a href="checkPatientBillPayment.form"><spring:message code="@MODULE_ID@.billing.patient.bill.payment.check"/></a>
		</li></openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="Manage Recovery"><li class="<c:if test='<%= request.getRequestURI().contains("eRecover")%>'> active</c:if>">
			<a href="manageRecovery.form"><spring:message code="@MODULE_ID@.billing.ManageRecovery"/></a>
		</li>
		</openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="Manage Billing Reports"><li class="<c:if test='<%= request.getRequestURI().contains("Cohort")%>'> active</c:if>">
			<a href="cohort.form"><spring:message code="@MODULE_ID@.billing.report"/></a>
		</li>	</openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="Manage Billing Reports">
			<li>
				<a href="hmisReport.form">HMIS Reports</a>
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
	
	.numbers{
		text-align: right;
		background-color: whitesmoke;
	}
	
	.amount{
		text-align: right;
		background-color: #C9DAFF;
		border: 2px solid #000000;
		font-weight: bold;
	}
	
	.right{
		text-align: right;
	}
	
	.center{
		text-align: center;
	}

	.searchRow:hover {
		background: #F0E68C;
		cursor: pointer;
	}
</style>