<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ taglib prefix="mohbilling_tag" tagdir="/WEB-INF/tags/module/mohbilling" %>


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
			</tr>
		</table>
		<input type="submit" value="Search" id="submitId" />
	</form>
</div>