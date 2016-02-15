<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require privilege="Manage Billing Reports"
	otherwise="/login.htm" redirect="/mohbilling/cohort.orm" />
<openmrs:htmlInclude
	file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />
<openmrs:htmlInclude
	file="/moduleResources/@MODULE_ID@/scripts/jquery.PrintArea.js" />
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ taglib prefix="billingtag"
	uri="/WEB-INF/view/module/@MODULE_ID@/taglibs/billingtag.tld"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>
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

<h2>
	<spring:message code="@MODULE_ID@.billing.report" />
</h2>

<ul id="menu">
        <openmrs:hasPrivilege privilege="Billing Reports - View Find Bills">
		<li class="<c:if test='<%= request.getRequestURI().contains("Cohort")%>'> active</c:if>">
			<a href="cohort.form"><spring:message code="@MODULE_ID@.billing.cohort"/></a>
		</li>
		</openmrs:hasPrivilege>
		
		<openmrs:hasPrivilege privilege="Billing Reports - View Payments">
	    <li>
			<a href="received.form"><spring:message code="@MODULE_ID@.billing.received"/></a>
		</li>
		</openmrs:hasPrivilege>
		
		 <openmrs:hasPrivilege privilege="Billing Reports - View Revenue">
		 <li class="<c:if test='<%= request.getRequestURI().contains("Revenue")%>'>active</c:if>">
			<a href="recettes.form"><spring:message code="@MODULE_ID@.billing.revenue"/></a>
		</li>
		</openmrs:hasPrivilege>
		
		 <openmrs:hasPrivilege privilege="Billing Reports - View Invoice">
		<li>
			<a href="invoice.form"><spring:message code="@MODULE_ID@.billing.invoice"/></a>
		</li>
		</openmrs:hasPrivilege>
		
		 <openmrs:hasPrivilege privilege="Billing Reports - View Releve">
		<li>
			<a href="facture.form"><spring:message code="@MODULE_ID@.billing.facture"/></a>
		</li>
		</openmrs:hasPrivilege>
		
		<!-- 
		<li>
			<a href="hmisReport.form">HMIS Reports</a>
		</li>
		 -->
</ul>

