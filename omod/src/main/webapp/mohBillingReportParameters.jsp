<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/headerMinimal.jsp" %>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ taglib prefix="mohbilling_tag" tagdir="/WEB-INF/tags/module/mohbilling" %>

<style>
.columnHeader {
	background: none repeat scroll 0 0 #E6E6E6;
	border: 1px solid #D3D3D3;
	color: #555555;
	cursor: pointer;
	font-size: 0.8em;
	font-weight: normal;
	margin: 0;
	padding: 3px 0 3px 5px;
	text-align: left;
}

.rowValue {
	font-size: 0.8em;
	font-weight: normal;
	margin: 0;
	padding: 5px;
	vertical-align: top;
	border-top: 1px solid cadetblue;
}

.rowAmountValue {
	font-size: 0.8em;
	font-weight: normal;
	margin: 0;
	padding: 5px;
	/* Here I would like to set the totals at the bottom*/
	vertical-align: bottom;
	border-top: 1px solid cadetblue;
	border-right: 1px solid cadetblue;
	border-left: 1px solid cadetblue;
}

.rowTotalValue {
	font-size: 0.8em;
	font-weight: normal;
	margin: 0;
	padding: 5px;
	vertical-align: top;
	border-top: 1px solid cadetblue;
	border-right: 1px solid cadetblue;
	border-bottom: 1px solid cadetblue;
	border-left: 1px solid cadetblue;
}

.even {
	background-color: whitesmoke;
}

.numbers {
	text-align: right;
	background-color: whitesmoke;
}

.amount {
	text-align: right;
	background-color: #C9DAFF;
	border: 2px solid #000000;
	font-weight: bold;
}

.right {
	text-align: right;
}

.center {
	text-align: center;
}

.searchRow:hover {
	background: #F0E68C;
	cursor: pointer;
}
	.boxHeader {
      background-color: #51A351;
    }
    .box {
      border: 1px solid #51A351;
    }


</style>

