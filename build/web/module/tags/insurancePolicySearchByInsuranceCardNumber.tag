<%@ tag language="java" pageEncoding="UTF-8"%>

<%@ attribute name="redirectUrl" required="true" type="java.lang.String" %>

<script src='<%= request.getContextPath()%>/dwr/interface/MOH_BILLING_DWRUtil.js'></script>

<script type="text/javascript">
	function beneficiaryListInTable(item,id){
		//alert(item.value);
			if (item.value != null && item.value.length >= '${minSearchCharacters}'){
				MOH_BILLING_DWRUtil.getBeneficiaryListInTable(item.value, function(ret){
	
					var box = document.getElementById("resultOfSearch");
					box.innerHTML = ret;
				});
			}
	}
	
	function editInsurancePolicy(ipId,ipCardNumber){
		window.location.href="${redirectUrl}?insurancePolicyId="+ipId+"&ipCardNumber="+ipCardNumber;
	}
	
</script>

<b class="boxHeader">Find An Existing Insurance Policy</b>
<div class="box">
	<table>
		<tr>
			<td>Patient Insurance Card Number</td>
			<td><input type="text" style="width:25em" autocomplete="off" value="${insuranceCardNumber}" onKeyUp='javascript:beneficiaryListInTable(this,1,1);' name='insuranceCardNumber' id='insuranceCardNumber'/> <b style="color: red"> ${messageIfNoInsuranceCardNoFound}</b> </td>
		</tr>
	</table>
	
	<div id='resultOfSearch' style="background: whitesmoke; max-height: 400px; font-size:1em;"></div>
		
</div>