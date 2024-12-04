<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ taglib prefix="billingtag"
		   uri="/WEB-INF/view/module/mohbilling/taglibs/billingtag.tld"%>
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingBillHeader.jsp"%>
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-3.5.1.js" />
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-ui.js" />
<openmrs:htmlInclude file="/moduleResources/mohbilling/jquery-ui.css" />

<style>
	.ui-widget-header,.ui-state-default, ui-button{
		background:lightseagreen;
		color: white;
		border: 1px solid #20B2AA;
		font-weight:  bold;
	}
	#invalid{
		color: red;
		font-size: large;
		font-weight: bold;
	}
</style>

<script>
	$(function(){
		$("#revert_discharge").dialog({
			autoOpen: false,
			resizeable: false,
			buttons: {
				Cancel: function() {$(this).dialog("close");},
			},
			position: {
				my: "center",
				at: "center"
			},
			title: "Discharge Patient",
			height: 450,
			width: 650
			/*hide: "slide",
			show: "slide"*/
		});
		$("#btnRevert").on("click",function(){
			$("#revert_discharge").dialog("open");
		});
		$("#uncloseBill").on('click',function (event){
			if ($("#editGlobalBill option:selected").val() !=""){
				$("#revertGB").submit();
				$("#revert_discharge").dialog("close");
			}
			$("#invalid_close").text("Please! Select Reverting Reason").show().fadeOut(4000);
			event.preventDefault();
		});
	});
</script>

<h2>Global Bill # ${globalBill.billIdentifier} </h2>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="ipCardNumber" value="${globalBill.admission.insurancePolicy.insuranceCardNo}" />
<c:set var="globalBillId" value="${globalBill.globalBillId}" />
<c:set var="insurancePolicy" value="${globalBill.admission.insurancePolicy}" />
<c:set var="billIdentifier" value="${globalBill.billIdentifier}" />
<c:set var="admissionDate" value="${globalBill.admission.admissionDate}" />
<c:set var="dischargingDate" value="${globalBill.closingDate}"/>
<c:set var="admissionMode" value="${globalBill.admission.isAdmitted}"/>
<c:set var="Patient_ID" value="${patientIdentifier}"/>

<c:set var="insuranceRate" value="${insurancePolicy.insurance.currentRate.rate}"/>
<c:set var="patientRate" value="${100-insurancePolicy.insurance.currentRate.rate}"/>

<b class="boxHeader">Summary</b>
<div class="box">
	<table>
		<tr>
			<td>Names:</td> <td><b>${insurancePolicy.owner.personName }</b></td>
			<td>Age </td><td><b> : ${insurancePolicy.owner.age }</b></td>
			<td>Sex </td><td> : <b>${(insurancePolicy.owner.gender=='F')?'Female':'Male'}</b></td> <td></td> <td></td>
			<td> Type of Disease: </td><td> <b> ${globalBill.admission.diseaseType} </b></td> <td></td> <td></td> <td></td> <td></td>
			<c:choose>
				<c:when test = "${globalBill.closingDate!=null}">
					<td>Global Bill Status :</td> <td class="rowAmountValue" style="color: green; font-weight: bold;">DISCHARGED</td>
				</c:when>

				<c:otherwise>
					<td>Global Bill Status :</td>  <td class="rowAmountValue" style="color: red; font-weight: bold;">NOT DISCHARGED</td>
				</c:otherwise>
			</c:choose>
		</tr>
		<tr>
			<td>Insurance:</td> <td><b>${insurancePolicy.insurance.name}</b></td>
			<td>Card No</td><td> :<b>${insurancePolicy.insuranceCardNo }</b></td>
			<td>Primary Care ID</td><td> :<b>${Patient_ID}</b></td>
		</tr>

		<tr>
			<td>Admission Mode :</td>
			<c:if test="${globalBill.admission.isAdmitted}">
				<td style="color: blue; font-weight: bold;">In-Patient</b></td>
			</c:if>
			<c:if test="${not globalBill.admission.isAdmitted}">
				<td style="color: green; font-weight: bold;">Out-Patient</b></td>
			</c:if>
			<td>Admission Type: <b>${(globalBill.admission.admissionType=='1')?'Ordinary Clinic':'Dual Clinic'} </b></td>
			<td>Admission Date</td><td> : <b><fmt:formatDate pattern="yyyy-MM-dd" value="${admissionDate}" /></b></td>
			<td>Discharge Date</td><td> : <b><fmt:formatDate pattern="yyyy-MM-dd" value="${dischargingDate}" /></b></td>
		</tr>
	</table>
