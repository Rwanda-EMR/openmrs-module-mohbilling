<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />
<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/@MODULE_ID@/taglibs/billingtag.tld" %>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingBillHeader.jsp"%>

 <script type="text/javascript">
        $(function () {
            var total;
            var checked = $('input:checkbox').click(function (e) {
                calculateSum();
            });

            function calculateSum() {
                var $checked = $(':checkbox:checked');
                total = 0.0;
                $checked.each(function () {
                    total += parseFloat($(this).val());
                    
                });
                $('#tot').text("Your Payable  Is: " + total.toFixed(2));
            }
        });
    </script>

<h2>Patient Bill Payment</h2>

<%@ include file="templates/mohBillingInsurancePolicySummaryForm.jsp"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="insurancePolicy" value="${consommation.beneficiary.insurancePolicy}"/>
<c:set var="globalBill" value="${consommation.globalBill}"/>

<div style="text-align: right;"><a href="billing.form?insurancePolicyId=${insurancePolicy.insurancePolicyId}&ipCardNumber=${insurancePolicy.insuranceCardNo}&globalBillId=${globalBill.globalBillId}">Add consommation</a></div>

<br/>
<div class="box">
	<form action="patientBillPayment.form?consommationId=${consommation.consommationId}&ipCardNumber=${param.ipCardNumber}&save=true" method="post" id="formSaveBillPayment">
		<table width="99%">
			<tr>
				<th class="columnHeader"></th>
				<th class="columnHeader">Service</td>
				<th class="columnHeader center">Qty</td>
				<th class="columnHeader right">Unit Price (Rwf)</td>
				<th class="columnHeader right">Price (Rwf)</td>
				<th class="columnHeader right">Insurance : ${insurancePolicy.insurance.currentRate.rate} %</td>
				<th class="columnHeader right">Patient : ${100-insurancePolicy.insurance.currentRate.rate} %</td>
				<th></td>
			</tr>
			<c:if test="${empty consommation.billItems}"><tr><td colspan="7"><center>No consommation found !</center></td></tr></c:if>
			<c:set var="totalBillInsurance" value="0"/>
			<c:set var="totalBillPatient" value="0"/>
			<c:forEach items="${consommation.billItems}" var="billItem" varStatus="status">
			<c:set var="service" value="${billItem.service.facilityServicePrice}"/>
			<c:set var="fieldName" value="item-${consommation.consommationId}-${billItem.patientServiceBillId}"/>
				<tr>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}.</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${service.name}</td>
					<td class="rowValue center ${(status.count%2!=0)?'even':''}">${billItem.quantity}</td>
					<td class="rowValue right ${(status.count%2!=0)?'even':''}">${billItem.unitPrice}</td>
					<td class="rowValue right ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${billItem.unitPrice*billItem.quantity}" type="number" pattern="#.##"/></td>
					<td class="rowValue right ${(status.count%2!=0)?'even':''}">					  
						<fmt:formatNumber value="${((billItem.unitPrice*billItem.quantity)*insurancePolicy.insurance.currentRate.rate)/100}" type="number" pattern="#.##"/>
						<c:set var="totalBillInsurance" value="${totalBillInsurance+(((billItem.unitPrice*billItem.quantity)*insurancePolicy.insurance.currentRate.rate)/100)}"/>
					</td>
					<td class="rowValue right ${(status.count%2!=0)?'even':''}">							
						 <fmt:formatNumber value="${((billItem.unitPrice*billItem.quantity)*(100-insurancePolicy.insurance.currentRate.rate))/100}" type="number" pattern="#.##"/>
						 <c:set var="totalBillPatient" value="${totalBillPatient+(((billItem.unitPrice*billItem.quantity)*(100-insurancePolicy.insurance.currentRate.rate))/100)}"/>
					</td>							
					<td><input name="${fieldName}"	value="${(((billItem.unitPrice*billItem.quantity)*(100-insurancePolicy.insurance.currentRate.rate))/100)}" type="checkbox"></td>
				</tr>
			</c:forEach>		   
			<tr>			   
				<td colspan="4"> <p align="center" style="color: red; " id="tot"></p></td>
				<td><div style="text-align: right;"><b>Total : </b></div></td>
				<td><div class="amount"><fmt:formatNumber value="${totalBillInsurance}" type="number" pattern="#.##"/></div></td>
				<td><div class="amount"><fmt:formatNumber value="${totalBillPatient}" type="number" pattern="#.##"/></div></td>
			</tr>			
			<tr>
				<td colspan="7"><hr/></td>
			</tr>
			<tr style="font-size: 1em">
				<td><b>Collector</b></td>
				<td colspan="2"><openmrs_tag:userField formFieldName="billCollector" initialValue="${authUser.userId}"/></td>
				<td colspan="2"></td>
				<td><div style="text-align: right;"><b>Amount Paid</b></div></td>
				<td><div class="amount">${billingtag:amountPaidForPatientBill(consommation.consommationId)}</div></td>
			    
				
			</tr>
			<tr>
				<td><b>Received Date</b></td>
				<td colspan="2"><input type="text" autocomplete="off" name="dateBillReceived" size="11" onclick="showCalendar(this);" value="<openmrs:formatDate date='${todayDate}' type="string"/>"/></td>
				<td colspan="2"></td>
				<td><div style="text-align: right;"><b>Paid by Third Part</b></div></td>
				<td><div class="amount">${consommation.thirdPartyBill.amount}</div></td>				
			</tr>
			<tr>
				<td><b>Received Cash</b></td>
				<td colspan="2"><input type="text" autocomplete="off" name="receivedCash" size="11" class="numbers" value=""/></td>
				<td colspan="2"></td>
				<td><div style="text-align: right;"><b>Rest</b></div></td>
				<td><div class="amount">${billingtag:amountNotPaidForPatientBill(consommation.consommationId)}</div></td>				
			</tr>			
			<tr>
				<td><b>Pay with deposit</b></td>
				<td><input type="checkbox" name="depositPayment" value="depositPayment"> </td>
							
			</tr>
			<tr>
			  <td><b>Pay with cash</b></td>
				<td><input type="checkbox" name="cashPayment" value="cashPayment" > </td>
								
			</tr>			
			<tr>
				<td colspan="7"><hr/></td>
			</tr>
			
			<tr style="font-size: 1.2em">
				<openmrs:hasPrivilege privilege="Edit Bill">
					<td colspan="2"><input type="submit"  value="Confirm Bill" style="min-width: 200px;"/></td>
				</openmrs:hasPrivilege>
			 <td colspan="3"></td>
              <!-- 
			<c:if test="${billingtag:amountPaidForPatientBill(patientBill.patientBillId)>0 ||patientBill.beneficiary.insurancePolicy.insurance.currentRate.rate==100 || patientBill.beneficiary.insurancePolicy.thirdParty!=nil}">
			 -->		
			
			</tr>
		
			</c:if>
		</table>
	</form>
</div>
<br/>

<b class="boxHeader">Bills History</b>
<div class="box">
	
</div>


<script>
	function savePatientBillPayment(){
		if(confirm("Are you sure you want to save the payment ?")){
			document.getElementById("formSaveBillPayment").submit();
		}
	}

	//function printPatientBill(patientBillId){
		
	//}
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>