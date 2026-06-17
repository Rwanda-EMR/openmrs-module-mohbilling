<%@ tag language="java" pageEncoding="UTF-8"%>

<%@ attribute name="redirectUrl" required="true" type="java.lang.String" %>

<script type="text/javascript">
	function beneficiaryListInTable(item,id){
		var box = document.getElementById("resultOfSearch");
		if (item.value == null || item.value.length < '${minSearchCharacters}'){
			box.innerHTML = "";
			return;
		}

		var request = new XMLHttpRequest();
		request.onreadystatechange = function() {
			if (request.readyState == 4 && request.status == 200) {
				box.innerHTML = request.responseText;
			}
		};
		request.open("GET", "<%= request.getContextPath()%>/module/mohbilling/beneficiarySearch.form?searchString=" + encodeURIComponent(item.value), true);
		request.send(null);
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
