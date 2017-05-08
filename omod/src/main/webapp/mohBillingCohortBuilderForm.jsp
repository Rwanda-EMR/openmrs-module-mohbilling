<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-1.3.2.js" />
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingReportHeader.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/mohbilling/taglibs/billingtag.tld" %>
<script type="text/javascript" language="JavaScript">
	var $bill = jQuery.noConflict();
	$bill(document).ready(function(){
		$bill('.meta').hide();
		$bill('#submitId').click(function() {
			$bill('#formStatusId').val("clicked");
		});
		$bill("input#print_button").click(function() {
			$bill('.meta').show();
			$bill("div.printarea").printArea();
			$bill('.meta').hide();
		});

	});
</script>


<style>
.collector,.time,.deposit,.paymentType {
    display: none;
}
a.print {
    border: 2px solid #009900;
    background-color: #aabbcc;
    color: #ffffff;
    text-decoration: none;
}
</style>

<h3>
	Search Fiche de Consommations
</h3>

<c:import url="mohBillingReportParameters.jsp" />


<c:if test="${empty consommations }">
 <div style="text-align: center;color: red;"><p>No bill found!</p></div>
</c:if>

<c:if test="${not empty consommations }">
<br/>
<b class="boxHeader">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;


<a href="#" class="print">PDF</a></b>
<div class="box">
<table width="99%">
	<tr>
		<td>No</td>
		<td>Date</td>
		<td>Department</td>
		<td> Creator</td>
		<td>Policy Id Number</td>
		<td>Beneficiary</td>

		<td>Billable Services</td>

		<td>Insurance Name</td>
		<td>Total</td>
		<td>Insurance due</td>
		<td>Patient Due</td>
		<td>Paid Amount</td>
		<td>Status</td>
		<td></td>
		<td>Collector</>

	</tr>
	<c:set var="totalAmountAllConsom" value="0"/>
	<c:set var="totalAmountPaidAllConsom" value="0"/>
	<c:set var="totalInsurances" value="0"/>
	<c:set var="totalPatients" value="0"/>
	<c:forEach items="${consommations}" var="c" varStatus="status">
		<tr>
			<td class="rowValue">${status.count}</td>
			<td class="rowValue">
			<fmt:formatDate pattern="yyyy-MM-dd" value="${c.createdDate}" />
			</td>
			<td class="rowValue">${c.department.name}</td>
			<td class="rowValue">${c.creator.person.familyName}&nbsp;${c.creator.person.givenName}</td>
			<td class="rowValue"><b>${c.beneficiary.policyIdNumber}</b></td>
			<td class="rowValue">${c.beneficiary.patient.personName}</td>
			<td class="rowValue">

			<table>

				<tr>
				    <td></td>
					<td><b>Service Name</b></td>
					<td colspan="2"><b>Price</b></td>
					<td colspan="6"><b>Quantity</b></td>
					<td colspan="3"><b>Total</b></td>
				</tr>
				 <c:set var="insuranceRate" value="${(c.beneficiary.insurancePolicy.insurance.currentRate.rate)/100 }"/>
				 <c:set var="patientRate" value="${(100-c.beneficiary.insurancePolicy.insurance.currentRate.rate)/100}"/>
				<c:set var="totalAmountByConsom" value="0"/>
				<c:forEach items="${c.billItems}" var="item" varStatus="status">
                    <c:if test="${not item.voided}">
					<tr>
					    <td>${status.count}-</td>
						<td>
						${item.service.facilityServicePrice.name}
						</td>

						<td colspan="4">${item.unitPrice }</td>
						<td colspan="6">${item.quantity}</td>
						<td colspan="1">${item.unitPrice*item.quantity}</td>
						<c:set var="totalAmountByConsom" value="${totalAmountByConsom+(item.unitPrice*item.quantity)}" />
					</tr>
					</c:if>
				</c:forEach>
			</table>
			</td>
			<c:set var="totalAmountPaidByCons" value="${billingtag:amountPaidByConsommation(c.consommationId)}"/>
			<td class="rowValue">${c.beneficiary.insurancePolicy.insurance.name}</td>
			<td class="rowAmountValue"><fmt:formatNumber value="${totalAmountByConsom}" type="number" pattern="#.##"/></td>
			<td class="rowAmountValue"><fmt:formatNumber value="${totalAmountByConsom*insuranceRate }" type="number" pattern="#.##"/></td>
			<td class="rowAmountValue"><fmt:formatNumber value="${totalAmountByConsom*patientRate }" type="number" pattern="#.##"/></td>
			<td class="rowAmountValue"><fmt:formatNumber value="${totalAmountPaidByCons}" type="number" pattern="#.##"/></td>
			<c:if test="${totalAmountPaidByCons >= (totalAmountByConsom*patientRate)}">
			<td class="rowAmountValue" style="color: green; font-weight: bold;">FULLY PAID</td>
			</c:if>
			<c:if test="${(totalAmountPaidByCons!='0') && (totalAmountPaidByCons < (totalAmountByConsom*patientRate))}">
			<td class="rowAmountValue" style="color: green; font-weight: bold;">PARTLY PAID</td>
			</c:if>
			<c:if test="${totalAmountPaidByCons=='0'&& (patientRate!='0')}">
			<td class="rowAmountValue" style="color: red; font-weight: bold;">UNPAID</td>
			</c:if>

			<td class="rowTotalValue"><a href="patientBillPayment.form?consommationId=${c.consommationId}">View/</a></td>
             <c:forEach items="${c.patientBill.payments}" var="payment" varStatus="status">
                                 <td class="rowValue">${payment.collector.person.familyName}&nbsp;</br>${payment.collector.person.givenName}</td>
              </c:forEach>

		</tr>
	<c:set var="totalAmountAllConsom" value="${totalAmountAllConsom+totalAmountByConsom}" />
	<c:set var="totalAmountPaidAllConsom" value="${totalAmountPaidAllConsom+totalAmountPaidByCons}" />
	<c:set var="totalInsurances" value="${totalInsurances+(totalAmountByConsom*insuranceRate)}" />
	<c:set var="totalPatients" value="${totalPatients+(totalAmountByConsom*patientRate)}" />
	</c:forEach>
	<tr>
		<td class="rowTotalValue" colspan="8"><b style="color: blue;font-size: 14px;">TOTAL</b></td>
		<td class="rowTotalValue"><b style="color: blue;font-size: 14px;"><fmt:formatNumber value="${totalAmountAllConsom}" type="number" pattern="#.##"/></b></td>
		<td class="rowTotalValue"><b style="color: blue;font-size: 14px;"><fmt:formatNumber value="${totalInsurances}" type="number" pattern="#.##"/></b></td>
		<td class="rowTotalValue"><b style="color: blue;font-size: 14px;"><fmt:formatNumber value="${totalPatients}" type="number" pattern="#.##"/></b></td>
		<td class="rowTotalValue"><b style="color: blue;font-size: 14px;"><fmt:formatNumber value="${totalAmountPaidAllConsom}" type="number" pattern="#.##"/></b></td>
		<td class="rowTotalValue"><b style="color: blue;font-size: 14px;"></b></td>
		<td class="rowTotalValue"><b style="color: red;font-size: 14px;"></b></td>
	</tr>
</table>
</div>
</c:if>


<%@ include file="/WEB-INF/template/footer.jsp"%>