<b class="boxHeader">Search Form(Advanced)</b>
<div class="box">


	<form method="post" action="refundBillReport.form">
		<input type="hidden" name="patientIdnew" value="${patientId}" />
		<input type="hidden" name="formStatus" id="formStatusId" value="" />
		<table>
			<tr>
				<td width="10%">When?</td>
				<td>
					<table>
						<tr>
							<td>On Or After <input type="text" size="11"
								value="${startDate}" name="startDate"
								onclick="showCalendar(this)" /></td>
							<td>
							<select name="startHour">
					          <option value="00">00</option>
				              <option value="01">01</option>
				              <option value="02">02</option>
				              <option value="03">03</option>
				              <option value="04">04</option>
				              <option value="05">05</option>
				              <option value="06">06</option>
				              <option value="07">07</option>
				              <option value="08">08</option>
				              <option value="09">09</option>
				              <option value="10">10</option>
				              <option value="11">11</option>
				              <option value="12">12</option>
				              <option value="13">13</option>
				              <option value="14">14</option>
				              <option value="15">15</option>
				              <option value="16">16</option>
				              <option value="17">17</option>
				              <option value="18">18</option>
				              <option value="19">19</option>
				              <option value="20">20</option>
				              <option value="21">21</option>
				              <option value="22">22</option>
				              <option value="23">23</option>
				             </select>
							
							</td>
							<td>
							<select name="startMinute">
					          <option value="00">00</option>
				              <option value="01">01</option>
				              <option value="02">02</option>
				              <option value="03">03</option>
				              <option value="04">04</option>
				              <option value="05">05</option>
				              <option value="06">06</option>
				              <option value="07">07</option>
				              <option value="08">08</option>
				              <option value="09">09</option>
				              <option value="10">10</option>
				              <option value="11">11</option>
				              <option value="12">12</option>
				              <option value="13">13</option>
				              <option value="14">14</option>
				              <option value="15">15</option>
				              <option value="16">16</option>
				              <option value="2">17</option>
				              <option value="17">18</option>
				              <option value="19">19</option>
				              <option value="20">20</option>
				              <option value="21">21</option>
				              <option value="22">22</option>
				              <option value="23">23</option>
				              <option value="24">24</option>
				              <option value="25">25</option>
				              <option value="25">26</option>
				              <option value="26">27</option>
				              <option value="27">28</option>
				              <option value="29">29</option>
				              <option value="30">30</option>
				              <option value="31">31</option>
				              <option value="32">32</option>
				              <option value="33">33</option>
				              <option value="34">34</option>
				              <option value="35">35</option>
				              <option value="36">36</option>
				              <option value="37">37</option>
				              <option value="38">38</option>
				              <option value="39">39</option>
				              <option value="40">40</option>
				              <option value="41">41</option>
				              <option value="42">42</option>
				              <option value="43">43</option>
				              <option value="44">44</option>
				              <option value="45">45</option>
				              <option value="46">46</option>
				              <option value="47">47</option>
				              <option value="48">48</option>
				              <option value="49">49</option>
				              <option value="50">50</option>
				              <option value="51">51</option>
				              <option value="52">52</option>
				              <option value="53">53</option>
				              <option value="54">54</option>
				              <option value="55">55</option>
				              <option value="56">56</option>
				              <option value="57">57</option>
				              <option value="58">58</option>
				              <option value="59">59</option>
				             </select>
				             </td>
						</tr>
						<tr>
							<td>On Or Before <input type="text" size="11"
								value="${endDate}" name="endDate" onclick="showCalendar(this)" /></td>
								<td>
							<select name="endHour">
					          <option value="00">00</option>
				              <option value="01">01</option>
				              <option value="02">02</option>
				              <option value="03">03</option>
				              <option value="04">04</option>
				              <option value="05">05</option>
				              <option value="06">06</option>
				              <option value="07">07</option>
				              <option value="08">08</option>
				              <option value="09">09</option>
				              <option value="10">10</option>
				              <option value="11">11</option>
				              <option value="12">12</option>
				              <option value="13">13</option>
				              <option value="14">14</option>
				              <option value="15">15</option>
				              <option value="16">16</option>
				              <option value="17">17</option>
				              <option value="18">18</option>
				              <option value="19">19</option>
				              <option value="20">20</option>
				              <option value="21">21</option>
				              <option value="22">22</option>
				              <option value="23">23</option>
				             </select>
								</td>
                           <td>
							<select name="endMinute">
					          <option value="00">00</option>
				              <option value="01">01</option>
				              <option value="02">02</option>
				              <option value="03">03</option>
				              <option value="04">04</option>
				              <option value="05">05</option>
				              <option value="06">06</option>
				              <option value="07">07</option>
				              <option value="08">08</option>
				              <option value="09">09</option>
				              <option value="10">10</option>
				              <option value="11">11</option>
				              <option value="12">12</option>
				              <option value="13">13</option>
				              <option value="14">14</option>
				              <option value="15">15</option>
				              <option value="16">16</option>
				              <option value="2">17</option>
				              <option value="17">18</option>
				              <option value="19">19</option>
				              <option value="20">20</option>
				              <option value="21">21</option>
				              <option value="22">22</option>
				              <option value="23">23</option>
				              <option value="24">24</option>
				              <option value="25">25</option>
				              <option value="25">26</option>
				              <option value="26">27</option>
				              <option value="27">28</option>
				              <option value="29">29</option>
				              <option value="30">30</option>
				              <option value="31">31</option>
				              <option value="32">32</option>
				              <option value="33">33</option>
				              <option value="34">34</option>
				              <option value="35">35</option>
				              <option value="36">36</option>
				              <option value="37">37</option>
				              <option value="38">38</option>
				              <option value="39">39</option>
				              <option value="40">40</option>
				              <option value="41">41</option>
				              <option value="42">42</option>
				              <option value="43">43</option>
				              <option value="44">44</option>
				              <option value="45">45</option>
				              <option value="46">46</option>
				              <option value="47">47</option>
				              <option value="48">48</option>
				              <option value="49">49</option>
				              <option value="50">50</option>
				              <option value="51">51</option>
				              <option value="52">52</option>
				              <option value="53">53</option>
				              <option value="54">54</option>
				              <option value="55">55</option>
				              <option value="56">56</option>
				              <option value="57">57</option>
				              <option value="58">58</option>
				              <option value="59">59</option>
				             </select>
						</td>
						</tr>
					</table>
				</td>
				<td>Collector :</td>
				<td><openmrs_tag:userField formFieldName="cashCollector"
						initialValue="${cashCollector}" roles="Cashier;Chief Cashier;Admin" /></td>
			</tr>

		</table>
		<input type="submit" value="Search" id="submitId" />
	</form>

</div>
<br/>
<c:if test="${fn:length(refundedBills)!=0}">

<c:set var="refundedBills" value="${refundedBills}" />
<b class="boxHeader"> Refund report</b> 
   
<div class="box">


<table width="99%">
	<tr>
		<th>No</th>
		<th>BillId</th>
		<th>Beneficiary</th>
		<th>Creator</th>
		<th>Created Date</th>	
		<th>Payment status </th>
				
		
	</tr>

	<c:forEach items="${refundedBills}" var="refundedBill" varStatus="status">
		<tr>
			<td class="rowValue">${status.count}</td>
			<td class="rowValue">${refundedBill.patientBillId}</td>
			<td class="rowValue"><b>${refundedBill.beneficiary.patient.familyName}</b></td>
			<td class="rowValue">${refundedBill.creator}</td>
			<td class="rowValue">${refundedBill.createdDate}</td>
						
			<td class="rowValue">				

			<table width="50%">

			   <tr>
				    <th></th>				   
					<th><b>Paid Amount</b></th>					
					<th colspan="10"><b>Collector</b></th>
					<th colspan="10"><b>Payment Date&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</b></th>
				</tr>
				<c:forEach items="${refundedBill.payments}" var="payment" varStatus="status">

					<tr>
					    <td class="rowValue even"><b>${status.count})</b></td>					 
						<td class="rowValue even">${payment.amountPaid}</td>
						<td class="rowValue even">${payment.collector }</td>
						<td class="rowValue even">${payment.createdDate}</td>						

					</tr>
			</c:forEach>
			</table>
	   </td>
	   	   
	</tr>

	</c:forEach>
	
</table>


</div>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>