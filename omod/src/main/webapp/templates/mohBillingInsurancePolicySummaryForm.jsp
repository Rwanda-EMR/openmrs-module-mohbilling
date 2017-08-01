<div class="box">
	
	<div style="float: left; width: 65%">
		<div style="float: left; width: 49%">
			<b class="${(valid)?'boxHeader':'boxHeaderRed'}">Insurance Owner Information</b>
			<div class="${(valid)?'box':'boxRed'}">
				<table width="100%">
					<tr>
						<td>Names</td>
						<td> : <b>${insurancePolicy.owner.personName}</b></td>
						<td>Gender</td>
						<td> : <b>${(insurancePolicy.owner.gender=='F')?'Female':'Male'}</b></td>
					</tr>
					<tr>
						<td>Birthdate</td>
						<td> : <b><openmrs:formatDate date="${insurancePolicy.owner.birthdate}" type="medium" /></b></td>
						<td>Age</td>
						<td> : <b>${insurancePolicy.owner.age}</b> yrs</td>
					</tr>
					<tr>
						<td>Validity</td>
						<td colspan="3"> : <b><openmrs:formatDate date="${insurancePolicy.coverageStartDate}" type="medium" /></b> / 
								<b><openmrs:formatDate date="${insurancePolicy.expirationDate}" type="medium" /></b></td>
					</tr>
				</table>
			</div>
		</div>
		
		<div style="float: right; width: 49%">
			<b class="${(valid)?'boxHeader':'boxHeaderRed'}">Beneficiary Information</b>
			<div class="${(valid)?'box':'boxRed'}">
				<table width="100%">
					<tr>
						<td>Names</td>
						<td> : <b>${beneficiary.patient.personName}</b></td>
						<td>Gender</td>
						<td> : <b>${(beneficiary.patient.gender=='F')?'Female':'Male'}</b></td>
					</tr>
					<tr>
						<td>Birthdate</td>
						<td> : <b><openmrs:formatDate date="${beneficiary.patient.birthdate}" type="medium" /></b></td>
						<td>Age</td>
						<td> : <b>${beneficiary.patient.age}</b> yrs</td>
					</tr>
					<tr>
						<td>Policy Number</td>
						<td colspan="3"> : <b>${beneficiary.policyIdNumber}</b></td>
					</tr>
				</table>
			</div>
		</div>
			
		<div style="clear: both;"></div>
		
	</div>
		
	<div style="float: right; width: 34%">
		<b class="${(valid)?'boxHeader':'boxHeaderRed'}">Insurance Company</b>
		<div class="${(valid)?'box':'boxRed'}">
			<table width="100%">
				<tr>
					<td>Insurance</td>
					<td> : <b>${insurancePolicy.insurance.name}</b></td>
					<td>Rate</td>
					<td> : <b>${insurancePolicy.insurance.currentRate.rate}</b>%</td>
				</tr>
				<tr>
					<td>Flat Fee</td>
					<td> : <b>${insurancePolicy.insurance.currentRate.flatFee}</b> Rwf</td>
					<td></td>
					<td><b></b></td>
				</tr>
			</table>
		</div>
	</div>
	
	<div style="clear: both;"></div>
	
</div>