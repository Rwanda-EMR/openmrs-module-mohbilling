<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/headerFull.jsp"%>
<openmrs:require privilege="Manage Patient Bill Calculations" otherwise="/login.htm" redirect="/module/mohbilling/patientBillPayment.form" />
<openmrs:require privilege="Create Bill Manually" otherwise="/login.htm" redirect="/module/@MODULE_ID@/admission.form" />

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<script type="text/javascript">
			var $j = jQuery.noConflict();
</script>
<script>
	function loadBillableServiceByCategory(serviceCategoryId,departmentId){
			$j("#serviceCategory_"+serviceCategoryId).load("billableServiceByServiceCategory.list?serviceCategoryId="+serviceCategoryId);
	}

	var index=0;

	function addServiceToCart(serviceId, serviceName, servicePrice){
      //alert(servicePrice);
		var isTheServiceExists=checkIfTheServiceAlreadyInTheList(serviceId);

		if(isTheServiceExists)
			return;

		var table = document.getElementById("cartOfServices");

	    var rowCount = table.rows.length;
	    var row = table.insertRow(rowCount);
	    row.style.verticalAlign="top";

	    var cell1 = row.insertCell(0);
	    cell1.innerHTML="<span id='row_"+index+"'>"+(index+1)+".</span>";

	    var cell2 = row.insertCell(1);
	    cell2.innerHTML=serviceName;

	    var cell3 = row.insertCell(2);
	    cell3.innerHTML="<input type='text' size='3' name='quantity_"+index+"' id='quantity_"+index+"' style='text-align: center;' value='1' onblur='calculateTheBill();'/>";

        //Drug Frequency
        var cell7 = row.insertCell(3);
        cell7.innerHTML="<input type='text' size='8' name='frequency_"+index+"' id='frequency_"+index+"' style='text-align: left;' value=''/>";

	    var cell4 = row.insertCell(4);
	    cell4.style.textAlign="right";
	    cell4.innerHTML="<span id='price_"+index+"'><b>"+servicePrice+"</b></span>";
	    var price=createHiddenElement('servicePrice',index,5);
	    price.value=servicePrice;
	    cell4.appendChild(price);

	    var cell5 = row.insertCell(5);
	    cell5.innerHTML="<span title='Remove Service' onclick=deleteRow('"+serviceId+"') class='deleteBt' id='delete_"+index+"'><b>X</b></span>";

	    var cell6 = row.insertCell(6);
	    cell6.innerHTML="<input type='hidden' size='5' name='billableServiceId_"+index+"' id='billableServiceId_"+index+"' value='"+serviceId+"'/>";

	    index++;

	    calculateTheBill();

	    $j("#billableService_"+serviceId).removeClass("unselectedService");
	    $j("#billableService_"+serviceId).addClass("selectedService");

	    recountServiceInTheCart();
	}

	function recountServiceInTheCart(){
		var table = document.getElementById("cartOfServices");

	    var rowCount = table.rows.length;
	    var counter=0;
	    for(var i=1; i<rowCount; i++){
		    for(var j=counter;j<index;j++){
			    if(null!=document.getElementById("row_"+j)){
			    	document.getElementById("row_"+j).innerHTML=i+".";
			    	counter=(j+1);
			    	break;
				}

			}
		}
	}

	function checkIfTheServiceAlreadyInTheList(serviceId){
		try {
		    var table = document.getElementById("cartOfServices");

		    var rowCount = table.rows.length;

		    for(var i=1; i<rowCount; i++) {
		        var row = table.rows[i];

		        if(null!=row){
			        var hiddenElmnt=row.cells[5].childNodes[0];
			        if(null != hiddenElmnt && serviceId == hiddenElmnt.value) {
				    	return true;
			        }
		        }

		    }

		    return false;

	    }catch(e) {
	        alert(e);
	    }
	}

	function createHiddenElement(name,index,size){
		var element = document.createElement("input");
	    element.type = "hidden";
	    element.name = name+"_"+index;
	    element.id = name+"_"+index;
	    element.size = size;
	    return element;
	}

	function deleteRow(serviceId) {
        if(confirm("Are you sure you want to remove selected service?")){
		    try {
			    var table = document.getElementById("cartOfServices");

			    var rowCount = table.rows.length;

			    for(var i=1; i<rowCount; i++) {
			        var row = table.rows[i];

			        var hiddenElmnt=row.cells[6].childNodes[0];
			        if(null != hiddenElmnt && serviceId == hiddenElmnt.value) {
				    	table.deleteRow(i);
			            rowCount--;
			            i--;

			            $j("#billableService_"+serviceId).removeClass("selectedService");
			    	    $j("#billableService_"+serviceId).addClass("unselectedService");
			        }

			    }

			    calculateTheBill();

			    recountServiceInTheCart();

		    }catch(e) {
		        alert(e);
		    }
    	}
	}

	function calculateTheBill(){
		try {
			var bill=0.00;
			var j=0;

		    while(j<index){
			   	if(document.getElementById("servicePrice_"+j)!=null && document.getElementById("servicePrice_"+j)!="undefined"){
					var price=parseFloat(document.getElementById("servicePrice_"+j).value);
					bill+=(price*parseFloat(document.getElementById("quantity_"+j).value));
			   	}

				j++;
			}

		    document.getElementById("pBill").innerHTML=bill.toFixed(2)+" RWF";
		    $j("#totalAmount").val(bill.toFixed(2));
	    }catch(e) {
	        alert(e);
	    }
	}

	function  savePatientBill(){
		if(confirm("Are you sure you want to save?")){

			//set the number of services which has been clicked
			$j("#numberOfServicesClicked").val(index);

			//submit the patient bill form
			document.getElementById("form_save_patient_bill").submit();
		}
	}

	function  cancelPatientBillCalculation(){
		if(confirm("Are you sure you want to Cancel?"))
			document.getElementById("form_cancel").submit();
	}

