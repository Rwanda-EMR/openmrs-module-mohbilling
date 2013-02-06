<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/style/listing.css" />

<div style="width: 640;">
	<table width="100%">
		<tr>
			<td rowspan="2" width="107px;"><img title="${patient.personName}" width=105px;" border="1px solid #BBBBBB;" src="<c:if test="${patient.gender=='F'}"><openmrs:contextPath/>/images/patient_F.gif</c:if><c:if test="${patient.gender=='M'}"><openmrs:contextPath/>/images/patient_M.gif</c:if>"/></td>
			<td colspan="2">
				<table width="100%">
					<tr>
						<td><h2 style="text-align: left; margin-left; 5px;">${patient.personName}</h2></td>
						<td><div style="text-align: right;">${patient.patientIdentifier.identifierType} : <b>${patient.patientIdentifier}</b></td>
					</tr>
					<tr>
						<td colspan="2"><hr/></td>
					</tr>
					<tr>
						<td>
							<table>
								<tr>
									<td style="text-align: right;">Gender</td>
									<td> <b>${(patient.gender=='F')?'Female':'Male'}</b></td>
								</tr>
								<tr>
									<td style="text-align: right;">Age</td>
									<td> <b>${patient.age} yrs</b> ( <openmrs:formatDate date="${patient.birthdate}" type="medium" /> )</td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
			</td>
			<td></td>
		</tr>
	</table>
	<br/>
	
	<table width="100%">
		<tr>
			<td>Last visit : <b><openmrs:formatDate date="${lastVisit}" type="medium" /></b></td>	
		</tr>
	</table>
	<br/>
	
	<table width="100%">
		<tr>
			<th class="columnHeader">Drug orders</th>
			<th class="columnHeader">Dose</th>
			<th class="columnHeader">Frequency</th>
			<th class="columnHeader">Start Date</th>
			<th class="columnHeader">Stop Date</th>
			<th class="columnHeader">Comments</th>
		</tr>
		<c:if test="${empty dOrders}"><tr><td colspan="6" class="rowValue" style="border-bottom: 1px solid cadetblue;">No <b>Orders</b> found !</td></tr></c:if>
		<c:forEach items="${dOrders}" var="order" varStatus="status">
			<tr>
				<td class="rowValue" style="border-bottom: 1px solid cadetblue;">${status.count}. ${order.drug.name}</td>	
				<td class="rowValue" style="border-bottom: 1px solid cadetblue;">${order.dose}${order.units}</td>
				<td class="rowValue" style="border-bottom: 1px solid cadetblue;">${order.frequency}</td>
				<td class="rowValue" style="border-bottom: 1px solid cadetblue;"><openmrs:formatDate date="${order.startDate}" type="medium" /></td>
				<td class="rowValue" style="border-bottom: 1px solid cadetblue;"><openmrs:formatDate date="${order.discontinuedDate}" type="medium" /></td>
				<td class="rowValue" style="border-bottom: 1px solid cadetblue;">${order.instructions}</td>
			</tr>
		</c:forEach>
		<!-- <tr>
			<td colspan="6"><hr/></td>	
		</tr> -->
	</table>
	<br/>
	
	<div style="display: inline;">
		<c:forEach items="${obsSummaryList}" var="obsSummary">
						
			<span style="width: 32%; padding-right: 4px; display: inline !important;">
				<table width="32%" style="display: inline;">
					<tr>
						<th colspan="2" class="columnHeader">${obsSummary.key}</th>	
					</tr>
					<tr>
						<th class="columnHeader">Date</th>
						<th class="columnHeader">Value</th>	
					</tr>
					<c:if test="${empty obsSummary.value}"><tr><td colspan="2" class="rowValue" style="border-bottom: 1px solid cadetblue;">No <b>${obsSummary.key}</b> found !</td></tr></c:if>
					<c:forEach items="${obsSummary.value}" var="simplifiedObs" varStatus="status">
						<tr>
							<td class="rowValue" style="border-bottom: 1px solid cadetblue;">${status.count}. <openmrs:formatDate date="${simplifiedObs.obsDatetime}" type="medium" /></td>	
							<td class="rowValue" style="border-bottom: 1px solid cadetblue;">${simplifiedObs.value}</td>
						</tr>
					</c:forEach>
					<!-- <tr>
						<td colspan="2"><hr/></td>	
					</tr> -->
				</table>
			</span>
			
		</c:forEach>
	</div>
</div>