<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/headerMinimal.jsp" %>

<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:require privilege="Manage Refund Bill" otherwise="/login.htm" redirect="/module/@MODULE_ID@/refundBill.form" />

<script type="text/javascript">

	var $bill = jQuery.noConflict();

	var index=$bill("#index").val();

	function deleteRow(serviceId,rowNumber,patientBillId,cardNumber) {

		var serv = "#delete_"+rowNumber;
		var hrefValue = "service.list?deleteService=true&serviceId=" + rowNumber;
		var hrefValue = "refundBill.form?patientBillId="+patientBillId+"&billItemId="+serviceId+"&ipCardNumber="+cardNumber+"&removeIt=false";
        if(confirm("Are you sure you want to remove selected service?")){
		    try {
			    var table = document.getElementById("cartOfServices");
			    			    
			    var rowCount = table.rows.length;
				
			    for(var i=1; i<rowCount; i++) {
			        var row = table.rows[i];
			        
			        var hiddenElmnt=row.cells[5].childNodes[0];
			        if(null != hiddenElmnt && serviceId == hiddenElmnt.value) {
				    	table.deleteRow(i);
			            rowCount--;
			            i--;

			            $bill("#billableService_"+serviceId).removeClass("selectedService");
			    	    $bill("#billableService_"+serviceId).addClass("unselectedService");
			        }

			    }

			    calculateTheBill();
			    
		    }catch(e) {
		        alert(e);
		    }
    	}else{
    		$bill(serv).attr("href", hrefValue);
    	}
	}

	function calculateTheBill(){
		try {
			var bill=0.00;
			var j=0;
			var index=$bill("#index").val();
			
		    while(j<index){
			   	if(document.getElementById("servicePrice_"+j)!=null && document.getElementById("servicePrice_"+j)!="undefined"){
					var price=parseFloat(document.getElementById("servicePrice_"+j).value);
					bill+=(price*parseInt(document.getElementById("quantity_"+j).value));
			   	}
				
				j++;
			}

		    document.getElementById("pBill").innerHTML=bill.toFixed(2)+" RWF";
		    $bill("#totalAmount").val(bill.toFixed(2));
	    }catch(e) {
	        alert(e);
	    }
	}

	function  savePatientBill(){
		if(confirm("Are you sure you want to save?")){
			//set the number of services which has been clicked
			$bill("#numberOfServicesClicked").val(index);

			//submit the patient bill form
			document.getElementById("form_save_patient_bill").submit();
		}
	}

	function  cancelPatientBillCalculation(){
		if(confirm("Are you sure you want to Cancel?"))
			document.getElementById("form_cancel").submit();
	}


</script>

<style>

	.deleteBt{
		color: #FFFFFF;
		background-color: red;
		padding: 2px;
		cursor: pointer;
		-moz-border-radius: 2px; 
		border-right: 2px solid #dddddd;
		border-bottom: 2px solid #dddddd;
	}

</style>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<h2><spring:message code="@MODULE_ID@.billing.refund"/></h2>

<%@ include file="templates/mohBillingInsurancePolicySummaryForm.jsp"%>

<br/>

<input type="hidden" name="index" id="index" value="${index}"/>
<div class="box">
	<div>
		<b class="boxHeader">Patient Bill Services list</b>
		<div class="box">
			<form action="patientBillPayment.form?patientBillId=${param.patientBillId}&ipCardNumber=${param.ipCardNumber}&save=true" method="post" id="form_save_patient_bill">
				<div style="max-width: 99%; overflow: auto;">
					<table width="99%; !important;" id="cartOfServices">
						<tr>
							<td class="columnHeader" style="width: 5%;"></td>
							<td class="columnHeader" style="width: 60%;">Services</td>
							<td class="columnHeader" style="width: 10%;">Qty</td>
							<td class="columnHeader" style="width: 19%;">Price (Rwf)</td>
							<td class="columnHeader" style="width: 5%;"></td>
							<td style="width: 1%;"></td>
						</tr>
						<c:forEach items="${paidItems}" var="paidItem" varStatus="status">
							<tr>
								<td><span id="row_${status.count-1}">${status.count}.</span></td>
								<td>${paidItem.patientServiceBill.service.facilityServicePrice.name}</td>
								<td><input type="text" size="3" name="quantity_${status.count-1}" id="quantity_${status.count-1}" style="text-align: center;" value="${billItem.quantity}" onblur="calculateTheBill();"/></td>
								<td><span id="price_${status.count-1}"><b>${paidItem.patientServiceBill.unitPrice}</b></span><input type="hidden" name="servicePrice_${status.count-1}" id="servicePrice_${status.count-1}" value="${billItem.unitPrice}"/></td>
								<td>
									<a onclick="deleteRow(${paidItem.patientServiceBill.patientServiceBillId},${status.count-1},${patientBill.patientBillId},${patientBill.beneficiary.policyIdNumber});" id="delete_${status.count-1}" style="color: red;" href="refundBill.form?patientBillId=${patientBill.patientBillId}&billItemId=${billItem.patientServiceBillId}&ipCardNumber=${patientBill.beneficiary.policyIdNumber}&removeIt=true">
										<span title='Remove Service' class='deleteBt'><b>X</b></span>
									</a>
								</td>
								<td><input type="hidden" size="5" name="billableServiceId_${status.count-1}" id="billableServiceId_${status.count-1}" value="${paidItem.patientServiceBill.patientServiceBillId}"/></td>
							</tr>
						</c:forEach>
					</table>
					<br/>
					<div>
						<table width="99%">
							<tr>
								<td class="columnHeader" style="width: 49%; font-size: 14px;"><b>Total</b></td>
								<td class="columnHeader" style="width: 49%; text-align: right; font-size: 14px;"><input type="hidden" value="" name="totalAmount" id="totalAmount"/><b id="pBill">${patientBill.amount} RWF</b></td>
							</tr>
						</table>
					</div>
					<br/>
					
					<input type="hidden" name="numberOfServicesClicked" id="numberOfServicesClicked" value=""/>
				</div>
				
			</form>
			
			<table width="99%">
				<tr>
					<td style="width: 49%;">
						<input type="button" value="Refund" onclick="savePatientBill();"/>
					</td>
					<td style="width: 49%; text-align: right;">
						<form action="patientSearchBill.form" method="post" id="form_cancel">
							<input type="button" value="Cancel" onclick="cancelPatientBillCalculation();"/>
						</form>
					</td>
				</tr>
			</table>
			
		</div>
	</div>
	
	<div style="clear: both;"></div>
</div>