</script>

<!-- Hide submit button when the user is adding items on existing consommation -->
<script type="text/javascript">
$j(document).ready(function(){
		  var dep = $j('#selectedDepId').val();
		  var sCategories = $j('#categories').length;
		  if(dep>0&&sCategories!=0){
		   $j('#depSubmitBtn').hide();
		 <!-- $j('#selectedDepIdx').html("(In case, you need to add item to the saved consommation,Please!!! Add 1 item at once)");-->
		   $j('#departments').attr('disabled', true);
	      }
		  else{
			  $j('#depSubmitBtn').show();
			   $j('#departments').attr('disabled', false);
		  }
});
</script>


<style>
	.deleteBt{
		color: #FFFFFF;
		background-color: red;
		padding: 2px;
		cursor: pointer;
		-moz-border-radius: 2px;
		border-right: 2px solid #dddddd;
		border-bottom: 2px solid #dddddd;
	}

	.selectedService{
		background-color: #56CC81;
	}
	#patientTabs{
	margin-top: 10px;
		margin-left: auto;
		margin-right: auto;
		padding-top: 5px;
		border-top: 1px solid #dddddd;
	}

</style>

<%@ include file="templates/mohBillingBillHeader.jsp"%>
<h2><spring:message code="@MODULE_ID@.billing.calculation"/></h2>

<%@ include file="templates/mohBillingInsurancePolicySummaryForm.jsp"%>

