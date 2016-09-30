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
            $('.items').click(function (e) {
            	var a = $('.items').filter(":checked").length;
         	    if (a == 0) 
         	        $('#refundReasonTitle').hide();
         	    else 
         	    	 $('#refundReasonTitle').show();
            	calculateTotalRefundAmount();        
            });

            function calculateTotalRefundAmount() {
                total = 0.0;
                $('.items:checked').each(function () {
                    var itemId = $(this).val();
                	$("#refundReason-"+itemId).show();
                    var patientRate = document.getElementById("patientRate").value;
                   
                    var refQty = $("#quantity-"+itemId).val();
                    var up = $("#up-"+itemId).val();
                    var price= refQty*up*patientRate;
                    //price of each
                  //  $('#price-'+itemId).val(price.toFixed(2));
                    
                    total+=price;
                    
                });
                
                $('#tot').text("Your refundable  is: " + total.toFixed(2));
            }
        }); 
</script>  

<script type="text/javascript">
$(function(){
    $('.items').click(function() {
    	 var itemId = $(this).val();
        if($(this).is(':checked'))
        	$("#refundReason-"+itemId).show();
        else
        	$("#refundReason-"+itemId).hide();
    });
});
</script>

<style type="text/css">
 .hide{
 display: none;
 }
</style>   

 
<h2>Payment Refund Form</h2>

<%@ include file="templates/mohBillingInsurancePolicySummaryForm.jsp"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="insurancePolicy" value="${consommation.beneficiary.insurancePolicy}"/>
<c:set var="globalBill" value="${consommation.globalBill}"/>

<div style="text-align: right;"><a href="billing.form?insurancePolicyId=${insurancePolicy.insurancePolicyId}&ipCardNumber=${insurancePolicy.insuranceCardNo}&globalBillId=${globalBill.globalBillId}">Add consommation</a></div>
    

<div class="box">
	<form action="paymentRefund.form?paymentId=${payment.billPaymentId}&ipCardNumber=${insurancePolicy.insuranceCardNo}&save=true" method="post" id="formSaveBillPayment">
		<table width="99%">
			<tr>
				<th class="columnHeader"></th>
				<th class="columnHeader">Service</td>
				<th class="columnHeader center">Qty</td>
				<th class="columnHeader center">PaidQty</td>
				<th class="columnHeader center">RefQty</td>
				<th class="columnHeader right">Unit Price (Rwf)</td>
				<th class="columnHeader right">Price (Rwf)</td>
				<th class="columnHeader right">Patient : ${100-insurancePolicy.insurance.currentRate.rate} %</td>
				<th></th>
				<th class="columnHeader left" id="refundReasonTitle" style="display: none;">Reason</th>
			</tr>
			<c:if test="${empty paidItems}"><tr><td colspan="9"><center>No paid Services Bill !</center></td></tr></c:if>
			<c:set var="totalBillInsurance" value="0"/>
			<c:set var="totalBillPatient" value="0"/>
			<c:forEach items="${paidItems}" var="paidItem" varStatus="status">
			<c:set var="service" value="${paidItem.billItem.service.facilityServicePrice}"/>
			<c:set var="billItem" value="${paidItem.billItem}"/>
			
			<c:set var="fieldName" value="item-${payment.billPaymentId}-${paidItem.paidServiceBillId}"/>
				<tr>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}.</td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}">${service.name}</td>
					<td class="rowValue center ${(status.count%2!=0)?'even':''}">${billItem.quantity}</td>
				    <td class="rowValue center ${(status.count%2!=0)?'even':''}">${paidItem.paidQty}</td>
					<td><input type="text" size="3" name="quantity-${paidItem.paidServiceBillId}" id="quantity-${paidItem.paidServiceBillId}" style="text-align: center;" value="1"/></td>
				
				    <input type="hidden" id="patientRate" value="${(100-insurancePolicy.insurance.currentRate.rate)/100}"/>
					<td class="rowValue center ${(status.count%2!=0)?'even':''}"><input type="text" value="${billItem.unitPrice }" name="up-${paidItem.paidServiceBillId}" id="up-${paidItem.paidServiceBillId}" disabled="disabled" style="border: none;"/></td>
					<td class="rowValue center ${(status.count%2!=0)?'even':''}"><input type="text" value="${billItem.unitPrice*billItem.quantity}" name="price-${paidItem.paidServiceBillId}" id="price-${paidItem.paidServiceBillId}" disabled="disabled" style="border: none;"/></td>

					<td class="rowValue right ${(status.count%2!=0)?'even':''}">							
						 <fmt:formatNumber value="${((billItem.unitPrice*billItem.quantity)*(100-insurancePolicy.insurance.currentRate.rate))/100}" type="number" pattern="#.##"/>
						 <c:set var="totalBillPatient" value="${totalBillPatient+(((billItem.unitPrice*billItem.quantity)*(100-insurancePolicy.insurance.currentRate.rate))/100)}"/>
					</td>							
					<td><input name="${fieldName}"	value="${paidItem.paidServiceBillId}" type="checkbox" class="items"></td>
					<td><textarea name="refundReason-${paidItem.paidServiceBillId}" id="refundReason-${paidItem.paidServiceBillId}" rows="1" cols="20" value="" style="display: none;"></textarea></td>	
				</tr>
			</c:forEach>		   
			<tr>			   
				<td colspan="6"> <p align="center" style="color: red; " id="tot"></p></td>
				<td><div style="text-align: right;"><b>Total : </b></div></td>
				<td><div class="amount"><fmt:formatNumber value="${totalBillPatient}" type="number" pattern="#.##"/></div></td>
			</tr>			
			<tr>
				<td colspan="11"><hr/></td>
			</tr>
			<tr style="font-size: 1em" class="hide">
				<td><b>Refunder</b></td>
				<td colspan="2"><openmrs_tag:userField formFieldName="refunder" initialValue="${authUser.userId}"/></td>
				<td colspan="4"></td>
				<td><div style="text-align: right;"><b>Amount Paid</b></div></td>
				<td><div class="amount">${payment.amountPaid}</div></td>				
			</tr>
			<tr class="hide">
				<td><b>Refunding Date</b></td>
				<td colspan="2"><input type="text" autocomplete="off" name="refundingDate" size="11" onclick="showCalendar(this);" value="<openmrs:formatDate date='${todayDate}' type="string"/>"/></td>
			    <td colspan="4"></td>
				<td><b>Refunded Amount</b></td>
				<td colspan="2"><input type="text" autocomplete="off" name="refundedAmount" size="11" class="numbers" value=""/></td>							
			</tr>									
			<tr>
				<td colspan="10"><hr/></td>
			</tr>			
			<tr style="font-size: 1.2em">			
					<td colspan="2"><input type="submit"  value="Submit Refund" style="min-width: 200px;" onclick="check()"/></td>			
			 <td colspan="3"></td>
              <!-- 
			<c:if test="${billingtag:amountPaidForPatientBill(patientBill.patientBillId)>0 ||patientBill.beneficiary.insurancePolicy.insurance.currentRate.rate==100 || patientBill.beneficiary.insurancePolicy.thirdParty!=nil}">
			 -->			
			</tr>
			
		
			</c:if>
		</table>
	</form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>





