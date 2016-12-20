<%@ include file="/WEB-INF/template/include.jsp"%>


<div class="box">
	<c:if test="${empty policies}">
		<span style="color: red;">The patient does not have any
			Insurance... Please send the patient back to the Registration to be
			registered</span>
	</c:if>
	<table>
		<c:if test="${!empty policies}">
			<tr>
				<td colspan="2"><b>INSURANCES</b></td>
			</tr>
		</c:if>
		<tr>
			<c:forEach items="${policies}" var="policy">
				<tr>
				
					<td>${policy[0]}:</td>
					<!-- <td><b><a
					href="${pageContext.request.contextPath}/module/mohbilling/billing.form?insurancePolicyId=&ipCardNumber=${policy[1]}">${policy[1]}</a></b>
					</td> -->
					<td><b><a
					href="${pageContext.request.contextPath}/module/mohbilling/globalBill.list?insurancePolicyId=${policy[2]}&ipCardNumber=${policy[1]}">${policy[1]}</a></b>
					</td>
				</tr>
			</c:forEach>
	</table>
</div>

<div id="bill_patient"></div>