</div>
<br/>

<c:if test="${!globalBill.closed}">
	<div style="float: left;">
		<openmrs:hasPrivilege privilege="Add Consommation">
			<a href="billing.form?insurancePolicyId=${insurancePolicy.insurancePolicyId}&ipCardNumber=${ipCardNumber}&globalBillId=${globalBillId}">Add Consommation |</a>
		</openmrs:hasPrivilege>
		<openmrs:hasPrivilege privilege="Discharge Patient">
			<button id="btn">Discharge Patient</button>
			<%--<a href="javascript:window.open('admission.form?globalBillId=${globalBill.globalBillId}&insurancePolicyId=${insurancePolicy.insurancePolicyId }&ipCardNumber=${insurancePolicy.insuranceCardNo}&discharge=true', 'dischargeWindow', 'height=300,width=450,left=100,top=100,resizable=yes,scrollbars=yes,toolbar=no,menubar=no,location=no,directories=no,status=yes').focus()">Discharge the Patient</a>--%>
		</openmrs:hasPrivilege>
	</div>
</c:if>
<openmrs:hasPrivilege privilege="Revert Patient Bill">
	<c:if test="${globalBill.closed==true}">
		<div style="float: left;">
			<button id="btnRevert">Edit Global Bill</button>
		</div>

	</c:if>
</openmrs:hasPrivilege>
<!-- <div id="revert_discharge_error">
	<form action="viewGlobalBill.form?globalBillId=${globalBill.globalBillId}&edit_global_bill=true&revert_global_bill=${revert_global_bill}" method="post" id="revertGB">
		<table>
			<tr><td style="font-size:15px">Names</td><td> : <b>${insurancePolicy.owner.personName }</b></td></tr>
			<tr>
				<td style="font-size:15px">Admission Date</td><td> : <b><fmt:formatDate pattern="yyyy-MM-dd" value="${admissionDate}" /></b></td>
			</tr>
			<tr>
				<td style="font-size:15px">Discharge Date</td><td> : <b><fmt:formatDate pattern="yyyy-MM-dd" value="${dischargingDate}" /></b></td>
			</tr>
			<tr>
				<td style="font-size:15px"><b>Reverting Reason</b></td>
				<td><select name="editGBill" id="editGlobalBill">
					<option value="">                        </option>
					<option value="Missing Items">Missing Items</option>
					<option value="Over Billing">Over Billing</option>
					<option value="Item(s) Not Available">Item(s) Not Available</option>
				</select><td>
			</tr>
			<tr><td></td><td><span id="invalid_close"></span></td></tr>
			<tr>
				<td></td>
				<td><button id="uncloseBill">Revert Closed Bill</button></td>
			</tr>
		</table>
	</form>
</div> -->

<div id="revert_discharge">
    <openmrs:hasPrivilege privilege="Revert Patient Bill">
	<form action="viewGlobalBill.form?globalBillId=${globalBill.globalBillId}&edit_global_bill=true&revert_global_bill=${revert_global_bill}" method="post" id="revertGB">
		<table>
			<tr><td style="font-size:15px">Names</td><td> : <b>${insurancePolicy.owner.personName }</b></td></tr>
			<tr>
				<td style="font-size:15px">Admission Date</td><td> : <b><fmt:formatDate pattern="yyyy-MM-dd" value="${admissionDate}" /></b></td>
			</tr>
			<tr>
				<td style="font-size:15px">Discharge Date</td><td> : <b><fmt:formatDate pattern="yyyy-MM-dd" value="${dischargingDate}" /></b></td>
			</tr>
			<tr>
				<td style="font-size:15px"><b>Reverting Reason</b></td>
				<td><select name="editGBill" id="editGlobalBill">
					<option value="">                        </option>
					<option value="Missing Items">Missing Items</option>
					<option value="Over Billing">Over Billing</option>
					<option value="Item(s) Not Available">Item(s) Not Available</option>
				</select><td>
			</tr>
			<tr><td></td><td><span id="invalid_close"></span></td></tr>
			<tr>
				<td></td>
				<td><button id="uncloseBill">Revert Closed Bill</button></td>
			</tr>
		</table>
	</form>
	</openmrs:hasPrivilege>