<b class="boxHeader">Search Form(Advanced)</b>
<div class="box">
	<form method="post" action=" ">
	<input type="hidden" name="formStatus" id="formStatusId" value="" />
		<table>
			<tr class="dates">
				<td>
					<table>
						<tr>
							<td>Start Date:</td> <td> <input type="text" size="11"
								value="${startDate}" name="startDate"
								onclick="showCalendar(this)" /></td>
							<td class="timelabel" >hh</td>
							<td>
							<select name="startHour" class="time">
							<option value="00">00</option>
							<c:forEach var="counter" begin="1" end="9">
   								  <option value="0${counter}">0${counter}</option>
							 </c:forEach>

							 <c:forEach var="j" begin="10" end="23">
   								<option value="${j}">${j}</option>
							 </c:forEach>
				             </select>

							</td>
							<td class="timelabel">mm</td>
						 <td>
							<select name="startMinute" class="time">
							<option value="00">00</option>
							 <c:forEach var="i" begin="1" end="9">
							 <option value="0${i}">0${i}</option>
							 </c:forEach>

							 <c:forEach var="j" begin="10" end="59">
   								  <option value="${j}">${j}</option>
							 </c:forEach>

				             </select>
						  </td>
						  <td class="reportType">Report Type</td>
							<td class="reportType">
								<select name="reportType">
									<option value="">Please!!! Select Report Type </option>
									<option value="NO_DCP_Report">Ordinary Report</option>
									<option value="DCP_Report">DCP Report</option>
									<option value="All">All</option>
								</select>
							</td>
						  <td class="deposit">Type</td>
						  <td class="deposit">
						  <select name="transactionType">
						  <option value="deposit">Deposit</option>
						   <option value="billPayment">Payment</option>
						   <option value="withdrawal">Withdrawal</option>
						  </select>
						  </td>
							<td class="billCreator">Bill Creator :</td>
							<td class="billCreator"><openmrs_tag:userField formFieldName="billCreator" initialValue="${billCreator}"/></td>
						</tr>
						<tr>
							<td>End Date:</td> <td> <input type="text" size="11"
								value="${endDate}" name="endDate" onclick="showCalendar(this)" /></td>
							<td class="timelabel" >hh</td>
							<td>
							<select name="endHour" class="time">
							<option value="00">00</option>
							 <c:forEach var="counter" begin="1" end="9">
							 <option value="0${counter}">0${counter}</option>
							 </c:forEach>

							 <c:forEach var="j" begin="10" end="23">
   								 <option value="${j}">${j}</option>
							 </c:forEach>
				             </select>
							</td>

                           <td class="timelabel">mm</td>
                           <td>
							<select name="endMinute" class="time">
							<option value="00">00</option>
							 <c:forEach var="i" begin="1" end="9">
							 <option value="0${i}">0${i}</option>
							 </c:forEach>

							 <c:forEach var="j" begin="10" end="59">
   								  <option value="${j}">${j}</option>
							 </c:forEach>

				             </select>
						  </td>

						  	<td class="billStatus">Bill Status</td>
							<td class="billStatus">
								<select name="billStatus">
									<option value="">---</option>
									<option value="FULLY PAID" ${billStatus== 'FULLY PAID' ? 'selected' : ''}>FULLY PAID</option>
									<option value="UNPAID" ${billStatus== 'UNPAID' ? 'selected' : ''}>UNPAID</option>
									<option value="PARTLY PAID" ${billStatus== 'PARTLY PAID' ? 'selected' : ''}>PARTLY PAID</option>
								</select>
							</td>

						  	<td class="collector">Collector :</td>
		    				<td class="collector"><openmrs_tag:userField formFieldName="cashCollector" initialValue="${cashCollector}" roles="Cashier;Chief Cashier" /></td>

								<td class="paymentType">Type</td>
								<td class="paymentType">
								<select name="paymentType">
									<option value="">All</option>
								<!-- <option value="cashPayment">Cash Payment</option>
									<option value="depositPayment">Deposit Payment</option> -->
								</select>
								</td>
						</tr>

						<tr class="insurances">
							<td class="insurances">Insurance: </td>
							<td class="insurances">
								<select name="insuranceId" class="insurance">
									<option value="">--select--</option>
									<c:forEach items="${insurances }" var="insurance">
										<option value="${insurance.insuranceId }">${insurance.name }</option>
									</c:forEach>
								</select>
							</td>
						</tr>
						<tr class="services">
							<td class="services">Service : </td>
							<td class="services">
								<select name="departmentId">
									<option value="">--select--</option>
									<c:forEach items="${departments }" var="department">
										<option value="${department.departmentId }">${department.name }</option>
									</c:forEach>
								</select>
							</td>
						</tr>
						<tr class="thirdParties">
							<td class="thirdParties">Third Party : </td>
							<td class="thirdParties">
								<select name="thirdPartyId" class="thirdParty">
									<option value="">--select--</option>
									<c:forEach items="${thirdParties }" var="thirdParty">
										<option value="${thirdParty.thirdPartyId }">${thirdParty.name }</option>
									</c:forEach>
								</select>
							</td>
						</tr>

					</table>
				</td>

			<!-- </tr>

			<tr class="insurances">
			<td>Insurance : </td>
				<td>
				  <select name="insuranceId" class="insurance">
				    <option value="">--select--</option>
				    <c:forEach items="${insurances }" var="insurance">
				    <option value="${insurance.insuranceId }">${insurance.name }</option>
				    </c:forEach>
				  </select>
				</td>

				<td class="services">Service : </td>
				<td class="services">
				  <select name="departmentId">
				    <option value="">--select--</option>
				    <c:forEach items="${departments }" var="department">
				    <option value="${department.departmentId }">${department.name }</option>
				    </c:forEach>
				  </select>
				</td>
			</tr>

			<tr class="thirdParties">
			<td>Third Party : </td>
				<td>
				  <select name="thirdPartyId" class="thirdParty">
				    <option value="">--select--</option>
				    <c:forEach items="${thirdParties }" var="thirdParty">
				    <option value="${thirdParty.thirdPartyId }">${thirdParty.name }</option>
				    </c:forEach>
				  </select>
				</td> -->
			</tr>
		</table>
		<input type="submit" value="Search" id="submitId" />
	</form>
</div>