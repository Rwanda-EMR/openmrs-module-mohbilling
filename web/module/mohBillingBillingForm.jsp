<%@ include file="/web-inf/template/include.jsp"%>
<%@ include file="/web-inf/template/header.jsp"%>
<openmrs:htmlinclude file="/moduleresources/@module_id@/scripts/jquery-1.3.2.js" />
<openmrs:require privilege="manage patient bill calculations" otherwise="/login.htm" redirect="/module/@module_id@/patientbillpayment.form" />
<script>

	function loadbillableservicebycategory(servicecategoryid){
		$("#servicecategory_"+servicecategoryid).load("billableservicebyservicecategory.list?servicecategoryid="+servicecategoryid);
	}

	var index=0;
	
	function addservicetocart(serviceid, servicename, serviceprice){
      // alert(serviceprice);
		var istheserviceexists=checkiftheservicealreadyinthelist(serviceid);

		if(istheserviceexists)
			return;
		
		var table = document.getelementbyid("cartofservices");
		
	    var rowcount = table.rows.length;
	    var row = table.insertrow(rowcount);
	    row.style.verticalalign="top";

	    var cell1 = row.insertcell(0);
	    cell1.innerhtml="<span id='row_"+index+"'>"+(index+1)+".</span>";

	    var cell2 = row.insertcell(1);
	    cell2.innerhtml=servicename;

	    var cell3 = row.insertcell(2);
	    cell3.innerhtml="<input type='text' size='3' name='quantity_"+index+"' id='quantity_"+index+"' style='text-align: center;' value='1' onblur='calculatethebill();'/>";
	    
	    var cell4 = row.insertcell(3);
	    cell4.style.textalign="right";
	    cell4.innerhtml="<span id='price_"+index+"'><b>"+serviceprice+"</b></span>";
	    var price=createhiddenelement('serviceprice',index,5);
	    price.value=serviceprice;
	    cell4.appendchild(price);
	    
	    var cell5 = row.insertcell(4);
	    cell5.innerhtml="<span title='remove service' onclick=deleterow('"+serviceid+"') class='deletebt' id='delete_"+index+"'><b>x</b></span>";

	    var cell6 = row.insertcell(5);
	    cell6.innerhtml="<input type='hidden' size='5' name='billableserviceid_"+index+"' id='billableserviceid_"+index+"' value='"+serviceid+"'/>";
	    
	    index++;

	    calculatethebill();

	    $("#billableservice_"+serviceid).removeclass("unselectedservice");
	    $("#billableservice_"+serviceid).addclass("selectedservice");

	    recountserviceinthecart();
	}

	function recountserviceinthecart(){
		var table = document.getelementbyid("cartofservices");
	    
	    var rowcount = table.rows.length;
	    var counter=0;
	    for(var i=1; i<rowcount; i++){
		    for(var j=counter;j<index;j++){
			    if(null!=document.getelementbyid("row_"+j)){
			    	document.getelementbyid("row_"+j).innerhtml=i+".";
			    	counter=(j+1);
			    	break;
				}
				    
			}
		}
	}

	function checkiftheservicealreadyinthelist(serviceid){
		try {
		    var table = document.getelementbyid("cartofservices");
		    			    
		    var rowcount = table.rows.length;
			
		    for(var i=1; i<rowcount; i++) {
		        var row = table.rows[i];

		        if(null!=row){
			        var hiddenelmnt=row.cells[5].childnodes[0];
			        if(null != hiddenelmnt && serviceid == hiddenelmnt.value) {
				    	return true;
			        }
		        }

		    }

		    return false;
		    
	    }catch(e) {
	        alert(e);
	    }
	}

	function createhiddenelement(name,index,size){
		var element = document.createelement("input");
	    element.type = "hidden";
	    element.name = name+"_"+index;
	    element.id = name+"_"+index;
	    element.size = size;
	    return element;
	}

	function deleterow(serviceid) {
        if(confirm("are you sure you want to remove selected service?")){
		    try {
			    var table = document.getelementbyid("cartofservices");
			    			    
			    var rowcount = table.rows.length;
				
			    for(var i=1; i<rowcount; i++) {
			        var row = table.rows[i];
			        
			        var hiddenelmnt=row.cells[5].childnodes[0];
			        if(null != hiddenelmnt && serviceid == hiddenelmnt.value) {
				    	table.deleterow(i);
			            rowcount--;
			            i--;

			            $("#billableservice_"+serviceid).removeclass("selectedservice");
			    	    $("#billableservice_"+serviceid).addclass("unselectedservice");
			        }

			    }

			    calculatethebill();

			    recountserviceinthecart();
			    
		    }catch(e) {
		        alert(e);
		    }
    	}
	}

	function calculatethebill(){
		try {
			var bill=0.00;
			var j=0;
			
		    while(j<index){
			   	if(document.getelementbyid("serviceprice_"+j)!=null && document.getelementbyid("serviceprice_"+j)!="undefined"){
					var price=parsefloat(document.getelementbyid("serviceprice_"+j).value);
					bill+=(price*parsefloat(document.getelementbyid("quantity_"+j).value));
			   	}
				
				j++;
			}

		    document.getelementbyid("pbill").innerhtml=bill.tofixed(2)+" rwf";
		    $("#totalamount").val(bill.tofixed(2));
	    }catch(e) {
	        alert(e);
	    }
	}

	function  savepatientbill(){
		if(confirm("are you sure you want to save?")){
			
			//set the number of services which has been clicked
			$("#numberofservicesclicked").val(index);

			//submit the patient bill form
			document.getelementbyid("form_save_patient_bill").submit();
		}
	}

	function  cancelpatientbillcalculation(){
		if(confirm("are you sure you want to cancel?"))
			document.getelementbyid("form_cancel").submit();
	}

