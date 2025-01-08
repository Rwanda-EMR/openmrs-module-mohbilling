<ul id="menu">

		
		<li class="<c:if test='<%= request.getRequestURI().contains("Cohort")%>'> active</c:if>">
			<a href="cohort.form">Find Bills</a>
		</li>
		 
		
		<openmrs:hasPrivilege privilege="Billing Report - View Cashier Report">
		<li class="<c:if test='<%= request.getRequestURI().contains("Cashier")%>'> active</c:if>">
			<a href="cashierReport.form">Cashier Report</a>
		</li>
		</openmrs:hasPrivilege>

		<openmrs:hasPrivilege privilege="Billing Report - View Deposit Report">
		<li class="<c:if test='<%= request.getRequestURI().contains("DepositReport")%>'> active</c:if>">
			<a href="depositReport.form">Deposits</a>
		</li>
		</openmrs:hasPrivilege>
		
		<openmrs:hasPrivilege privilege="Billing Report - View Service Report">
	    <li class="<c:if test='<%= request.getRequestURI().contains("RevenueReport")%>'> active</c:if>">
			<a href="serviceRevenueReport.form">Service Report</a>
		</li>
		</openmrs:hasPrivilege>
		
		<openmrs:hasPrivilege privilege="Billing Report - View Refund Report">
		<li class="<c:if test='<%= request.getRequestURI().contains("RefundReport")%>'> active</c:if>">
			<a href="refundBillReport.form">Refund Report</a>
		</li>
		</openmrs:hasPrivilege>
		
		 <openmrs:hasPrivilege privilege="Billing Report - View Insurance Report">
		 <li class="<c:if test='<%= request.getRequestURI().contains("InsuranceReport")%>'> active</c:if>">
			<a href="insuranceReport.form">Insurance Report</a>
		</li>
		</openmrs:hasPrivilege>
		
		 <openmrs:hasPrivilege privilege="Billing Report - View Third Party Report">
		<li class="<c:if test='<%= request.getRequestURI().contains("ThirdPartyReport")%>'> active</c:if>">
			<a href="thirdPartyReport.form">Third Party Report</a>
		</li>
		</openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="View Provider DCP Report">
		<li class="<c:if test='<%= request.getRequestURI().contains("ProviderDCPReport")%>'> active</c:if>">
			<a href="providerDCPReport.form">DCP Provider Report</a>
		</li>
	</openmrs:hasPrivilege>

</ul>

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
	.boxHeader {
      background-color: #51A351;
    }
    .box {
      border: 1px solid #51A351;
    }


</style>