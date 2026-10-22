<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/view/module/mohbilling/templates/header.jsp"%>
<openmrs:require privilege="Billing Report - View Bill Report" otherwise="/login.htm" redirect="/module/@MODULE_ID@/billReport.form" />
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingReportHeader.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<style>
.facilityHeaderBox td {
	padding-right: 18px;
	padding-bottom: 6px;
}
.facility-setting-value {
	min-width: 80px;
	display: inline-block;
	cursor: pointer;
	color: #0645ad;
	border-bottom: 1px dotted #0645ad;
	padding: 0 2px;
}
.facility-setting-value.empty {
	color: #999;
	font-style: italic;
}
.facility-setting-value.saving {
	opacity: 0.5;
}
</style>

<script type="text/javascript" language="JavaScript">
	var $bill = jQuery.noConflict();

	function displayFacilityValue(value) {
		if (!value || $bill.trim(value) === '') {
			return '(click to set)';
		}
		return value;
	}

	function editFacilitySetting(span) {
		var $el = $bill(span);
		if ($el.hasClass('saving')) {
			return;
		}
		var fieldKey = $el.attr('data-field');
		var label = $el.attr('data-label');
		var current = $el.attr('data-value') || '';
		var entered = window.prompt('Set ' + label + ':', current);
		if (entered === null) {
			return;
		}
		entered = $bill.trim(entered);
		$el.addClass('saving');
		$bill.ajax({
			url: 'billReport.form',
			type: 'POST',
			dataType: 'json',
			data: {
				ajaxSaveFacilitySetting: 'true',
				fieldKey: fieldKey,
				value: entered
			},
			success: function(resp) {
				if (resp && resp.success) {
					var saved = resp.value || '';
					$el.attr('data-value', saved);
					$el.text(displayFacilityValue(saved));
					if (saved === '') {
						$el.addClass('empty');
					} else {
						$el.removeClass('empty');
					}
				} else {
					alert((resp && resp.message) ? resp.message : 'Unable to save facility setting.');
				}
			},
			error: function(xhr) {
				var msg = 'Unable to save facility setting.';
				try {
					var parsed = $bill.parseJSON(xhr.responseText);
					if (parsed && parsed.message) {
						msg = parsed.message;
					}
				} catch (e) {}
				alert(msg);
			},
			complete: function() {
				$el.removeClass('saving');
			}
		});
	}

	$bill(document).ready(function(){
		$bill('#submitId').click(function() {
			$bill('#formStatusId').val("clicked");
		});
		$bill('.facility-setting-value').each(function() {
			var $el = $bill(this);
			var current = $el.attr('data-value') || '';
			$el.text(displayFacilityValue(current));
			if (current === '') {
				$el.addClass('empty');
			}
		}).click(function() {
			editFacilitySetting(this);
		});
	});
</script>

<h2>Bill Report</h2>

<b class="boxHeader">Search Form</b>
<div class="box">
	<form method="post" action="">
		<input type="hidden" name="formStatus" id="formStatusId" value="" />
		<table>
			<tr>
				<td>Insurance:</td>
				<td>
					<select name="insuranceId">
						<option value="">-- All insurances --</option>
						<c:forEach items="${insurances}" var="insurance">
							<option value="${insurance.insuranceId}"
								${insurance.insuranceId == selectedInsuranceId ? 'selected="selected"' : ''}>${insurance.name}</option>
						</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td>Start Date:</td>
				<td>
					<input type="text" size="11" value="${startDate}" name="startDate" onclick="showCalendar(this)" />
				</td>
			</tr>
			<tr>
				<td>End Date:</td>
				<td>
					<input type="text" size="11" value="${endDate}" name="endDate" onclick="showCalendar(this)" />
				</td>
			</tr>
		</table>
		<br/>
		<input type="submit" value="Search" id="submitId" />
	</form>
</div>

