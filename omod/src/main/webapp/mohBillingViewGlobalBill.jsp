<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-1.3.2.js" />
<%@ taglib prefix="billingtag"
	uri="/WEB-INF/view/module/mohbilling/taglibs/billingtag.tld"%>
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingBillHeader.jsp"%>


<h2>Global Bill # ${globalBill.billIdentifier} </h2>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

	<c:set var="ipCardNumber" value="${globalBill.admission.insurancePolicy.insuranceCardNo}" />
	<c:set var="globalBillId" value="${globalBill.globalBillId}" />
	<c:set var="insurancePolicy" value="${globalBill.admission.insurancePolicy}" />
	<c:set var="billIdentifier" value="${globalBill.billIdentifier}" />
	<c:set var="admissionDate" value="${globalBill.admission.admissionDate}" />
	<c:set var="dischargingDate" value="${globalBill.closingDate}"/>
	<c:set var="admissionMode" value="${globalBill.admission.isAdmitted}"/>
	
	<c:set var="insuranceRate" value="${insurancePolicy.insurance.currentRate.rate}"/>
	<c:set var="patientRate" value="${100-insurancePolicy.insurance.currentRate.rate}"/>
	
<b class="boxHeader">Summary</b>
<div class="box">
	<table>
		<tr>
		   <td>Names:</td> <td><b>${insurancePolicy.owner.personName }</b></td>
		   <td>Age </td><td><b> : ${insurancePolicy.owner.age }</b></td>
		   <td>Sex </td><td> : <b>${(insurancePolicy.owner.gender=='F')?'Female':'Male'}</b></td>
		</tr>
		<tr>
		   <td>Insurance:</td> <td><b>${insurancePolicy.insurance.name}</b></td>
		   <td>Card Nbr:</td><td><b>${insurancePolicy.insuranceCardNo }</b></td>	   
		</tr>
		
		<tr>
		<td>Admission Mode</td>
		<c:if test="${globalBill.admission.isAdmitted}">
		<td> : <b>IPD</b></td>
		</c:if>
		<c:if test="${not globalBill.admission.isAdmitted}">
		<td> : <b>OPD</b></td>
		</c:if>
		<td>Admission Date</td><td> : <b><fmt:formatDate pattern="yyyy-MM-dd" value="${admissionDate}" /></b></td> 
		<td>Discharge Date</td><td> : <b><fmt:formatDate pattern="yyyy-MM-dd" value="${dischargingDate}" /></b></td>
		</tr>

	</table>
</div>
<br/>

<c:if test="${!globalBill.closed}">
<div style="float: left;">
<a href="billing.form?insurancePolicyId=${insurancePolicy.insurancePolicyId}&ipCardNumber=${ipCardNumber}&globalBillId=${globalBillId}">Add Consommation | </a>

<a href="javascript:window.open('admission.form?globalBillId=${globalBill.globalBillId}&insurancePolicyId=${insurancePolicy.insurancePolicyId }&ipCardNumber=${insurancePolicy.insuranceCardNo}&discharge=true', 'dischargeWindow', 'height=300,width=450,left=100,top=100,resizable=yes,scrollbars=yes,toolbar=no,menubar=no,location=no,directories=no,status=yes').focus()">Dischage the Patient</a>
</div>
</c:if>

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
		  <th>Unit Price</th>
		  <th>100%</th>
		  <th><b>${insuranceRate}</b>%</th>
		  <th><b>${patientRate}</b> %</th>			
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
	<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: red;"><fmt:formatNumber value="${total100}" type="number" pattern="#.##"/></b></td>
	<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: red;"><fmt:formatNumber value="${totalInsurance}" type="number" pattern="#.##"/></b></td>
	<td class="rowValue ${(status.count%2!=0)?'even':''}"><b style="color: red;"><fmt:formatNumber value="${totalTM}" type="number" pattern="#.##"/></b></td>
	</tr>
	</table>
</div>
<%@ include file="/WEB-INF/template/footer.jsp"%>