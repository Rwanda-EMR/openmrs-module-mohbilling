<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ taglib prefix="mohbilling_tag" tagdir="/WEB-INF/tags/module/mohbilling" %>


<b class="boxHeader">Search Form(Advanced)</b>
<div class="box">
	<form method="post" action=" ">	
	<input type="hidden" name="formStatus" id="formStatusId" value="" />	
		<table>
			<tr class="dates">
				<td width="10%">When?</td>
				<td>
					<table>
						<tr>
							<td>On Or After <input type="text" size="11"
								value="${startDate}" name="startDate"
								onclick="showCalendar(this)" /></td>
							<td>
							<select name="startHour" class="time">
							<option value="">hh</option>
							<c:forEach var="counter" begin="0" end="9">
   								  <option value="0${counter}">0${counter}</option>
							 </c:forEach>
							 
							 <c:forEach var="j" begin="10" end="23">
   								<option value="${j}">${j}</option>
							 </c:forEach>
				             </select>
							
							</td>
						 <td>
							<select name="startMinute" class="time">
							<option value="">mm</option>
							 <c:forEach var="i" begin="0" end="9">
							 <option value="0${i}">0${i}</option>
							 </c:forEach>
							 
							 <c:forEach var="j" begin="10" end="59">
   								  <option value="${j}">${j}</option>
							 </c:forEach>
							 
				             </select>
						  </td>

						</tr>
						<tr>
							<td>On Or Before <input type="text" size="11"
								value="${endDate}" name="endDate" onclick="showCalendar(this)" /></td>
							<td>
							<select name="endHour" class="time">
							<option value="">hh</option>
							 <c:forEach var="counter" begin="0" end="9">
							 <option value="0${counter}">0${counter}</option>
							 </c:forEach>
							 
							 <c:forEach var="j" begin="10" end="23">
   								 <option value="${j}">${j}</option>
							 </c:forEach>
				             </select>
							</td>
								

                           <td>
							<select name="endMinute" class="time">
							<option value="">mm</option>
							 <c:forEach var="i" begin="0" end="9">
							 <option value="0${i}">0${i}</option>
							 </c:forEach>
							 
							 <c:forEach var="j" begin="10" end="59">
   								  <option value="${j}">${j}</option>
							 </c:forEach>
							 
				             </select>
						  </td>
						  
						  	<td class="collector">Collector :</td>
		    				<td class="collector"><openmrs_tag:userField formFieldName="cashCollector" initialValue="${cashCollector}" roles="Cashier;Chief Cashier" /></td>
							
											  
						</tr>

					</table>
				</td>
				
			</tr>

			
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
				</td>
			</tr>
		</table>
		<input type="submit" value="Search" id="submitId" />
	</form>
</div>