<c:if test="${not empty facilityHeader}">
<br/>
<b class="boxHeader">Facility settings</b>
<div class="box">
	<table class="facilityHeaderBox">
		<tr>
			<td><b>HF CODE:</b>
				<span class="facility-setting-value" data-field="hfCode" data-label="HF CODE"
					data-value="<c:out value='${facilityHeader.hfCode}'/>"></span>
			</td>
			<td><b>Facility:</b> <c:out value="${facilityHeader.facilityName}"/></td>
			<td><b>Type:</b>
				<span class="facility-setting-value" data-field="facilityType" data-label="Type"
					data-value="<c:out value='${facilityHeader.facilityType}'/>"></span>
			</td>
		</tr>
		<tr>
			<td><b>Category:</b>
				<span class="facility-setting-value" data-field="facilityCategory" data-label="Category"
					data-value="<c:out value='${facilityHeader.facilityCategory}'/>"></span>
			</td>
			<td><b>Level of Care:</b>
				<span class="facility-setting-value" data-field="facilityLevelOfCare" data-label="Level of Care"
					data-value="<c:out value='${facilityHeader.facilityLevelOfCare}'/>"></span>
			</td>
			<td><b>Ownership:</b>
				<span class="facility-setting-value" data-field="facilityOwnershipType" data-label="Ownership"
					data-value="<c:out value='${facilityHeader.facilityOwnershipType}'/>"></span>
			</td>
		</tr>
		<tr>
			<td><b>Province:</b> <c:out value="${facilityHeader.provinceName}"/></td>
			<td><b>District:</b> <c:out value="${facilityHeader.districtName}"/></td>
			<td><b>Sector:</b> <c:out value="${facilityHeader.sectorName}"/></td>
		</tr>
	</table>
</div>
</c:if>

<c:if test="${empty admissions && not empty resultMsg}">
	<div style="text-align: center;color: red;"><p>No admissions found from ${startDate} to ${endDate}.</p></div>
</c:if>

<c:if test="${empty admissions && empty resultMsg}">
	<div style="text-align: center;color: #666;"><p>Select insurance (optional), start date and end date, then Search.</p></div>
</c:if>

<c:if test="${not empty admissions}">
<br/>
<b class="boxHeader">${resultMsg}</b>
<div class="box" style="overflow-x: auto;">
	<table style="width: 100%">
		<tr>
			<th class="columnHeader">#</th>
			<th class="columnHeader">Admission Date</th>
			<th class="columnHeader">Discharge Date</th>
			<th class="columnHeader">Admission Status</th>
			<th class="columnHeader">PatientId</th>
			<th class="columnHeader">Patient</th>
			<th class="columnHeader">Patient NID</th>
			<th class="columnHeader">House Hold NID</th>
			<th class="columnHeader">Patient Date of Birth</th>
			<th class="columnHeader">Social Registry Level</th>
			<th class="columnHeader">Patient Phone</th>
			<th class="columnHeader">Patient Gender</th>
			<th class="columnHeader">Insurance</th>
			<th class="columnHeader">Card No</th>
			<th class="columnHeader">Voucher ID</th>
			<th class="columnHeader">Provider Name</th>
			<th class="columnHeader">Provider Qualification</th>
			<th class="columnHeader">Provider Phone Number</th>
			<th class="columnHeader">Provider Registration</th>
			<th class="columnHeader">Primary Diagnosis</th>
			<th class="columnHeader">Confirmed By lab/Imaging</th>
			<th class="columnHeader">ICD-11 Code</th>
			<th class="columnHeader">Product Name</th>
			<th class="columnHeader">Product Code</th>
			<th class="columnHeader">Product Type</th>
			<th class="columnHeader">Product Cost</th>
			<th class="columnHeader">Co-Payment</th>
			<th class="columnHeader">RSSB Payment</th>
			<th class="columnHeader">Total Invoice</th>
		</tr>
		<c:forEach items="${admissions}" var="row" varStatus="status">
			<tr>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${status.count}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">
					<fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${row.admission.admissionDate}" />
				</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">
					<fmt:formatDate pattern="dd/MM/yyyy HH:mm" value="${row.dischargeDate}" />
				</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.admissionStatus}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.patientId}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">
					<c:if test="${not empty row.patient}">
						${row.patient.personName.fullName}
					</c:if>
				</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.patientNid}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.householdNid}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">
					<fmt:formatDate pattern="dd/MM/yyyy" value="${row.patientDob}" />
				</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.socialRegistryLevel}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.patientPhone}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.patientGender}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.insuranceName}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.cardNumber}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.voucherId}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.providerName}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.providerQualification}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.providerPhone}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.providerRegistration}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.primaryDiagnosis}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.confirmedByLabImaging}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.icd11Code}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.productName}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.productCode}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.productType}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.productCost}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.coPayment}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.rssbPayment}</td>
				<td class="rowValue ${(status.count%2!=0)?'even':''}">${row.totalInvoice}</td>
			</tr>
		</c:forEach>
	</table>
</div>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>
