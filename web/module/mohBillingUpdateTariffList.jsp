<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />
<openmrs:require privilege="Manage Patient Bill Calculations" otherwise="/login.htm" redirect="/module/@MODULE_ID@/patientBillPayment.form" />


<br/>

<div class="box">
	<div style="float: left; width: 29%">
		<b class="boxHeader">Calculator</b>
		<div class="box">
			<form action="billing.form?insurancePolicyId=${param.insurancePolicyId}&ipCardNumber=${param.ipCardNumber}&save=true" method="post" id="form_save_patient_bill">
				<div style="max-width: 99%; overflow: auto;">

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

				
			</form>
			
			<table width="99%">
				<h3>File Upload:</h3>
                  Select a file to upload: <br />
                 <form action="UploadServlet" method="post" enctype="multipart/form-data">
                   <input type="file" name="file" size="50" />
                      <br />
                   <input type="submit" value="Upload File" />
                </form>
			</table>
			
		</div>
	</div>

	<div style="clear: both;"></div>
</div>



<%@ include file="/WEB-INF/template/footer.jsp"%>
