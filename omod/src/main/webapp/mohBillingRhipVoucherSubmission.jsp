<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/view/module/mohbilling/templates/header.jsp"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="templates/mohBillingLocalHeader.jsp"%>
<%@ include file="templates/mohBillingAdminHeader.jsp"%>

<h2>RHIP Voucher Submission</h2>

<style>
	.rhip-voucher-filter {
		border: 1px solid #d8d8d8;
		padding: 10px;
		margin: 10px 0 14px 0;
	}
	.rhip-voucher-filter table td {
		padding: 4px 8px 4px 0;
	}
	.rhip-voucher-filter input,
	.rhip-voucher-filter select {
		width: 160px;
	}
	.rhip-voucher-filter .wide {
		width: 280px;
	}
	.rhip-voucher-actions form {
		margin: 0;
	}
	.rhip-voucher-action-group {
		display: flex;
		align-items: center;
		gap: 6px;
	}
	.rhip-voucher-actions input[type=submit],
	.rhip-voucher-open-link {
		box-sizing: border-box;
		min-height: 30px;
		padding: 5px 9px;
		border-radius: 3px;
		font-size: 12px;
		line-height: 18px;
		white-space: nowrap;
	}
	.rhip-voucher-open-link {
		display: inline-block;
		border: 1px solid #1565c0;
		background: #1976d2;
		color: #ffffff;
		text-decoration: none;
	}
	.rhip-voucher-open-link:hover,
	.rhip-voucher-open-link:focus {
		border-color: #0d47a1;
		background: #1565c0;
		color: #ffffff;
		text-decoration: none;
	}
	.rhip-voucher-details {
		display: none;
		background-color: #fafafa;
		border-top: 1px solid #d8d8d8;
		padding: 10px;
	}
	.rhip-voucher-details table {
		margin-bottom: 10px;
	}
	.rhip-voucher-status-SENT {
		color: green;
		font-weight: bold;
	}
	.rhip-voucher-status-FAILED {
		color: #b30000;
		font-weight: bold;
	}
	.rhip-voucher-status-CONFIRMED {
		color: #005eb8;
		font-weight: bold;
	}
	.rhip-voucher-status-PROCESSING {
		color: #8a6d3b;
		font-weight: bold;
	}
	.rhip-voucher-pagination {
		margin: 10px 0;
	}
	.rhip-voucher-pagination a,
	.rhip-voucher-pagination span {
		margin-right: 10px;
	}
</style>

<script type="text/javascript">
	function toggleRhipVoucherDetails(id) {
		var detail = document.getElementById('rhip-voucher-detail-' + id);
		if (detail) {
			detail.style.display = detail.style.display === 'none' || detail.style.display === '' ? 'table-row' : 'none';
		}
	}

	function confirmRhipVoucherSubmission(form) {
		if (!confirm('Are you sure you want to send the RHIP voucher for this global bill?')) {
			return false;
		}
		var buttons = form.getElementsByTagName('input');
		for (var i = 0; i < buttons.length; i++) {
			if (buttons[i].type === 'submit') {
				buttons[i].disabled = true;
				buttons[i].value = 'Processing...';
			}
		}
		return true;
	}

	function showRhipVoucherLoading() {
		var indicator = document.getElementById('rhip-voucher-loading');
		if (indicator) {
			indicator.style.display = 'inline';
		}
	}
</script>