</div>

<div style="float: right;">
	<a href="viewGlobalBill.form?globalBillId=${globalBill.globalBillId}&print=true">Print</a>
</div>

<br/>
<b class="boxHeader">Services</b>
<div class="box">
	<table style="width: 100%">

		<tr>
			<th>#.</th>
			<th>Date</th>
			<th>Service</th>
			<th>Quantity</th>
			<th>Dosage</th>
			<th>Unit Price</th>
			<th>100%</th>
			<th><b>Insurance rate: ${insuranceRate}</b>%</th>
			<th><b>Patient rate: ${patientRate}</b> %</th>
		</tr>
		<c:set var="total100" value="0"/>
		<c:set var="totalInsurance" value="0"/>
		<c:set var="totalTM" value="0"/>
		<c:forEach items="${serviceRevenueList}" var="sr" varStatus="status">
			<c:if test="${sr.dueAmount !=0 }">
				<tr>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"><b>${sr.service }</b></td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
				</tr>
				<c:set var="totalByCategory100" value="0"/>
				<c:set var="totalByCategoryInsurance" value="0"/>
				<c:set var="totalByCategoryTM" value="0"/>

				<c:forEach items="${sr.billItems}" var="item" varStatus="status">
					<c:set var="itemCost" value="${item.quantity*item.unitPrice}"/>
					<tr>
						<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
						<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatDate pattern="yyyy-MM-dd" value="${item.serviceDate}" /></td>
						<td class="rowValue ${(status.count%2!=0)?'even':''}">${item.service.facilityServicePrice.name }</td>
						<td class="rowValue ${(status.count%2!=0)?'even':''}">${item.quantity }</td>
						<td class="rowValue ${(status.count%2!=0)?'even':''}">${item.drugFrequency}</td>
						<td class="rowValue ${(status.count%2!=0)?'even':''}">${item.unitPrice }</td>
						<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${itemCost}" type="number" pattern="#.##"/></td>
						<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${(itemCost*insuranceRate)/100 }" type="number" pattern="#.##"/></td>
						<td class="rowValue ${(status.count%2!=0)?'even':''}"><fmt:formatNumber value="${(itemCost*patientRate)/100 }" type="number" pattern="#.##"/></td>
					</tr>
					<!-- total by service category -->
					<c:set var="totalByCategory100" value="${totalByCategory100+itemCost}"/>
					<c:set var="totalByCategoryInsurance" value="${totalByCategoryInsurance+((itemCost*insuranceRate)/100)}"/>
					<c:set var="totalByCategoryTM" value="${totalByCategoryTM+((itemCost*patientRate)/100)}"/>

				</c:forEach>
				<!-- big total 100%,Insurance and TM -->
				<c:set var="total100" value="${total100+totalByCategory100}"/>
				<c:set var="totalInsurance" value="${totalInsurance+totalByCategoryInsurance}"/>
				<c:set var="totalTM" value="${totalTM+totalByCategoryTM}"/>
				<tr>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"><b><fmt:formatNumber value="${totalByCategory100}" type="number" pattern="#.##"/></b></td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"><b><fmt:formatNumber value="${totalByCategoryInsurance}" type="number" pattern="#.##"/></b></td>
					<td class="rowValue ${(status.count%2!=0)?'even':''}"><b><fmt:formatNumber value="${totalByCategoryTM}" type="number" pattern="#.##"/></b></td>
				</tr>
			</c:if>
		</c:forEach>
		<tr>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"><b>TOTAL</b></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: red;"><fmt:formatNumber value="${total100}" type="number" pattern="#.##"/></b></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: red;"><fmt:formatNumber value="${totalInsurance}" type="number" pattern="#.##"/></b></td>
			<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: red;"><fmt:formatNumber value="${totalTM}" type="number" pattern="#.##"/></b></td>
		</tr>
	</table>
</div>
<%@ include file="templates/dischargePatient.jsp"%>
<%@ include file="/WEB-INF/template/footer.jsp"%>

