<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/view/module/mohbilling/templates/header.jsp"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<openmrs:require privilege="Billing Proforma - Create" otherwise="/login.htm" redirect="/module/@MODULE_ID@/proforma.form" />
<%@ include file="templates/mohBillingBillHeader.jsp"%>

<h2>New Proforma Invoice</h2>

<style>
	.proforma-box td { padding: 4px 8px 4px 0; vertical-align: top; }
	.proforma-box input[type=text], .proforma-box select, .proforma-box textarea { width: 320px; }
	#proformaItems td, #proformaItems th { padding: 4px 6px; }
	#proformaItems .num { text-align: right; }
	.proforma-totals td { font-weight: bold; text-align: right; padding: 3px 6px; }
	.proforma-note { color: #8a6d3b; background: #fcf8e3; border: 1px solid #faebcc; padding: 6px; margin: 8px 0; }
</style>

<b class="boxHeader">Step 1 &gt;&gt; Patient and insurance</b>
<div class="box proforma-box">
	<form method="get" action="proforma.form">
		<table>
			<tr>
				<td>Patient *</td>
				<td><openmrs_tag:patientField formFieldName="patientId" initialValue="${patient.patientId}" /></td>
			</tr>
			<tr>
				<td>Price as insurance *</td>
				<td>
					<select name="insuranceId">
						<option value="">-- Select insurance --</option>
						<c:forEach items="${insurances}" var="ins">
							<option value="${ins.insuranceId}" <c:if test="${ins.insuranceId == insurance.insuranceId}">selected="selected"</c:if>><c:out value="${ins.name}" /></option>
						</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td>Department</td>
				<td>
					<select name="departmentId">
						<option value="">-- Any --</option>
						<c:forEach items="${departments}" var="dep">
							<option value="${dep.departmentId}" <c:if test="${dep.departmentId == department.departmentId}">selected="selected"</c:if>><c:out value="${dep.name}" /></option>
						</c:forEach>
					</select>
				</td>
			</tr>
			<tr>
				<td></td>
				<td><input type="submit" value="Load services" /></td>
			</tr>
		</table>
	</form>
	<c:if test="${not empty insurance}">
		<div class="proforma-note">Changing the patient or insurance reloads the page and clears the services added below.</div>
	</c:if>
</div>

<c:if test="${not empty insurance && not empty patient}">
<br />
<b class="boxHeader">Step 2 &gt;&gt; Services (tariff: <c:out value="${insurance.name}" />,
	<c:choose>
		<c:when test="${not empty flatFee && flatFee > 0}">patient flat fee <fmt:formatNumber value="${flatFee}" pattern="#,##0.00" /></c:when>
		<c:otherwise>insurance pays ${empty insuranceRate ? 0 : insuranceRate}%</c:otherwise>
	</c:choose>)</b>
<div class="box proforma-box">
	<form method="post" action="proforma.form" id="proformaForm" onsubmit="return validateProforma();">
		<input type="hidden" name="patientId" value="${patient.patientId}" />
		<input type="hidden" name="insuranceId" value="${insurance.insuranceId}" />
		<input type="hidden" name="departmentId" value="${department.departmentId}" />

		<table>
			<tr>
				<td>Search service</td>
				<td><input type="text" id="serviceFilter" placeholder="Type to filter by name or category" onkeyup="filterServices()" /></td>
			</tr>
			<tr>
				<td>Service</td>
				<td>
					<select id="servicePicker" size="8" style="width: 520px;">
						<c:forEach items="${services}" var="bs">
							<option value="${bs.serviceId}" data-price="${bs.maximaToPay}"
									data-category="<c:out value='${bs.serviceCategory.name}' />"
									data-name="<c:out value='${bs.facilityServicePrice.name}' />"><c:out value="${bs.serviceCategory.name}" /> - <c:out value="${bs.facilityServicePrice.name}" /> (<fmt:formatNumber value="${bs.maximaToPay}" pattern="#,##0.00" />)</option>
						</c:forEach>
					</select>
					<c:if test="${empty services}"><div class="error">No active services are priced for this insurance.</div></c:if>
				</td>
			</tr>
			<tr>
				<td>Quantity</td>
				<td><input type="number" id="serviceQty" value="1" min="0.01" step="any" style="width: 80px;" />
					<input type="button" value="Add service" onclick="addService()" /></td>
			</tr>
		</table>

		<table id="proformaItems" class="list" width="100%" style="margin-top: 10px;">
			<thead>
				<tr>
					<th class="columnHeader">Category</th>
					<th class="columnHeader">Service</th>
					<th class="columnHeader">Unit price</th>
					<th class="columnHeader">Quantity</th>
					<th class="columnHeader">Amount</th>
					<th class="columnHeader"></th>
				</tr>
			</thead>
			<tbody></tbody>
		</table>
		<table class="proforma-totals" width="100%">
			<tr><td width="80%">TOTAL (100%)</td><td id="totalAmount">0.00</td></tr>
			<tr><td>INSURANCE SHARE</td><td id="insuranceAmount">0.00</td></tr>
			<tr><td>PATIENT SHARE</td><td id="patientAmount">0.00</td></tr>
		</table>

		<table>
			<tr>
				<td>Addressed to</td>
				<td><input type="text" name="addressee" maxlength="255" value="<c:out value='${addressee}' />" placeholder="Supporting entity (optional)" /></td>
			</tr>
			<tr>
				<td>Notes</td>
				<td><textarea name="notes" rows="3"><c:out value="${notes}" /></textarea></td>
			</tr>
			<tr>
				<td></td>
				<td><input type="submit" name="save" value="Save proforma" /></td>
			</tr>
		</table>
	</form>
</div>

<script type="text/javascript">
	var proformaRate = ${empty insuranceRate ? 0 : insuranceRate};
	var proformaFlatFee = ${empty flatFee ? 0 : flatFee};

	function formatAmount(v) {
		return v.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",");
	}

	function filterServices() {
		var term = document.getElementById('serviceFilter').value.toLowerCase();
		var options = document.getElementById('servicePicker').options;
		for (var i = 0; i < options.length; i++) {
			options[i].style.display = options[i].text.toLowerCase().indexOf(term) >= 0 ? '' : 'none';
		}
	}

	function cell(text, cls) {
		var td = document.createElement('td');
		td.appendChild(document.createTextNode(text));
		if (cls) td.className = cls;
		return td;
	}

	function hidden(name, value) {
		var input = document.createElement('input');
		input.type = 'hidden';
		input.name = name;
		input.value = value;
		return input;
	}

	function addService() {
		var picker = document.getElementById('servicePicker');
		var opt = picker.options[picker.selectedIndex];
		var qty = parseFloat(document.getElementById('serviceQty').value);
		if (!opt) { alert('Select a service first.'); return; }
		if (isNaN(qty) || qty <= 0) { alert('Enter a quantity greater than zero.'); return; }
		var price = parseFloat(opt.getAttribute('data-price'));

		var tr = document.createElement('tr');
		tr.setAttribute('data-amount', (price * qty).toString());
		tr.appendChild(cell(opt.getAttribute('data-category')));
		tr.appendChild(cell(opt.getAttribute('data-name')));
		tr.appendChild(cell(formatAmount(price), 'num'));
		tr.appendChild(cell(qty.toString(), 'num'));
		tr.appendChild(cell(formatAmount(price * qty), 'num'));
		var actions = document.createElement('td');
		actions.appendChild(hidden('serviceId', opt.value));
		actions.appendChild(hidden('quantity', qty));
		var remove = document.createElement('a');
		remove.href = '#';
		remove.appendChild(document.createTextNode('Remove'));
		remove.onclick = function() { tr.parentNode.removeChild(tr); updateTotals(); return false; };
		actions.appendChild(remove);
		tr.appendChild(actions);
		document.querySelector('#proformaItems tbody').appendChild(tr);

		document.getElementById('serviceQty').value = 1;
		updateTotals();
	}

	function updateTotals() {
		var rows = document.querySelectorAll('#proformaItems tbody tr');
		var total = 0;
		for (var i = 0; i < rows.length; i++) {
			total += Math.round(parseFloat(rows[i].getAttribute('data-amount')) * 100) / 100;
		}
		var insurance, patient;
		if (proformaFlatFee > 0) {
			patient = Math.min(proformaFlatFee, total);
			insurance = total - patient;
		} else {
			insurance = Math.round(total * proformaRate) / 100;
			patient = total - insurance;
		}
		document.getElementById('totalAmount').innerHTML = formatAmount(total);
		document.getElementById('insuranceAmount').innerHTML = formatAmount(insurance);
		document.getElementById('patientAmount').innerHTML = formatAmount(patient);
	}

	function validateProforma() {
		if (document.querySelectorAll('#proformaItems tbody tr').length == 0) {
			alert('Add at least one service.');
			return false;
		}
		return true;
	}
</script>
</c:if>

<c:if test="${empty insurance || empty patient}">
	<p>Select a patient and the insurance whose tariff should be used, then click <b>Load services</b>.</p>
</c:if>

<%@ include file="/WEB-INF/view/module/mohbilling/templates/footer.jsp"%>