</script>

<style>
	.deletebt{
		color: #ffffff;
		background-color: red;
		padding: 2px;
		cursor: pointer;
		-moz-border-radius: 2px; 
		border-right: 2px solid #dddddd;
		border-bottom: 2px solid #dddddd;
	}
	
	.selectedservice{
		background-color: #56cc81;
	}
</style>

<%@ include file="templates/mohbillinglocalheader.jsp"%>


<h2><spring:message code="@module_id@.billing.calculation"/></h2>

<%@ include file="templates/mohbillinginsurancepolicysummaryform.jsp"%>

<br/>

<div class="box">
	<div style="float: left; width: 29%">
		<b class="boxheader">calculator</b>
		<div class="box">
			<form action="billing.form?insurancepolicyid=${param.insurancepolicyid}&ipcardnumber=${param.ipcardnumber}&save=true" method="post" id="form_save_patient_bill">
				<div style="max-width: 99%; overflow: auto;">
					<table width="99%; !important;" id="cartofservices">
						<tr>
							<td class="columnheader" style="width: 5%;"></td>
							<td class="columnheader" style="width: 60%;">services</td>
							<td class="columnheader" style="width: 10%;">qty</td>
							<td class="columnheader" style="width: 19%;">price (rwf)</td>
							<td class="columnheader" style="width: 5%;"></td>
							<td style="width: 1%;"></td>
						</tr>
					</table>
					<br/>
					<div>
						<table width="99%">
							<tr>
								<td class="columnheader" style="width: 49%; font-size: 14px;"><b>total</b></td>
								<td class="columnheader" style="width: 49%; text-align: right; font-size: 14px;"><input type="hidden" value="" name="totalamount" id="totalamount"/><b id="pbill"> 0 rwf</b></td>
							</tr>
						</table>
					</div>
					<br/>
					
					<input type="hidden" name="numberofservicesclicked" id="numberofservicesclicked" value=""/>
				</div>
				
			</form>
			
			<table width="99%">
				<tr>
					<td style="width: 49%;">
						<input type="button" value="save" onclick="savepatientbill();"/>
					</td>
					<td style="width: 49%; text-align: right;">
						<form action="patientsearchbill.form" method="post" id="form_cancel">
							<input type="button" value="cancel" onclick="cancelpatientbillcalculation();"/>
						</form>
					</td>
				</tr>
			</table>
		</div>
	</div>
	<div>
	 <table>
	 <tr>
	 <c:foreach items="${departments}" var="dep" varstatus="status">
			<td><a hidefocus="hidefocus" onclick="return changetab(this);" href="#" id="department_${dep.departmentid}tab" class="${(status.count==1)?'current':''} ">${dep.name}</a></td>
	</c:foreach>
	 </tr>
	 </table>
	</div>
	<div style="float: right; width: 70%">
		<b class="boxheader">services</b>
		<div class="box">
			<div id="patienttabs">
				<ul>
					<c:foreach items="${insurancepolicy.insurance.categories}" var="servicecategory" varstatus="status">
						<li><a hidefocus="hidefocus" onclick="return changetab(this);" href="#" id="servicecategory_${servicecategory.servicecategoryid}tab" class="${(status.count==1)?'current':''} ">${servicecategory.name}</a></li>
					</c:foreach>
				</ul>
			</div>
			
			<c:foreach items="${insurancepolicy.insurance.categories}" var="sc" varstatus="counter">
				<div id="servicecategory_${sc.servicecategoryid}" <c:if test='${counter.count>1}'>style='display: none;'</c:if>>
					<script>
						loadbillableservicebycategory("${sc.servicecategoryid}");
					</script>
				</div>
			</c:foreach>
		</div>
	</div>
	<div style="clear: both;"></div>
</div>

<script type="text/javascript">

	function changetab(tabobj) {
		if (!document.getelementbyid || !document.createtextnode) {return;}
		if (typeof tabobj == "string")
			tabobj = document.getelementbyid(tabobj);
		
		if (tabobj) {
			var tabs = tabobj.parentnode.parentnode.getelementsbytagname('a');
			for (var i=0; i<tabs.length; i++) {
				if (tabs[i].classname.indexof('current') != -1) {
					manipulateclass('remove', tabs[i], 'current');
				}
				var divid = tabs[i].id.substring(0, tabs[i].id.lastindexof("tab"));
				var divobj = document.getelementbyid(divid);
				if (divobj) {
					if (tabs[i].id == tabobj.id)
						divobj.style.display = "";
					else
						divobj.style.display = "none";
				}
			}
			addclass(tabobj, 'current');
			
			settabcookie(tabobj.id);
		}
		return false;
	}
	
</script>

<%@ include file="/web-inf/template/footer.jsp"%>
