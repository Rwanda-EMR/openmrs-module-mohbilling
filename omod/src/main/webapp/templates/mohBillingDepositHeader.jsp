<ul id="menu">

		
		<li class="<c:if test='<%= request.getRequestURI().contains("SearchDepost")%>'> active</c:if>">
			<a href="searchPatientAccount.form">Manage Deposit</a>
		</li>
		
		
</ul>

	   <!-- Here I would like to set the totals at the bottom --> 
<style>
	.columnHeader {
	    background: none repeat scroll 0 0 #E6E6E6;
	    border: 1px solid #D3D3D3;
	    color: #555555;
	    cursor: pointer;
	    font-size: 0.8em;
	    font-weight: normal;
	    margin: 0;
	    padding: 3px 0 3px 5px;
	    text-align: left;
	}
	
	.rowValue {
	    font-size: 0.8em;
	    font-weight: normal;
	    margin: 0;
	    padding: 5px;
	    vertical-align: top;
	    border-top: 1px solid cadetblue;
	}
	.rowAmountValue {
	    font-size: 0.8em;
	    font-weight: normal;
	    margin: 0;
	    padding: 5px;
	    /* Here I would like to set the totals at the bottom*/
	    vertical-align: bottom;
	    border-top: 1px solid cadetblue;
	    border-right: 1px solid cadetblue;
	    border-left: 1px solid cadetblue;
	}
	.rowTotalValue {
	    font-size: 0.8em;
	    font-weight: normal;
	    margin: 0;
	    padding: 5px;
	    vertical-align: top;
	    border-top: 1px solid cadetblue;
	    border-right: 1px solid cadetblue;
	    border-bottom: 1px solid cadetblue;
	    border-left: 1px solid cadetblue;
	}
	
	.even {
	    background-color: whitesmoke;
	}
	
	.numbers{
		text-align: right;
		background-color: whitesmoke;
	}
	
	.amount{
		text-align: right;
		background-color: #C9DAFF;
		border: 2px solid #000000;
		font-weight: bold;
	}
	
	.right{
		text-align: right;
	}
	
	.center{
		text-align: center;
	}

	.searchRow:hover {
		background: #F0E68C;
		cursor: pointer;
	}
</style>