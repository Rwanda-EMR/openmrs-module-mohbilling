<ul id="menu">

		<openmrs:hasPrivilege privilege="Search Insurance Policy"><li class="<c:if test='<%= request.getRequestURI().contains("mohBillingInsurancePolicy")%>'> active</c:if>">
			<a href="insurancePolicySearch.form"><spring:message code="@MODULE_ID@.insurance.policy.manage"/></a>
		</li>
		</openmrs:hasPrivilege>
		<li><a href="admission.form">Admission</a></li>
		
		<openmrs:hasPrivilege privilege="Manage Patient Bill Calculations">
		<li class="<c:if test='<%= request.getRequestURI().contains("mohBillingBilling")%>'> active</c:if>">
			<a href="patientSearchBill.form"><spring:message code="@MODULE_ID@.billing.manage"/></a>
		</li></openmrs:hasPrivilege>
		
		<li><a href="deposit.form">Deposit</a></li>
		
		<openmrs:hasPrivilege privilege="Manage Billing Reports"><li class="<c:if test='<%= request.getRequestURI().contains("Cohort")%>'> active</c:if>">
			<a href="cohort.form"><spring:message code="@MODULE_ID@.billing.report"/></a>
		</li>	</openmrs:hasPrivilege>
	
	<li><a href="billingAdmin.form">Billing Admin</a></li>
		
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