<form method="get" action="rhipVoucherSubmissions.form" class="rhip-voucher-filter" onsubmit="showRhipVoucherLoading();">
	<table>
		<tr>
			<td>Discharge Date</td>
			<td><input type="text" name="dischargeDate" value="${dischargeDate}" placeholder="yyyy-MM-dd" /></td>
			<td>RHIP Submission Status</td>
			<td>
				<select name="status">
					<option value="NOT_SENT" <c:if test="${status eq 'NOT_SENT'}">selected="selected"</c:if>>Not Sent</option>
					<option value="FAILED" <c:if test="${status eq 'FAILED'}">selected="selected"</c:if>>Failed</option>
					<option value="SENT" <c:if test="${status eq 'SENT'}">selected="selected"</c:if>>Sent</option>
					<option value="CONFIRMED" <c:if test="${status eq 'CONFIRMED'}">selected="selected"</c:if>>Confirmed</option>
					<option value="ALL" <c:if test="${status eq 'ALL'}">selected="selected"</c:if>>All</option>
				</select>
			</td>
			<td>Search</td>
			<td><input class="wide" type="text" name="query" value="${query}" /></td>
		</tr>
		<tr>
			<td>Insurance</td>
			<td>
				<select name="insuranceId">
					<option value="">All</option>
					<c:forEach items="${insurances}" var="insurance">
						<option value="${insurance.insuranceId}" <c:if test="${selectedInsuranceId eq insurance.insuranceId}">selected="selected"</c:if>>${insurance.name}</option>
					</c:forEach>
				</select>
			</td>
			<td>Sort By</td>
			<td>
				<select name="sortBy">
					<option value="dischargeDate" <c:if test="${sortBy eq 'dischargeDate'}">selected="selected"</c:if>>Discharge date</option>
					<option value="patientName" <c:if test="${sortBy eq 'patientName'}">selected="selected"</c:if>>Patient name</option>
					<option value="globalBillNumber" <c:if test="${sortBy eq 'globalBillNumber'}">selected="selected"</c:if>>Global bill number</option>
					<option value="totalAmount" <c:if test="${sortBy eq 'totalAmount'}">selected="selected"</c:if>>Total amount</option>
					<option value="status" <c:if test="${sortBy eq 'status'}">selected="selected"</c:if>>RHIP status</option>
				</select>
			</td>
			<td>Direction</td>
			<td>
				<select name="sortDirection">
					<option value="desc" <c:if test="${sortDirection eq 'desc'}">selected="selected"</c:if>>Descending</option>
					<option value="asc" <c:if test="${sortDirection eq 'asc'}">selected="selected"</c:if>>Ascending</option>
				</select>
			</td>
			<td>Page Size</td>
			<td>
				<select name="pageSize">
					<option value="25" <c:if test="${pageSize eq 25}">selected="selected"</c:if>>25</option>
					<option value="50" <c:if test="${pageSize eq 50}">selected="selected"</c:if>>50</option>
					<option value="100" <c:if test="${pageSize eq 100}">selected="selected"</c:if>>100</option>
				</select>
			</td>
		</tr>
		<tr>
			<td colspan="8">
				<input type="submit" value="Filter" />
				<a href="rhipVoucherSubmissions.form">Reset</a>
				<span id="rhip-voucher-loading" style="display: none;">Loading...</span>
			</td>
		</tr>
	</table>
</form>

<p>
	Showing <b>${fn:length(rows)}</b> of <b>${totalCount}</b> discharged global bills.
</p>

<div class="rhip-voucher-pagination">
	<c:choose>
		<c:when test="${hasPreviousPage}">
			<a href="rhipVoucherSubmissions.form?page=${previousPage}&${filterQueryString}">Previous</a>
		</c:when>
		<c:otherwise><span>Previous</span></c:otherwise>
	</c:choose>
	<span>Page ${page} of ${totalPages}</span>
	<c:choose>
		<c:when test="${hasNextPage}">
			<a href="rhipVoucherSubmissions.form?page=${nextPage}&${filterQueryString}">Next</a>
		</c:when>
		<c:otherwise><span>Next</span></c:otherwise>
	</c:choose>
</div>

