<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<openmrs:require privilege="Manage Recovery" otherwise="/login.htm" redirect="/module/@MODULE_ID@/manageRecovery.form" />
<script language="javascript" type="text/javascript">

var $k= jQuery.noConflict();

</script>
<b class="boxHeader"> Insurance Due </b>
<div class="box">

<form action="manageRecovery.form" method="post" name="">

<table>
	<tr>
		<td width="10%">When?</td>
		<td>
		<table>
			<tr>
				<td>On Or After <input type="text" size="11" value="${startDate}"
					name="startDate" onclick="showCalendar(this)" /></td>
			</tr>
			<tr>
				<td>On Or Before <input type="text" size="11" value="${endDate}"
					name="endDate" onclick="showCalendar(this)" /></td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td>Insurance:</td>
		<td><select name="insurance">
			<option selected="selected" value="${insuranceId}"><c:choose>
             <c:when test="${insuranceId != null}"> ${insurance.name} 

             </c:when>

               <c:otherwise>--select-- 
             </c:otherwise>
            </c:choose>
		</option>
			<c:forEach items="${allInsurances}" var="ins">
				<option value="${ins.insuranceId}">${ins.name}</option>
			</c:forEach>
		</select></td>
	</tr>
</table>

<input type="submit" value="Search" />

</form>
</div>
</br> </br>

<c:if test="${!empty amount}">


<b class="boxHeader"> Due Amount </b>
<div class="box">
<table width="100%" >
<tr> <td> Start Period</td>  <td> End Period </td><td> Insurance  </td><td>Due Amount </td><td>Already Paid Amount</td><td> Remaining to pay </td></tr> 
<tr> <td>${startDate}  </td>  <td>${endDate} </td><td>${insuranceName} </td><td>${amount} </td><td>${AlreadyPaidAmount}</td><td>${amount - AlreadyPaidAmount}</td></tr> </table>
</div>
</c:if>

</br> </br>
<c:if test="${amount > 0}">
<b class="boxHeader"> Payment Form </b>
<div class="box">
<form action="manageRecovery.form" method="post" name="payment" onSubmit="return validateNumber()">

<table >
			<tr>
				<td>Amount paid </td> <td><input type="text" size="11" value=""
					name="amountPaid" /></td>
			</tr>
			
	    <tr>
				<td>Payment date </td> <td><input type="text" size="11" value=""
					name="datePayment" onclick="showCalendar(this)" /></td>
			</tr>
		<tr>
				<td></td> <td><input type="submit" size="11" value="Submit"
					name="submit" /></td>
			</tr>	
</table>
<input type="hidden" name="startDateNew" value="${startDate}" />
<input type="hidden" name="endDateNew" value="${endDate}" />
<input type="hidden" name="insuranceId" value="${insuranceId}" />
</form>

</div>


</c:if>

</br> </br>

<c:if test="${!empty amountPaid}">


<b class="boxHeader"> Paid Amount </b>
<div class="box">
<table width="100%" >
<tr> <td> perion From</td> <td> Period To</td> <td>Amount Paid</td>  </tr> 
<tr> <td>${startDateNew}  </td><td>${endDateNew}</td> <td> ${amountPaid}</td> </tr> </table>
</div>
</c:if>


<%@ include file="/WEB-INF/template/footer.jsp"%>