<br/>
<c:if test="${editStr==null }">
<div id="billingForm">
<div class="box">
	<div style="float: left; width: 40%">
		<b class="boxHeader">Calculator</b>
		<div class="box">
			<form action="billing.form?insurancePolicyId=${param.insurancePolicyId}&ipCardNumber=${param.ipCardNumber}&globalBillId=${param.globalBillId}&departmentId=${param.departmentId }&save=true" method="post" id="form_save_patient_bill">
			<input type="hidden" name="consomationToAddOn" value="${addNew.consommationId}"/>
				<div style="max-width: 99%; overflow: auto;">
					<table width="99%; !important;" id="cartOfServices">
						<tr>
							<td class="columnHeader" style="width: 8%;"></td>
							<td class="columnHeader" style="width: 60%;">Services</td>
							<td class="columnHeader" style="width: 8%;">Qty</td>
							<td class="columnHeader" style="width: 8%;">Drug Frequency</td>
							<td class="columnHeader" style="width: 17%;">Price (Rwf)</td>
							<td class="columnHeader" style="width: 3%;"></td>
							<td style="width: 1%"></td>
						</tr>
					</table>
					<br/>
					<div>
						<table width="99%">
							<tr>
								<td class="columnHeader" style="width: 49%; font-size: 14px;"><b>Total</b></td>
								<td class="columnHeader" style="width: 49%; text-align: right; font-size: 14px;"><input type="hidden" value="" name="totalAmount" id="totalAmount"/><b id="pBill"> 0 RWF</b></td>
							</tr>
						</table>
					</div>
					<br/>

					<input type="hidden" name="numberOfServicesClicked" id="numberOfServicesClicked" value=""/>
				</div>

			</form>

			<table width="99%">
				<tr>
					<td style="width: 49%;">
						<input type="button" value="Save" onclick="savePatientBill();"/>
					</td>
					<td style="width: 49%; text-align: right;">
						<form action="patientSearchBill.form" method="post" id="form_cancel">
							<input type="button" value="Cancel" onclick="cancelPatientBillCalculation();"/>
						</form>
					</td>
				</tr>
			</table>

		</div>
	</div>

	<div style="float: right; width: 60%">
		<b class="boxHeader">Search Services by Department</b>
<div class="box">
	<form
		action="billing.form?insurancePolicyId=${param.insurancePolicyId}&ipCardNumber=${param.ipCardNumber}&globalBillId=${param.globalBillId}&searchDpt=true"	method="post" id="depSearchForm">

		<table>
			<tr>
				<td><select name="departmentId" id="departments">
						<!-- <option value="">--select--</option> -->
						<c:forEach items="${departments }" var="dep">
						<option value="${dep.departmentId}" ${dep.departmentId == param.departmentId ? 'selected':''}>${dep.name }</option>
						</c:forEach>
				</select>
				</td>
				<!-- <td><p align="center" style="color: red;font-weight: bold; " id="selectedDepIdx"></p></td> -->
				<td><input type="submit" value="search services" id="depSubmitBtn"/></td>
			</tr>
		</table>
		<input type="hidden" id="selectedDepId" value="${param.departmentId}"/>
	</form>
</div>
</div>
		<c:if test="${param.departmentId !=null}">
		<div>
			<div id="patientTabs">
				<ul>
					<c:forEach items="${categories}" var="serviceCategory" varStatus="status">

						<li><a hidefocus="hidefocus" onclick="return changeTab(this);" href="#" id="serviceCategory_${serviceCategory.serviceCategoryId}Tab" class="${(status.count==1)?'current':''} ">${serviceCategory.name}</a></li>
				     <input type="hidden" value="${categories}" id="categories"/>
					</c:forEach>
				</ul>
			</div>

			<c:forEach items="${categories}" var="sc" varStatus="counter">

				<div id="serviceCategory_${sc.serviceCategoryId}" <c:if test='${counter.count>1}'>style='display: none;'</c:if>>
					<script>
						loadBillableServiceByCategory("${sc.serviceCategoryId}");
					</script>
				</div>
			</c:forEach>
		</div>
	 </c:if>
	</div>
	<div style="clear: both;"></div>
	</div>
</c:if>
<c:if test="${consommation != null }">
<c:import url="mohBillingConsommationItemList.jsp"/>
</c:if>



<script type="text/javascript">

	function changeTab(tabObj) {
		if (!document.getElementById || !document.createTextNode) {return;}
		if (typeof tabObj == "string")
			tabObj = document.getElementById(tabObj);

		if (tabObj) {
			var tabs = tabObj.parentNode.parentNode.getElementsByTagName('a');
			for (var i=0; i<tabs.length; i++) {
				if (tabs[i].className.indexOf('current') != -1) {
					manipulateClass('remove', tabs[i], 'current');
				}
				var divId = tabs[i].id.substring(0, tabs[i].id.lastIndexOf("Tab"));
				var divObj = document.getElementById(divId);
				if (divObj) {
					if (tabs[i].id == tabObj.id)
						divObj.style.display = "";
					else
						divObj.style.display = "none";
				}
			}
			addClass(tabObj, 'current');

			setTabCookie(tabObj.id);
		}
		return false;
	}

	function setTabCookie(value) {
		document.cookie = value;
	}
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>