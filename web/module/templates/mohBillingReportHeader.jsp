<ul id="menu">
        <openmrs:hasPrivilege privilege="Billing Reports - View Find Bills">
		<li class="<c:if test='<%= request.getRequestURI().contains("Cohort")%>'> active</c:if>">
			<a href="cohort.form"><spring:message code="@MODULE_ID@.billing.cohort"/></a>
		</li>
		</openmrs:hasPrivilege>
		
		<openmrs:hasPrivilege privilege="Billing Reports - View Payments">
	    <li>
			<a href="received.form"><spring:message code="@MODULE_ID@.billing.received"/></a>
		</li>
		</openmrs:hasPrivilege>
		
		 <openmrs:hasPrivilege privilege="Billing Reports - View Revenue">
		 <li>
			<a href="recettes.form"><spring:message code="@MODULE_ID@.billing.revenue"/></a>
		</li>
		</openmrs:hasPrivilege>
		
		 <openmrs:hasPrivilege privilege="Billing Reports - View Invoice">
		<li>
			<a href="invoice.form"><spring:message code="@MODULE_ID@.billing.invoice"/></a>
		</li>
		</openmrs:hasPrivilege>
		
		 <openmrs:hasPrivilege privilege="Billing Reports - View Releve">
		<li>
			<a href="facture.form"><spring:message code="@MODULE_ID@.billing.facture"/></a>
		</li>
		</openmrs:hasPrivilege>
		<li>
			<a href="refundBillReport.form">Refunding report</a>
		</li>
		
		<!-- 
		<li>
			<a href="hmisReport.form">HMIS Reports</a>
		</li>
		 -->
</ul>