<c:choose>
	<c:when test="${empty rows}">
		<p>No global bills match the selected filters.</p>
	</c:when>
	<c:otherwise>
		<table cellpadding="3" cellspacing="0" width="100%">
			<thead>
			<tr>
				<th class="columnHeader">Global bill number</th>
				<th class="columnHeader">Patient identifier</th>
				<th class="columnHeader">Patient name</th>
				<th class="columnHeader">Insurance</th>
				<th class="columnHeader">Insurance card number</th>
				<th class="columnHeader">Admission date</th>
				<th class="columnHeader">Discharge date</th>
				<th class="columnHeader">Total bill amount</th>
				<th class="columnHeader">RHIP submission status</th>
				<th class="columnHeader">Last submission date</th>
				<th class="columnHeader">Actions</th>
			</tr>
			</thead>
			<tbody>
			<c:forEach items="${rows}" var="row" varStatus="rowStatus">
				<c:set var="globalBill" value="${row.globalBill}" />
				<c:set var="globalBillId" value="${globalBill.globalBillId}" />
				<tr class="searchRow" onclick="toggleRhipVoucherDetails('${globalBillId}');">
					<td class="rowValue ${(rowStatus.count%2!=0)?'even':''}">${globalBill.billIdentifier}</td>
					<td class="rowValue ${(rowStatus.count%2!=0)?'even':''}">${row.patientIdentifier}</td>
					<td class="rowValue ${(rowStatus.count%2!=0)?'even':''}">${row.patientName}</td>
					<td class="rowValue ${(rowStatus.count%2!=0)?'even':''}">${row.insuranceName}</td>
					<td class="rowValue ${(rowStatus.count%2!=0)?'even':''}">${row.insuranceCardNumber}</td>
					<td class="rowValue ${(rowStatus.count%2!=0)?'even':''}">
						<fmt:formatDate value="${globalBill.admission.admissionDate}" pattern="yyyy-MM-dd HH:mm"/>
					</td>
					<td class="rowValue ${(rowStatus.count%2!=0)?'even':''}">
						<fmt:formatDate value="${globalBill.closingDate}" pattern="yyyy-MM-dd HH:mm"/>
					</td>
					<td class="rowValue right ${(rowStatus.count%2!=0)?'even':''}">
						<fmt:formatNumber value="${globalBill.globalAmount}" type="number" pattern="#,##0.##"/>
					</td>
					<td class="rowValue ${(rowStatus.count%2!=0)?'even':''}">
						<span class="rhip-voucher-status-${row.effectiveStatus}">${row.displayStatus}</span>
						<c:if test="${not empty row.voucherReference}">
							<br />Ref: ${row.voucherReference}
						</c:if>
					</td>
					<td class="rowValue ${(rowStatus.count%2!=0)?'even':''}">
						<fmt:formatDate value="${row.lastSubmissionDate}" pattern="yyyy-MM-dd HH:mm:ss"/>
					</td>
					<td class="rowValue rhip-voucher-actions ${(rowStatus.count%2!=0)?'even':''}" onclick="event.cancelBubble=true;">
						<div class="rhip-voucher-action-group">
						<c:if test="${row.effectiveStatus eq 'NOT_SENT' && canSend}">
							<form method="post" action="rhipVoucherSubmissions.form" onsubmit="return confirmRhipVoucherSubmission(this);">
								<input type="hidden" name="action" value="send" />
								<input type="hidden" name="globalBillId" value="${globalBillId}" />
								<input type="hidden" name="dischargeDate" value="${dischargeDate}" />
								<input type="hidden" name="status" value="${status}" />
								<input type="hidden" name="insuranceId" value="${selectedInsuranceId}" />
								<input type="hidden" name="query" value="${query}" />
								<input type="hidden" name="sortBy" value="${sortBy}" />
								<input type="hidden" name="sortDirection" value="${sortDirection}" />
								<input type="hidden" name="page" value="${page}" />
								<input type="hidden" name="pageSize" value="${pageSize}" />
								<input type="submit" value="Send RHIP Voucher" />
							</form>
						</c:if>
						<c:if test="${row.effectiveStatus eq 'SENT' && row.mmiInsurance && canConfirm}">
							<form method="post" action="rhipVoucherSubmissions.form" onsubmit="return confirmRhipVoucherSubmission(this);">
								<input type="hidden" name="action" value="confirm" />
								<input type="hidden" name="globalBillId" value="${globalBillId}" />
								<input type="hidden" name="dischargeDate" value="${dischargeDate}" />
								<input type="hidden" name="status" value="${status}" />
								<input type="hidden" name="insuranceId" value="${selectedInsuranceId}" />
								<input type="hidden" name="query" value="${query}" />
								<input type="hidden" name="sortBy" value="${sortBy}" />
								<input type="hidden" name="sortDirection" value="${sortDirection}" />
								<input type="hidden" name="page" value="${page}" />
								<input type="hidden" name="pageSize" value="${pageSize}" />
								<input type="submit" value="Confirm RHIP Voucher" />
							</form>
						</c:if>
						<c:if test="${row.effectiveStatus eq 'FAILED' && canRetry}">
							<form method="post" action="rhipVoucherSubmissions.form" onsubmit="return confirmRhipVoucherSubmission(this);">
								<input type="hidden" name="action" value="retry" />
								<input type="hidden" name="globalBillId" value="${globalBillId}" />
								<input type="hidden" name="dischargeDate" value="${dischargeDate}" />
								<input type="hidden" name="status" value="${status}" />
								<input type="hidden" name="insuranceId" value="${selectedInsuranceId}" />
								<input type="hidden" name="query" value="${query}" />
								<input type="hidden" name="sortBy" value="${sortBy}" />
								<input type="hidden" name="sortDirection" value="${sortDirection}" />
								<input type="hidden" name="page" value="${page}" />
								<input type="hidden" name="pageSize" value="${pageSize}" />
								<input type="submit" value="Retry RHIP Voucher" />
							</form>
						</c:if>
						<a class="rhip-voucher-open-link" href="viewGlobalBill.form?globalBillId=${globalBillId}">Open Global Bill</a>
						</div>
					</td>
				</tr>
				<tr id="rhip-voucher-detail-${globalBillId}" class="rhip-voucher-details">
					<td colspan="11">
						<table cellpadding="3" cellspacing="0" width="100%">
							<tr>
								<th class="columnHeader" colspan="4">Global bill summary</th>
							</tr>
							<tr>
								<td class="rowValue"><b>Patient</b></td>
								<td class="rowValue">${row.patientName} (${row.patientIdentifier})</td>
								<td class="rowValue"><b>Insurance</b></td>
								<td class="rowValue">${row.insuranceName} / ${row.insuranceCardNumber}</td>
							</tr>
							<tr>
								<td class="rowValue"><b>Admission date</b></td>
								<td class="rowValue"><fmt:formatDate value="${globalBill.admission.admissionDate}" pattern="yyyy-MM-dd HH:mm"/></td>
								<td class="rowValue"><b>Discharge date</b></td>
								<td class="rowValue"><fmt:formatDate value="${globalBill.closingDate}" pattern="yyyy-MM-dd HH:mm"/></td>
							</tr>
							<tr>
								<td class="rowValue"><b>Total bill amount</b></td>
								<td class="rowValue"><fmt:formatNumber value="${globalBill.globalAmount}" type="number" pattern="#,##0.##"/></td>
								<td class="rowValue"><b>Patient contribution</b></td>
								<td class="rowValue"><fmt:formatNumber value="${row.patientContribution}" type="number" pattern="#,##0.##"/></td>
							</tr>
							<tr>
								<td class="rowValue"><b>Insurance contribution</b></td>
								<td class="rowValue"><fmt:formatNumber value="${row.insuranceContribution}" type="number" pattern="#,##0.##"/></td>
								<td class="rowValue"><b>Diagnoses</b></td>
								<td class="rowValue">${globalBill.admission.diseaseType}</td>
							</tr>
						</table>

						<table cellpadding="3" cellspacing="0" width="100%">
							<tr>
								<th class="columnHeader">Billable services, drugs and consumables</th>
								<th class="columnHeader">Date</th>
								<th class="columnHeader">Quantity</th>
								<th class="columnHeader">Unit price</th>
								<th class="columnHeader">Amount</th>
							</tr>
							<c:forEach items="${consommationsByGlobalBillId[globalBillId]}" var="consommation">
								<c:forEach items="${consommation.billItems}" var="item">
									<c:if test="${!item.voided}">
										<tr>
											<td class="rowValue">
												<c:choose>
									<c:when test="${not empty item.service.facilityServicePrice.name}">${item.service.facilityServicePrice.name}</c:when>
													<c:when test="${not empty item.serviceOther}">${item.serviceOther}</c:when>
													<c:otherwise>${item.hopService.name}</c:otherwise>
												</c:choose>
											</td>
											<td class="rowValue"><fmt:formatDate value="${item.serviceDate}" pattern="yyyy-MM-dd"/></td>
											<td class="rowValue right">${item.quantity}</td>
											<td class="rowValue right"><fmt:formatNumber value="${item.unitPrice}" type="number" pattern="#,##0.##"/></td>
											<td class="rowValue right"><fmt:formatNumber value="${item.amount}" type="number" pattern="#,##0.##"/></td>
										</tr>
									</c:if>
								</c:forEach>
							</c:forEach>
						</table>

						<c:if test="${canViewHistory}">
							<table cellpadding="3" cellspacing="0" width="100%">
								<tr>
									<th class="columnHeader">Attempt</th>
									<th class="columnHeader">Status</th>
									<th class="columnHeader">Submitted by</th>
									<th class="columnHeader">Date submitted</th>
									<th class="columnHeader">HTTP</th>
									<th class="columnHeader">Voucher reference</th>
									<th class="columnHeader">Message</th>
								</tr>
								<c:forEach items="${historyByGlobalBillId[globalBillId]}" var="history">
									<tr>
										<td class="rowValue">${history.attemptNumber}</td>
										<td class="rowValue">${history.status}</td>
										<td class="rowValue">${history.submittedBy.username}</td>
										<td class="rowValue"><fmt:formatDate value="${history.dateSubmitted}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
										<td class="rowValue">${history.responseCode}</td>
										<td class="rowValue">
											<c:choose>
												<c:when test="${not empty history.voucherReferenceNumber}">${history.voucherReferenceNumber}</c:when>
												<c:otherwise>${history.voucherCode}</c:otherwise>
											</c:choose>
										</td>
										<td class="rowValue">${history.errorMessage}</td>
									</tr>
								</c:forEach>
							</table>
						</c:if>
					</td>
				</tr>
			</c:forEach>
			</tbody>
		</table>
	</c:otherwise>
</c:choose>

<div class="rhip-voucher-pagination">
	<c:choose>
		<c:when test="${hasPreviousPage}">
			<a href="rhipVoucherSubmissions.form?page=${previousPage}&${filterQueryString}">Previous</a>
		</c:when>
		<c:otherwise><span>Previous</span></c:otherwise>
	</c:choose>
	<span>Page ${page} of ${totalPages}</span>
	<c:choose>
		<c:when test="${hasNextPage}">
			<a href="rhipVoucherSubmissions.form?page=${nextPage}&${filterQueryString}">Next</a>
		</c:when>
		<c:otherwise><span>Next</span></c:otherwise>
	</c:choose>
</div>

<%@ include file="/WEB-INF/view/module/mohbilling/templates/footer.jsp"%>
