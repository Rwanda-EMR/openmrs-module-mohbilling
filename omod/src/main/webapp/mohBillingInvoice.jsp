<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/mohbilling/scripts/jquery-1.3.2.js" />
<openmrs:require privilege="Manage Billing Reports" otherwise="/login.htm" redirect="/mohbilling/cohort.orm" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery.PrintArea.js" />
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ taglib prefix="billingtag" uri="/WEB-INF/view/module/mohbilling/taglibs/billingtag.tld"%>

<%@ include file="templates/mohBillingLocalHeader.jsp"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<script type="text/javascript" language="JavaScript">
	var $bill = jQuery.noConflict();

	$bill(document).ready(function() {
		$bill('.meta').hide();
		
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
		 <li>
			<a href="recettes.form"><spring:message code="@MODULE_ID@.billing.revenue"/></a>
		</li>
		</openmrs:hasPrivilege>
		
		 <openmrs:hasPrivilege privilege="Billing Reports - View Invoice">
		<li  class="<c:if test='<%= request.getRequestURI().contains("Invoice")%>'>active</c:if>">
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
	<form method="post" action="invoice.form">
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
				<td>Bill creator :</td>
				<td><openmrs_tag:userField formFieldName="cashCollector"
						initialValue="${cashCollector}" roles="Cashier;Chief Cashier" /></td>
			</tr>

			<tr>
				<td>Insurance:</td>
				<td><select name="insurance">
						<option selected="selected" value="${insurance.insuranceId}">
							<c:choose>
								<c:when test="${insurance!=null}">${insurance.name}</c:when>
								<c:otherwise>--Select insurance--</c:otherwise>
							</c:choose>
						</option>
						<c:forEach items="${allInsurances}" var="ins">
							<option value="${ins.insuranceId}">${ins.name}</option>
						</c:forEach>
				</select></td>
				

			</tr>

			<tr>
				<td>Patient</td>
				<td><openmrs_tag:patientField formFieldName="patientId"
						initialValue="${patientId}" /></td>
				
			</tr>

		</table>
		<input type="submit" value="Search" id="submitId" />
	</form>
	

</div>
<br />

<br />
<div id="display_bill">
<c:if test="${fn:length(patientInvoiceList)!=0}">
	<div class="box">
<b class="boxHeader">Health Cares Received
	<!--<div style="float:right"><a href="invoice.form?print=true${prmtrs}"><b style="color: white;font-size: 14px;">PDF</b></a></div>  -->
</b>
	
		<table width="70%" border="0">
		<!-- 
		<tr>
		<td>
		   form elements for csv exportation   
			<form action="invoice.form?print=true${prmtrs}"  method="post">
			<input type="submit" value="PDF"/>
			</form>
	    end hidden form
		</td>
		</tr>
		-->
         
			<tr>
				<th class="columnHeader">Bill Id
				</th>
				<th class="columnHeader">Card No
				</th>
				<th class="columnHeader">Patient
				</th>
				<th class="columnHeader">Total 100%
				</th>
				<th class="columnHeader">Insurance Cost
				</th>
				<th class="columnHeader">Patient Due
				</th>
				<th class="columnHeader">View Bill
				</th>
			</tr>
			

			<c:forEach var="patientInvoice" items="${patientInvoiceList}"> 
		 			 <c:set var="patientBillId" value="${patientInvoice.patientBill.patientBillId}"/>
		             <c:set var="cardNumber" value="${patientInvoice.patientBill.beneficiary.insurancePolicy.insuranceCardNo}" />
		             <c:set var="patient" value="${patientInvoice.patientBill.beneficiary.patient}" />
		             <c:set var="total" value="${patientInvoice.totalAmount}" /> 
		             <c:set var="insuranceCost" value="${patientInvoice.insuranceCost}" />
		             <c:set var="patientCost" value="${patientInvoice.patientCost}" />

				<tr>
					<td>${patientBillId}</td>
					<td>${cardNumber}</td>
					<td>${patient.familyName} ${patient.givenName}</td>
					<td>${total}</td>					
					<td>${insuranceCost}</td>
					<td>${patientCost}</td>
					<td><a href="invoice.form?billId=${patientBillId}">View</a></td>
				</tr>
			</c:forEach>

		</table>
	</div>
</c:if>
</div>

<c:if test="${not empty patientInvoice}">
					<c:set var="patientBill" value="${patientInvoice.patientBill}" />
		             <c:set var="cardNumber" value="${patientInvoice.patientBill.beneficiary.insurancePolicy.insuranceCardNo}" />
		             <c:set var="patient" value="${patientInvoice.patientBill.beneficiary.patient}" />
		             <c:set var="total" value="${patientInvoice.totalAmount}" /> 
		             <c:set var="insuranceCost" value="${patientInvoice.insuranceCost}" />
		             <c:set var="patientCost" value="${patientInvoice.patientCost}" />
		              <c:set var="invoiceMap" value="${patientInvoice.invoiceMap}" />
		               <c:set var="insurance" value="${patientInvoice.patientBill.beneficiary.insurancePolicy.insurance.name}" />

<div>		  
<a href="invoice.form?patientBillId=${patientInvoice.patientBill.patientBillId}">PDF</a>	              
<div class="box">
<table>
<tr><td>Names:</td><td>${patient.familyName} ${patient.givenName}</td></tr>
<tr><td>Insurance:</td><td>${insurance}</td></tr>
<tr><td>Card No:</td><td>${cardNumber}</td></tr>
</table>
</div>
<br>
<div class="box">
<table>
		<tr>
				<th class="columnHeader">Recording Date
				</th>
				<th class="columnHeader">Libelle
				</th>
				<th class="columnHeader">Unit Cost
				</th>
				<th class="columnHeader">Qty
				</th>
				<th class="columnHeader">Total100%
				</th>
			</tr>

						
					 <c:forEach var="invoiceMap" items="${invoiceMap}">  
					   <c:set var="invoice" value="${invoiceMap.value}" />
					    <c:set var="consommationList" value="${invoice.consommationList}" />
					    
					    <c:if test="${fn:length(consommationList)!=0}">
					   <tr>
					   <td></td>
					   <td><b>${invoiceMap.key}</b></td>
					   <td></td>
					   <td></td>
					   <td></td>
					   </tr>
	
                        <c:forEach var="cons" items="${consommationList}">
                        <tr> 
                          <td><fmt:formatDate pattern="yyyy-MM-dd" value="${cons.recordDate}" /></td>
                          <td>${cons.libelle }</td>
                          <td>${cons.unitCost }</td>
                          <td>${cons.quantity }</td>
                          <td>${cons.cost }</td>
                          </tr>
                        </c:forEach>
                        
                        
                     <tr>
					   <td></td><td></td><td></td><td></td>
                       <td><b>${invoice.subTotal }</b></td>
					</tr>
					</c:if>
                     </c:forEach>         
					<tr>
					<td></td><td><b>TOTAL FACTURE</</b></td><td></td><td></td>
					<td><b>${patientInvoice.totalAmount }</b></td>
					</tr>
				
</table>
</div>
</div>
</c:if>
<%@ include file="/WEB-INF/template/footer.jsp"%>