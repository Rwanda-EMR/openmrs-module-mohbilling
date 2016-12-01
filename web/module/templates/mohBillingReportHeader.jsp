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
		

</ul>