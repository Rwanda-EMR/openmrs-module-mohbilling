package org.openmrs.module.mohbilling.businesslogic;

import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.businesslogic.ReportsUtil.HeaderFooter;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FileExporter {
    //private Log log = LogFactory.getLog(this.getClass());
    private static final String WHITESPACE = " ";
    protected final Log log = LogFactory.getLog(getClass());
    private PatientIdentifierType patientID = Context.getPatientService().getPatientIdentifierType(Integer.valueOf(Context.getAdministrationService().getGlobalProperty(
            BillingConstants.GLOBAL_PROPERTY_PRIMARY_IDENTIFIER_TYPE)));


    public void printTransaction(HttpServletRequest request, HttpServletResponse response, Transaction transaction, String filename) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\""); // file name

        Document document = new Document();

        try {
            Font font = new Font(FontFamily.COURIER, 6, Font.NORMAL);
            FontSelector fontselector = new FontSelector();
            fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
            openFile(request, response, document);
            displayHeader(document, fontselector);
            displayTransaction(document, transaction, font);
            document.add(new Paragraph("\n"));
            // displayFooter(document,transaction.getPatientAccount().getPatient(), fontselector);
            displayFooter(document, transaction.getPatientAccount().getPatient(), Context.getAuthenticatedUser(), null, fontselector);
            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void printPayment(HttpServletRequest request, HttpServletResponse response, BillPayment payment, Consommation consommation, String filename) throws Exception {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        Document document = new Document();

        try {

            FontSelector fontselector = new FontSelector();
            fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
            openFile(request, response, document);
            displayHeader(document, fontselector);
            displayPaidItems(document, payment, consommation, fontselector);
            document.add(new Paragraph("............................................................................................................................................................\n"));
            displayAllItems(document, consommation, fontselector);
            document.add(new Paragraph("\n"));
            // displayFooter(document,consommation.getBeneficiary().getPatient(), fontselector);
            displayFooter(document, consommation.getBeneficiary().getPatient(), Context.getAuthenticatedUser(), null, fontselector, consommation, payment);

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void openFile(HttpServletRequest request, HttpServletResponse response, Document document) throws DocumentException, IOException {

        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

        writer.setBoxSize("art", PageSize.A4);
        HeaderFooter event = new HeaderFooter();
        writer.setPageEvent(event);
        document.open();

    }

    public void displayHeader(Document document, FontSelector fontSelector) throws Exception {
        Image image = Image.getInstance(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_LOGO));
        image.scaleToFit(40, 40);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        document.add(fontSelector.process("REPUBLIQUE DU RWANDA                                                        Date:" + df.format(new Date()) + "\n"));

        document.add(image);
        document.add(fontSelector.process(Context.getAdministrationService().getGlobalProperty(
                BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME) + "\n"));
        document.add(fontSelector.process(Context.getAdministrationService()
                .getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS) + "\n"));
        document.add(fontSelector.process(Context.getAdministrationService().getGlobalProperty(
                BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL) + "\n"));
    }

    public void displayTransaction(Document document, Transaction transaction, Font font) throws DocumentException {
        Chunk chk = new Chunk("RECU POUR CAUTION #" + transaction.getTransactionId());
        chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
        chk.setUnderline(0.2f, -2f);
        Paragraph pa = new Paragraph();
        pa.add(chk);
        pa.setAlignment(Element.ALIGN_CENTER);
        document.add(pa);
        document.add(new Paragraph("\n"));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100f);


        FontSelector normal = new FontSelector();
        normal.addFont(new Font(FontFamily.COURIER, 8f, Font.NORMAL));

        FontSelector boldFont = new FontSelector();
        boldFont.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));


        PdfPTable tableLeft = new PdfPTable(1);
        tableLeft.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPTable tableRight = new PdfPTable(1);
        tableRight.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPTable transTable = new PdfPTable(2);

        PdfPCell c1 = new PdfPCell(normal.process("Amount : " + transaction.getAmount().abs()));
        c1.setBorder(Rectangle.NO_BORDER);
        tableLeft.addCell(c1);


        c1 = new PdfPCell(normal.process("Received from :  " + transaction.getPatientAccount().getPatient().getPersonName()));
        c1.setBorder(Rectangle.NO_BORDER);
        tableLeft.addCell(c1);

        c1 = new PdfPCell(normal.process("Patient_ID: " + transaction.getPatientAccount().getPatient().getPatientIdentifier(patientID)));
        c1.setBorder(Rectangle.NO_BORDER);
        tableLeft.addCell(c1);

        c1 = new PdfPCell(normal.process("Received by : " + transaction.getCollector().getPersonName()));
        c1.setBorder(Rectangle.NO_BORDER);
        tableLeft.addCell(c1);

        c1 = new PdfPCell(normal.process("For : " + transaction.getReason()));
        c1.setBorder(Rectangle.NO_BORDER);
        tableRight.addCell(c1);

        c1 = new PdfPCell(normal.process("Receipt No : " + transaction.getTransactionId()));
        c1.setBorder(Rectangle.NO_BORDER);
        tableRight.addCell(c1);

        PdfPCell c = new PdfPCell(tableLeft);
        c.setBorder(Rectangle.NO_BORDER);
        transTable.addCell(c);

        c = new PdfPCell(tableRight);
        c.setBorder(Rectangle.NO_BORDER);
        transTable.addCell(c);

        document.add(transTable);
    }

    public void displayPaidItems(Document document, BillPayment payment, Consommation consommation, FontSelector fontSelector) throws DocumentException {
        NumberFormat formatter = new DecimalFormat("#0.0");
        NumberFormat fm = new DecimalFormat("#0");
        InsurancePolicy ip = consommation.getBeneficiary().getInsurancePolicy();
        Float insuranceRate = ip.getInsurance().getCurrentRate().getRate();
        float patRate = 100f - insuranceRate;
        if (ip.getThirdParty() != null) {
            float thirdPartyRate = ip.getThirdParty().getRate();
            patRate = 100f - insuranceRate - thirdPartyRate;
        }
        BigDecimal patientRate = new BigDecimal("" + patRate);

        Chunk chk = new Chunk("RECEIPT#" + payment.getBillPaymentId() + "BILL#" + consommation.getConsommationId() + " GB#" + consommation.getGlobalBill().getBillIdentifier() + " - " + payment.getDateReceived() + " Patient_ID: " + consommation.getBeneficiary().getPatient().getPatientIdentifier(patientID));
        chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
        chk.setUnderline(0.2f, -2f);
        Paragraph pa = new Paragraph();
        pa.add(chk);
        pa.setAlignment(Element.ALIGN_CENTER);
        document.add(pa);
        document.add(new Paragraph("\n"));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100f);

        FontSelector normal = new FontSelector();
        normal.addFont(new Font(FontFamily.COURIER, 6f, Font.NORMAL));

        FontSelector boldFont = new FontSelector();
        boldFont.addFont(new Font(FontFamily.COURIER, 7f, Font.BOLD));

        PdfPCell cell = null;

        int number = 0;

        float[] colsRigth = {45f, 20f, 35f};
        float[] colsLeft = {45f, 20f, 35f};

        PdfPTable tableLeft = new PdfPTable(colsLeft);
        tableLeft.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableLeft.setSpacingAfter(0f);
        tableLeft.setSpacingBefore(0f);

        PdfPTable tableRight = new PdfPTable(colsRigth);
        tableRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tableRight.setSpacingAfter(0f);
        tableRight.setSpacingBefore(0f);

        String itemNamep;
        String drugFrequency;
        BigDecimal unitPricep;

        for (PatientServiceBill psbitems : consommation.getBillItems()) {
            if (!psbitems.getVoided()) {
                number = number + 1;
                BigDecimal itemCostp = psbitems.getQuantity().multiply(psbitems.getUnitPrice()).multiply(patientRate).divide(new BigDecimal(100));
                itemNamep = psbitems.getService().getFacilityServicePrice().getName();
                drugFrequency = psbitems.getDrugFrequency();
                unitPricep = psbitems.getUnitPrice().multiply(patientRate).divide(new BigDecimal(100));
                String itemPaymentStatus = "";
                if (psbitems.isPaid()) {
                    itemPaymentStatus = "Paid";
                } else {
                    itemPaymentStatus = "Unpaid";
                }
                StringBuilder builder = new StringBuilder();
                builder.append(formatter.format(unitPricep));
                builder.append("X");
                builder.append(WHITESPACE);
                builder.append(fm.format(psbitems.getQuantity()));
                builder.append("=");
                builder.append(formatter.format(itemCostp));
                //builder.append(WHITESPACE);
                builder.append("(" + itemPaymentStatus + ")");

                String resultBuilder = builder.toString();

                //String itemDetailsp = number + ")" + itemNamep +"/"+boldFont.process(drugFrequency)+ "/" +formatter.format(unitPricep) + " X " + psbitems.getQuantity() + " = " + formatter.format(itemCostp);

                //cell = new PdfPCell(fontSelector.process("" + itemDetailsp));

                //cell = new PdfPCell(" "+fontSele(number)+" "+fontSelector.process(itemNamep)+" "+boldFont.process(drugFrequency)+" "+" "+formatter.format(unitPricep)+" "+psbitems.getQuantity()+""+formatter.format(itemCostp));
                StringBuilder sb = new StringBuilder();
                sb.append("" + number + ")");
                sb.append(itemNamep);
                String resultItemName = sb.toString();

				/*cell=new PdfPCell(boldFont.process(""+number+")"));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setBorderWidth(0f);
				cell.setPadding(0f);
				if (number % 2 != 0)
					tableRight.addCell(cell);
				else tableLeft.addCell(cell);
*/
                cell = new PdfPCell(fontSelector.process("" + resultItemName));
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setBorderWidth(0f);
                cell.setPadding(0f);
                if (number % 2 != 0)
                    tableRight.addCell(cell);
                else tableLeft.addCell(cell);

                cell = new PdfPCell(boldFont.process("" + drugFrequency));
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setBorderWidth(0f);
                cell.setPadding(0f);
                if (number % 2 != 0)
                    tableRight.addCell(cell);
                else tableLeft.addCell(cell);

				/*cell=new PdfPCell(fontSelector.process(""+formatter.format(unitPricep)+"X"));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setBorderWidth(0f);
				cell.setPadding(0f);
				if (number % 2 != 0)
					tableRight.addCell(cell);
				else tableLeft.addCell(cell);*/

				/*cell=new PdfPCell(fontSelector.process(""+psbitems.getQuantity()+"="));
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setBorderWidth(0f);
				cell.setPadding(0f);
				if (number % 2 != 0)
					tableRight.addCell(cell);
				else tableLeft.addCell(cell);*/

                cell = new PdfPCell(boldFont.process("" + resultBuilder));
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setBorderWidth(0f);
                cell.setPadding(0f);
                if (number % 2 != 0)
                    tableRight.addCell(cell);
                else tableLeft.addCell(cell);

				/*cell.setBorder(Rectangle.NO_BORDER);
				if (number % 2 != 0)
					tableRight.addCell(cell);
				else tableLeft.addCell(cell);*/
            }
        }


		/*String itemName;
		BigDecimal unitPrice,paidQty,itemPaidCost=new BigDecimal(0.0);
		BigDecimal totalToBePaidByPatient = new BigDecimal(0.0);
		*/
        BigDecimal totalPaid = new BigDecimal(0.0);

        List<PaidServiceBill> paidItems = BillPaymentUtil.getPaidItemsByBillPayment(payment);

        if (consommation.getGlobalBill().getBillIdentifier().substring(0, 4).equals("bill")) {
            paidItems = BillPaymentUtil.getOldPayments(payment);
        }

		/*for (PaidServiceBill service: paidItems) {
			number++;

			BigDecimal itemCost = service.getBillItem().getQuantity().multiply(service.getBillItem().getUnitPrice().multiply(patientRate).divide(new BigDecimal(100)));
			totalToBePaidByPatient=totalToBePaidByPatient.add(itemCost);

			BigDecimal paid= service.getBillItem().getPaidQuantity().multiply(itemCost);
			//totalPaid=totalPaid.add(paid);

			itemName = service.getBillItem().getService().getFacilityServicePrice().getName();
			unitPrice = service.getBillItem().getUnitPrice().multiply(patientRate).divide(new BigDecimal(100));
			paidQty = service.getPaidQty();
			itemPaidCost = paid;


			String itemDetails = number+")"+itemName +" "+unitPrice +" X "+paidQty+" = "+formatter.format(unitPrice.multiply(paidQty));

			cell = new PdfPCell(fontSelector.process(""+itemDetails));
			cell.setBorder(Rectangle.NO_BORDER);
			if(number%2!=0)tableRight.addCell(cell);else tableLeft.addCell(cell);
		}
		*/
        PdfPCell c = new PdfPCell(tableRight);
        c.setBorder(Rectangle.NO_BORDER);
        table.addCell(c);

        c = new PdfPCell(tableLeft);
        c.setBorder(Rectangle.NO_BORDER);
        table.addCell(c);

        document.add(table);

        PdfPTable serviceTotPat = new PdfPTable(4);

        for (BillPayment pay : consommation.getPatientBill().getPayments()) {
            if (pay.getVoidReason() == null) {

                totalPaid = totalPaid.add(pay.getAmountPaid());
            }
        }

        BigDecimal totale = new BigDecimal(0);
        BigDecimal totalDueAmount = new BigDecimal(0);
        for (PatientServiceBill item : consommation.getBillItems()) {
            if (!item.getVoided()) {

                totale = totale.add(item.getQuantity().multiply(item.getUnitPrice()));
                totalDueAmount = totale.multiply(patientRate).divide(new BigDecimal(100));
            }
        }

        PdfPCell c1 = new PdfPCell(boldFont.process("Due Amount: " + formatter.format(totalDueAmount)));

        c1.setBorder(Rectangle.NO_BORDER);
        serviceTotPat.addCell(c1);

        c1 = new PdfPCell(boldFont.process("Paid: " + payment.getAmountPaid()));
        if (consommation.getGlobalBill().getBillIdentifier().substring(0, 4).equals("bill")) {
            c1 = new PdfPCell(boldFont.process("Paid: " + formatter.format(payment.getAmountPaid())));
        }
        c1.setBorder(Rectangle.NO_BORDER);
        serviceTotPat.addCell(c1);

        c1 = new PdfPCell(boldFont.process(""));
        c1.setBorder(Rectangle.NO_BORDER);
        serviceTotPat.addCell(c1);

        String rest = formatter.format((totalDueAmount).subtract(totalPaid));
		/*if(rest.compareTo(BigDecimal.ZERO)<0)
		c1 = new PdfPCell(boldFont.process("Rest: "+0));
		else
			c1 = new PdfPCell(boldFont.process("Rest: "+consommation.getPatientBill().getAmount().subtract(totalPaid)));*/
        if (consommation.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getFlatFee() != null) {
            c1 = new PdfPCell(boldFont.process("Rest: " + "0.00"));
        } else {
            c1 = new PdfPCell(boldFont.process("Rest: " + rest));
        }
        c1.setBorder(Rectangle.NO_BORDER);
        serviceTotPat.addCell(c1);

        document.add(serviceTotPat);

        //displayFooter(document, consommation.getBeneficiary().getPatient(), fontSelector);
        User user = Context.getAuthenticatedUser();
        displayFooter(document, consommation.getBeneficiary().getPatient(), user, null, fontSelector);
    }

    public void displayAllItems(Document document, Consommation consommation, FontSelector fontSelector) throws Exception {

        displayHeader(document, fontSelector);
        //HEADING

        float[] colsWidt = {25f, 25f, 25f, 25f};
        PdfPTable heading2Tab = new PdfPTable(colsWidt);
        heading2Tab.setWidthPercentage(100f);

        fontSelector = new FontSelector();
        fontSelector.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        PdfPCell head3 = new PdfPCell(fontSelector.process(""));
        head3.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head3);

        head3 = new PdfPCell(fontSelector.process(""));
        head3.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head3);

        head3 = new PdfPCell(fontSelector.process(""));
        head3.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head3);

        head3 = new PdfPCell(fontSelector.process("DISEASE TYPE: " + consommation.getGlobalBill().getAdmission().getDiseaseType() + "\n"));
        head3.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head3);

        head3 = new PdfPCell(fontSelector.process(""));
        head3.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head3);

        head3 = new PdfPCell(fontSelector.process(""));
        head3.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head3);

        head3 = new PdfPCell(fontSelector.process(""));
        head3.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head3);

        head3 = new PdfPCell(fontSelector.process(""));
        head3.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head3);

        PdfPCell head2 = new PdfPCell(fontSelector.process("CARD: " + consommation.getBeneficiary().getInsurancePolicy().getInsurance().getName() + "/" + consommation.getBeneficiary().getPolicyIdNumber() + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        head2 = new PdfPCell(fontSelector.process("BENEFICIARY'S NAMES: " + consommation.getBeneficiary().getPatient().getPersonName() + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        head2 = new PdfPCell(fontSelector.process("DOB: " + df.format(consommation.getBeneficiary().getPatient().getBirthdate()) + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        head2 = new PdfPCell(fontSelector.process("SEX: " + consommation.getBeneficiary().getPatient().getGender() + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        // After RSSB meeting

        head2 = new PdfPCell(fontSelector.process("PROVINCE: " + consommation.getBeneficiary().getPatient().getPersonAddress().getStateProvince() + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        head2 = new PdfPCell(fontSelector.process("DISTRICT: " + consommation.getBeneficiary().getPatient().getPersonAddress().getCountyDistrict() + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        head2 = new PdfPCell(fontSelector.process("SECTOR: " + consommation.getBeneficiary().getPatient().getPersonAddress().getCityVillage() + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        head2 = new PdfPCell(fontSelector.process("CELL/VILLAGE: " + consommation.getBeneficiary().getPatient().getPersonAddress().getAddress3() + "/" + consommation.getBeneficiary().getPatient().getPersonAddress().getAddress1() + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

		/*head2 = new PdfPCell(fontSelector.process("Village: "+consommation.getBeneficiary().getPatient().getPersonAddress().getAddress1()+"\n"));
		head2.setBorder(Rectangle.NO_BORDER);
		heading2Tab.addCell(head2);*/


//if(!(consommation.getBeneficiary().getOwnerName()==null) && !(consommation.getBeneficiary().getOwnerCode()==null)) {
        head2 = new PdfPCell(fontSelector.process("NAME(S) OF HOUSEHOLD HEAD: " + consommation.getBeneficiary().getOwnerName() + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        head2 = new PdfPCell(fontSelector.process("FAMILY'S/AFFILIATION CODE: " + consommation.getBeneficiary().getOwnerCode() + " / Company:" + consommation.getBeneficiary().getCompany() + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        head2 = new PdfPCell(fontSelector.process("CATEGORY: " + consommation.getBeneficiary().getLevel() + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);


        head2 = new PdfPCell(fontSelector.process("PHONE NO: " + consommation.getBeneficiary().getPatient().getAttribute("Phone number") + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

	/*head2 = new PdfPCell(fontSelector.process("    "));
	head2.setBorder(Rectangle.NO_BORDER);
	heading2Tab.addCell(head2);*/
//}


        head2 = new PdfPCell(fontSelector.process("SERVICE: " + consommation.getDepartment().getName() + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        String admissionMode = "";
        if (consommation.getGlobalBill().getAdmission().getIsAdmitted())
            admissionMode = "Non";
        else
            admissionMode = "Qui";

        head2 = new PdfPCell(fontSelector.process("AMBULANT: " + admissionMode + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        head2 = new PdfPCell(fontSelector.process("DATE D'ENTREE: " + df.format(consommation.getGlobalBill().getAdmission().getAdmissionDate()) + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);
        String dischargeDate = "";
        if (consommation.getGlobalBill().getClosingDate() != null)
            dischargeDate = df.format(consommation.getGlobalBill().getClosingDate());
        else
            dischargeDate = "-";

        head2 = new PdfPCell(fontSelector.process("DATE DE SORTIE: " + dischargeDate + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        document.add(heading2Tab);

        //END HEADING
        Chunk chk = new Chunk("FACTURE DES PRESTATIONS DE SOINS DE SANTE #" + consommation.getGlobalBill().getBillIdentifier() + " - " + consommation.getCreatedDate() + " Patient_ID: " + consommation.getBeneficiary().getPatient().getPatientIdentifier(patientID));
        chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
        chk.setUnderline(0.2f, -2f);
        Paragraph pa = new Paragraph();
        pa.add(chk);
        pa.setAlignment(Element.ALIGN_CENTER);
        document.add(pa);
        document.add(new Paragraph("\n"));

        float[] colsWidth = {15f, 50f, 15f, 15f, 15f, 15f};
        PdfPTable table = new PdfPTable(colsWidth);
        table.setWidthPercentage(100f);

        FontSelector normal = new FontSelector();
        normal.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));

        FontSelector boldFont = new FontSelector();
        boldFont.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
        // table Header
        PdfPCell cell = new PdfPCell(boldFont.process("Date"));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("Libelle"));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("Dosage"));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("Quantity"));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("P.U"));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("P.T"));
        table.addCell(cell);


        AllServicesRevenue servicesRevenu = ReportsUtil.getAllServicesRevenue(consommation, "mohbilling.REVENUE");

        // BigDecimal allGlobalAmount = servicesRevenu.getAllDueAmounts();
        List<ServiceRevenue> revenueList = servicesRevenu.getRevenues();

        ServiceRevenue imagingRevenue = ReportsUtil.getServiceRevenue(consommation, "mohbilling.IMAGING");
        revenueList.add(imagingRevenue);

        ServiceRevenue actsRevenue = ReportsUtil.getServiceRevenue(consommation, "mohbilling.ACTS");
        revenueList.add(actsRevenue);

        ServiceRevenue autresRevenue = ReportsUtil.getServiceRevenue(consommation, "mohbilling.AUTRES");
        revenueList.add(autresRevenue);

        Float insRate = consommation.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate();
        float patRate = 100f - insRate;
        BigDecimal patientRate = new BigDecimal("" + patRate);
        BigDecimal insuranceRate = new BigDecimal("" + insRate);

        BigDecimal total = new BigDecimal(0);

        NumberFormat formatter = new DecimalFormat("#0.00");
        if (revenueList != null)
            for (ServiceRevenue sr : revenueList) {
                //if(sr!=null&& sr.getDueAmount().compareTo(BigDecimal.ZERO)!=0){
                if (sr != null && sr.getBillItems().size() != 0) {

                    cell = new PdfPCell(fontSelector.process(""));
                    table.addCell(cell);
                    cell = new PdfPCell(boldFont.process("" + sr.getService()));
                    table.addCell(cell);
                    cell = new PdfPCell(fontSelector.process(""));
                    table.addCell(cell);
                    cell = new PdfPCell(fontSelector.process(""));
                    table.addCell(cell);
                    cell = new PdfPCell(fontSelector.process(""));
                    table.addCell(cell);
                    cell = new PdfPCell(fontSelector.process(""));
                    table.addCell(cell);
                    BigDecimal totalByService = new BigDecimal(0);
                    for (PatientServiceBill item : sr.getBillItems()) {
                        if (!item.getVoided()) {
                            cell = new PdfPCell(fontSelector.process("" + df.format(item.getServiceDate())));
                            table.addCell(cell);
                            cell = new PdfPCell(fontSelector.process("" + item.getService().getFacilityServicePrice().getName()));
                            table.addCell(cell);

                            cell = new PdfPCell(fontSelector.process("" + item.getDrugFrequency()));
                            table.addCell(cell);

                            cell = new PdfPCell(fontSelector.process("" + item.getQuantity()));
                            table.addCell(cell);

                            cell = new PdfPCell(fontSelector.process("" + item.getUnitPrice()));
                            table.addCell(cell);
//					 cell = new PdfPCell(fontSelector.process(""+formatter.format(item.getQuantity().multiply(item.getUnitPrice().multiply(patientRate.divide(new BigDecimal(100) ))))));
                            cell = new PdfPCell(fontSelector.process("" + formatter.format(item.getQuantity().multiply(item.getUnitPrice()))));
                            table.addCell(cell);

//					 totalByService=totalByService.add(item.getQuantity().multiply(item.getUnitPrice().multiply(patientRate.divide(new BigDecimal(100) ))));
                            totalByService = totalByService.add(item.getQuantity().multiply(item.getUnitPrice()));
                            total = total.add(item.getQuantity().multiply(item.getUnitPrice()));
                        }
                    }
                    cell = new PdfPCell(fontSelector.process(""));
                    table.addCell(cell);
                    cell = new PdfPCell(fontSelector.process(""));
                    table.addCell(cell);
                    cell = new PdfPCell(fontSelector.process(""));
                    table.addCell(cell);
                    cell = new PdfPCell(fontSelector.process(""));
                    table.addCell(cell);
                    cell = new PdfPCell(fontSelector.process(""));
                    table.addCell(cell);
                    cell = new PdfPCell(boldFont.process("" + formatter.format(totalByService)));
                    table.addCell(cell);
                }
            }
        cell = new PdfPCell(fontSelector.process(""));
        table.addCell(cell);
        cell = new PdfPCell(fontSelector.process("TOTAL FACTURE"));
        table.addCell(cell);
        cell = new PdfPCell(fontSelector.process(""));
        table.addCell(cell);
        cell = new PdfPCell(fontSelector.process(""));
        table.addCell(cell);
        cell = new PdfPCell(boldFont.process("" + formatter.format(total)));
        table.addCell(cell);

        document.add(table);
        document.add(new Paragraph("\n"));
        float[] width = {4f, 3f, 3f};
        PdfPTable summaryTable = new PdfPTable(width);

        PdfPCell c = new PdfPCell(boldFont.process("Assurance"));
        cell.setBorder(Rectangle.NO_BORDER);
        summaryTable.addCell(c);

        c = new PdfPCell(boldFont.process("Montant"));
        cell.setBorder(Rectangle.NO_BORDER);
        summaryTable.addCell(c);

        c = new PdfPCell(boldFont.process("%"));
        cell.setBorder(Rectangle.NO_BORDER);
        summaryTable.addCell(c);

        c = new PdfPCell(fontSelector.process("Total"));
        cell.setBorder(Rectangle.NO_BORDER);
        summaryTable.addCell(c);


        c = new PdfPCell(fontSelector.process("" + formatter.format(total)));
        cell.setBorder(Rectangle.NO_BORDER);
        summaryTable.addCell(c);

        c = new PdfPCell(fontSelector.process("100%"));
        cell.setBorder(Rectangle.NO_BORDER);
        summaryTable.addCell(c);

        c = new PdfPCell(fontSelector.process("Ticket Moderateur"));
        cell.setBorder(Rectangle.NO_BORDER);
        summaryTable.addCell(c);

//		c = new PdfPCell(fontSelector.process(""+consommation.getPatientBill().getAmount()));
        c = new PdfPCell(fontSelector.process("" + formatter.format(total.multiply(patientRate).divide(new BigDecimal(100)))));
        cell.setBorder(Rectangle.NO_BORDER);
        summaryTable.addCell(c);

        c = new PdfPCell(fontSelector.process(patientRate + "%"));
        cell.setBorder(Rectangle.NO_BORDER);
        summaryTable.addCell(c);

        if (consommation.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getFlatFee() != null && consommation.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getFlatFee().compareTo(BigDecimal.ZERO) > 0) {
            c = new PdfPCell(fontSelector.process("Ticket Moderateur"));
            cell.setBorder(Rectangle.NO_BORDER);
            summaryTable.addCell(c);

//		c = new PdfPCell(fontSelector.process(""+consommation.getPatientBill().getAmount()));
            c = new PdfPCell(fontSelector.process("" + consommation.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getFlatFee()));
            cell.setBorder(Rectangle.NO_BORDER);
            summaryTable.addCell(c);

            c = new PdfPCell(fontSelector.process("Flat fee"));
            cell.setBorder(Rectangle.NO_BORDER);
            summaryTable.addCell(c);
        } else {
            c = new PdfPCell(fontSelector.process("Ticket Moderateur"));
            cell.setBorder(Rectangle.NO_BORDER);
            summaryTable.addCell(c);

//		c = new PdfPCell(fontSelector.process(""+consommation.getPatientBill().getAmount()));
            c = new PdfPCell(fontSelector.process("" + formatter.format(total.multiply(patientRate).divide(new BigDecimal(100)))));
            cell.setBorder(Rectangle.NO_BORDER);
            summaryTable.addCell(c);

            c = new PdfPCell(fontSelector.process(patientRate + "%"));
            cell.setBorder(Rectangle.NO_BORDER);
            summaryTable.addCell(c);

        }


        c = new PdfPCell(fontSelector.process("" + consommation.getBeneficiary().getInsurancePolicy().getInsurance().getName()));
        cell.setBorder(Rectangle.NO_BORDER);
        summaryTable.addCell(c);

        c = new PdfPCell(fontSelector.process("" + formatter.format(total.multiply(insuranceRate).divide(new BigDecimal(100)))));
        cell.setBorder(Rectangle.NO_BORDER);
        summaryTable.addCell(c);

        c = new PdfPCell(fontSelector.process(insuranceRate + "%"));
        cell.setBorder(Rectangle.NO_BORDER);
        summaryTable.addCell(c);

        document.add(summaryTable);
    }

    public void displayFooter(Document document, Patient patient, User user, String other, FontSelector fontselector, Consommation consommation, BillPayment payment) throws DocumentException {
        // Table of signatures;
        PdfPTable table1 = new PdfPTable(4);
        table1.setWidthPercentage(100f);

        PdfPCell cell = new PdfPCell(fontselector.process(""));
        if (payment.getCollector() != null) {
            cell = new PdfPCell(fontselector.process("Cashier Signature \n" + payment.getCollector().getPersonName() + "\n\n........................."));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);
        }

        //for report where a patient has to sign on it
        if (patient != null) {
			/*cell = new PdfPCell(fontselector.process("                      "));
			cell.setBorder(Rectangle.NO_BORDER);
			table1.addCell(cell);*/

            cell = new PdfPCell(fontselector.process("Beneficiary Names/Signature: \n\n........................."));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);

            //cell = new PdfPCell(fontselector.process("Clinician/Doctor \n"+consommation.getCreator().getPersonName()+"\n\n........................."));
            cell = new PdfPCell(fontselector.process("Clinician/Doctor\n\n........................."));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);

            cell = new PdfPCell(fontselector.process(" Insurance's Staff: \n" + "\n\n......................"));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);

        }

        if (other != null) {
            cell = new PdfPCell(fontselector.process("                      "));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);

            cell = new PdfPCell(fontselector.process(other + "\n\n........................."));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);
        }

        document.add(table1);
    }

    public void displayFooter(Document document, String other, FontSelector fontselector) throws DocumentException {
        // Table of signatures;
        PdfPTable table1 = new PdfPTable(2);
        table1.setWidthPercentage(100f);

        PdfPCell cell = new PdfPCell(fontselector.process("                      "));
        cell.setBorder(Rectangle.NO_BORDER);
        table1.addCell(cell);

        cell = new PdfPCell(fontselector.process(other + "\n\n........................."));
        cell.setBorder(Rectangle.NO_BORDER);
        table1.addCell(cell);

        document.add(table1);
    }

    public void printGlobalBill(HttpServletRequest request, HttpServletResponse response, GlobalBill gb, List<ServiceRevenue> sr, String filename) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\""); // file name
        Document document = new Document();

        try {

            FontSelector fontselector = new FontSelector();
            fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
            openFile(request, response, document);
            displayHeader(document, fontselector);
            displayServiceRevenues(document, gb, sr, fontselector);
            document.add(new Paragraph("\n"));
            // displayFooter(document,gb.getAdmission().getInsurancePolicy().getOwner(), fontselector);
            User generatedBy = Context.getAuthenticatedUser();
            User createdBy = gb.getClosedBy();
            displayFooter(document, gb.getAdmission().getInsurancePolicy().getOwner(), createdBy, generatedBy, null, fontselector);

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void displayServiceRevenues(Document document, GlobalBill gb, List<ServiceRevenue> sr, FontSelector fontSelector) throws DocumentException {
        float[] colsWidt = {5f, 20f, 55f, 25f, 15f, 25f, 25f, 25f, 25f};
        PdfPTable table = new PdfPTable(colsWidt);
        table.setWidthPercentage(100f);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dfgb = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        FontSelector boldFont = new FontSelector();
        boldFont.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));

        //header
        float[] colsWidt1 = {25f, 25f, 25f, 25f};
        PdfPTable heading2Tab = new PdfPTable(colsWidt1);
        heading2Tab.setWidthPercentage(100f);


        PdfPCell head1 = new PdfPCell(fontSelector.process(""));
        head1.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head1);

        head1 = new PdfPCell(fontSelector.process(""));
        head1.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head1);

        head1 = new PdfPCell(fontSelector.process(""));
        head1.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head1);

        head1 = new PdfPCell(fontSelector.process("DISEASE TYPE: " + gb.getAdmission().getDiseaseType() + "\n"));
        head1.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head1);

        head1 = new PdfPCell(fontSelector.process(""));
        head1.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head1);

        head1 = new PdfPCell(fontSelector.process(""));
        head1.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head1);

        head1 = new PdfPCell(fontSelector.process(""));
        head1.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head1);

        head1 = new PdfPCell(fontSelector.process(""));
        head1.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head1);


        PdfPCell head2 = new PdfPCell(fontSelector.process("Card: " + gb.getAdmission().getInsurancePolicy().getInsurance().getName() + "/" + gb.getAdmission().getInsurancePolicy().getInsuranceCardNo() + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        head2 = new PdfPCell(fontSelector.process("Beneficiary Names: " + gb.getAdmission().getInsurancePolicy().getOwner().getPersonName() + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        head2 = new PdfPCell(fontSelector.process("DOB: " + df.format(gb.getAdmission().getInsurancePolicy().getOwner().getBirthdate()) + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        String admissionMode = "";
        if (gb.getAdmission().getIsAdmitted())
            admissionMode = "Non";
        else
            admissionMode = "Qui";

        head2 = new PdfPCell(fontSelector.process("Ambulant: " + admissionMode + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        head2 = new PdfPCell(fontSelector.process("Sex: " + gb.getAdmission().getInsurancePolicy().getOwner().getGender() + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        head2 = new PdfPCell(fontSelector.process("Date d'entree: " + df.format(gb.getAdmission().getAdmissionDate()) + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        String dischargeDate = "";
        if (gb.getClosingDate() != null)
            dischargeDate = df.format(gb.getClosingDate());
        else
            dischargeDate = "-";

        head2 = new PdfPCell(fontSelector.process("Date de sortie: " + dischargeDate + "\n"));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

        head2 = new PdfPCell(fontSelector.process(""));
        head2.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head2);

		/*Consommation cons=null;

		for(Consommation con:gb.getConsommations()){
			cons=con;
			break;
		}*/


        PdfPCell head3 = new PdfPCell(fontSelector.process("NAME(S) OF HOUSEHOLD HEAD: " + gb.getAdmission().getInsurancePolicy().getBeneficiaries().iterator().next().getOwnerName() + "\n"));
        head3.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head3);

        head3 = new PdfPCell(fontSelector.process("FAMILY'S/AFFILIATION CODE:: " + gb.getAdmission().getInsurancePolicy().getBeneficiaries().iterator().next().getOwnerCode() + "\n"));
        head3.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head3);

        head3 = new PdfPCell(fontSelector.process("CATEGORY: " + gb.getAdmission().getInsurancePolicy().getBeneficiaries().iterator().next().getLevel() + "\n"));
        head3.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head3);

        head3 = new PdfPCell(fontSelector.process("PHONE NO: " + gb.getAdmission().getInsurancePolicy().getOwner().getAttribute("Phone number") + "\n"));
        head3.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head3);


        head3 = new PdfPCell(fontSelector.process("PROVINCE: " + gb.getAdmission().getInsurancePolicy().getOwner().getPersonAddress().getStateProvince() + "\n"));
        head3.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head3);

        head3 = new PdfPCell(fontSelector.process("DISTRICT: " + gb.getAdmission().getInsurancePolicy().getOwner().getPersonAddress().getCountyDistrict() + "\n"));
        head3.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head3);

        head3 = new PdfPCell(fontSelector.process("SECTOR: " + gb.getAdmission().getInsurancePolicy().getOwner().getPersonAddress().getCityVillage() + "\n"));
        head3.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head3);

        head3 = new PdfPCell(fontSelector.process("CELL/VILLAGE: " + gb.getAdmission().getInsurancePolicy().getOwner().getPersonAddress().getAddress3() + "/" + gb.getAdmission().getInsurancePolicy().getOwner().getPersonAddress().getAddress1() + "\n"));
        head3.setBorder(Rectangle.NO_BORDER);
        heading2Tab.addCell(head3);


        document.add(heading2Tab);
        //end header

        Chunk chk = new Chunk("FACTURE DES PRESTATIONS DE SOINS DE SANTE #" + gb.getBillIdentifier() + " - " + df.format(gb.getCreatedDate()) + " Patient_ID: " + gb.getAdmission().getInsurancePolicy().getOwner().getPatientIdentifier(patientID));
        chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
        chk.setUnderline(0.2f, -2f);
        Paragraph pa = new Paragraph();
        pa.add(chk);
        pa.setAlignment(Element.ALIGN_CENTER);
        document.add(pa);
        document.add(new Paragraph("\n"));

        PdfPCell cell = new PdfPCell(boldFont.process("#"));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("Date "));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("Service"));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("Dosage "));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("Qty "));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("UP"));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("100%"));
        table.addCell(cell);

        Float insuranceRate = gb.getAdmission().getInsurancePolicy().getInsurance().getCurrentRate().getRate();
        Float patientRate = 100 - insuranceRate;

        cell = new PdfPCell(boldFont.process(insuranceRate + "%"));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process(patientRate + "%"));
        table.addCell(cell);

        NumberFormat formatter = new DecimalFormat("#0.00");

        BigDecimal totalBill = new BigDecimal(0);

        for (ServiceRevenue r : sr) {
            r.getBillItems();
            //if(r!=null&& r.getDueAmount().compareTo(BigDecimal.ZERO)!=0){
            if (r != null && r.getBillItems().size() != 0) {
                cell = new PdfPCell(fontSelector.process(""));
                table.addCell(cell);
                cell = new PdfPCell(fontSelector.process(""));
                table.addCell(cell);
                cell = new PdfPCell(boldFont.process("" + r.getService()));
                table.addCell(cell);
                cell = new PdfPCell(fontSelector.process(""));
                table.addCell(cell);
                cell = new PdfPCell(fontSelector.process(""));
                table.addCell(cell);
                cell = new PdfPCell(fontSelector.process(""));
                table.addCell(cell);
                cell = new PdfPCell(fontSelector.process(""));
                table.addCell(cell);
                cell = new PdfPCell(fontSelector.process(""));
                table.addCell(cell);
                cell = new PdfPCell(fontSelector.process(""));
                table.addCell(cell);
                int i = 0;
                BigDecimal totalByCategory = new BigDecimal(0);
                for (PatientServiceBill item : r.getBillItems()) {
                    i++;
                    if (!item.getVoided()) {
                        cell = new PdfPCell(fontSelector.process("" + i));
                        table.addCell(cell);
                        cell = new PdfPCell(fontSelector.process("" + dfgb.format(item.getServiceDate())));
                        table.addCell(cell);
                        cell = new PdfPCell(fontSelector.process("" + item.getService().getFacilityServicePrice().getName()));
                        table.addCell(cell);
                        cell = new PdfPCell(fontSelector.process("" + item.getDrugFrequency()));
                        table.addCell(cell);
                        cell = new PdfPCell(fontSelector.process("" + item.getQuantity()));
                        table.addCell(cell);
                        cell = new PdfPCell(fontSelector.process("" + item.getUnitPrice()));
                        table.addCell(cell);
                        BigDecimal total = item.getQuantity().multiply(item.getUnitPrice());
                        totalBill = totalBill.add(total);
                        cell = new PdfPCell(fontSelector.process("" + formatter.format(total)));
                        table.addCell(cell);
                        cell = new PdfPCell(fontSelector.process("" + formatter.format(total.multiply(new BigDecimal(insuranceRate).divide(new BigDecimal(100))))));
                        table.addCell(cell);
                        cell = new PdfPCell(fontSelector.process("" + formatter.format(total.multiply(new BigDecimal(patientRate).divide(new BigDecimal(100))))));
                        table.addCell(cell);

                        totalByCategory = totalByCategory.add(total);
                    }
                }
                PdfPCell c = new PdfPCell(fontSelector.process(""));
                table.addCell(c);
                c = new PdfPCell(fontSelector.process(""));
                table.addCell(c);
                c = new PdfPCell(fontSelector.process(""));
                table.addCell(c);
                c = new PdfPCell(fontSelector.process(""));
                table.addCell(c);
                c = new PdfPCell(fontSelector.process(""));
                table.addCell(c);
                c = new PdfPCell(fontSelector.process(""));
                table.addCell(c);
                c = new PdfPCell(boldFont.process("" + formatter.format(totalByCategory)));
                table.addCell(c);
                c = new PdfPCell(boldFont.process("" + formatter.format(totalByCategory.multiply(new BigDecimal(insuranceRate).divide(new BigDecimal(100))))));
                table.addCell(c);
                c = new PdfPCell(boldFont.process("" + formatter.format(totalByCategory.multiply(new BigDecimal(patientRate).divide(new BigDecimal(100))))));
                table.addCell(c);
            }

        }
        PdfPCell c = new PdfPCell(fontSelector.process(""));
        table.addCell(c);
        c = new PdfPCell(fontSelector.process(""));
        table.addCell(c);
        c = new PdfPCell(fontSelector.process(""));
        table.addCell(c);
        c = new PdfPCell(fontSelector.process(""));
        table.addCell(c);
        c = new PdfPCell(fontSelector.process(""));
        table.addCell(c);
        c = new PdfPCell(fontSelector.process(""));
        table.addCell(c);
        c = new PdfPCell(boldFont.process("" + formatter.format(totalBill)));
        table.addCell(c);
        c = new PdfPCell(boldFont.process("" + formatter.format(totalBill.multiply(new BigDecimal(insuranceRate).divide(new BigDecimal(100))))));
        table.addCell(c);
        c = new PdfPCell(boldFont.process("" + formatter.format(totalBill.multiply(new BigDecimal(patientRate).divide(new BigDecimal(100))))));
        table.addCell(c);
        document.add(table);
    }

    public void printCashierReport(HttpServletRequest request, HttpServletResponse response, BigDecimal amount, List<PaymentRevenue> paymentRevenues, List<BigDecimal> subTotal, BigDecimal bigTotal, BigDecimal totalPaid, String filename, User cachier) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\""); // file name
        Document document = new Document();

        try {

            FontSelector fontselector = new FontSelector();
            fontselector.addFont(new Font(FontFamily.COURIER, 6, Font.NORMAL));
            openFile(request, response, document);
            displayHeader(document, fontselector);
            displayPaidServiceRevenues(document, amount, paymentRevenues, subTotal, bigTotal, totalPaid, fontselector);
            document.add(new Paragraph("\n"));
            //  displayFooter(document,null, fontselector);
            String chiefCashier = "Chief Cashier's names and Signature";
            displayFooter(document, null, Context.getAuthenticatedUser(), chiefCashier, fontselector, cachier);
            //displayFooter(document, null, Context.getAuthenticatedUser(), chiefCashier, fontselector);

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void printServicesRevenuesReport(HttpServletRequest request, HttpServletResponse response, List<DepartmentRevenues> departRevenues, String filename) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\""); // file name
        Document document = new Document();

        try {

            FontSelector fontselector = new FontSelector();
            fontselector.addFont(new Font(FontFamily.COURIER, 6, Font.NORMAL));
            openFile(request, response, document);
            displayHeader(document, fontselector);
            displayDepartRevenues(document, departRevenues, fontselector);
            document.add(new Paragraph("\n"));
            String reporter = "Reporter's names and Signature";
            displayFooter(document, reporter, fontselector);

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void displayPaidServiceRevenues(Document document, BigDecimal amount, List<PaymentRevenue> paymentRevenue, List<BigDecimal> subTotals, BigDecimal bigTotal, BigDecimal totalPaid, FontSelector fontSelector) throws DocumentException {

        document.setPageSize(PageSize.A4.rotate());

        Chunk chk = new Chunk("DETAILED CASHIER DAILY REPORT");
        chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
        chk.setUnderline(0.2f, -2f);
        Paragraph pa = new Paragraph();
        pa.add(chk);
        pa.setAlignment(Element.ALIGN_CENTER);
        document.add(pa);
        document.add(new Paragraph("\n"));

        FontSelector boldFont = new FontSelector();
        boldFont.addFont(new Font(FontFamily.COURIER, 6, Font.BOLD));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        NumberFormat formatter = new DecimalFormat("#0.00");


        Chunk chk1 = new Chunk("Total Cash Collected : " + formatter.format(totalPaid));
        chk1.setFont(new Font(FontFamily.COURIER, 6, Font.BOLD));
        Paragraph pa1 = new Paragraph();
        pa1.add(chk1);
        document.add(pa1);

        document.add(new Paragraph("\n"));

        float[] colsWidt = {20f, 110f, 150f, 80f, 80f};
        for (PaidServiceRevenue n : paymentRevenue.get(0).getPaidServiceRevenues()) {
            colsWidt = Arrays.copyOf(colsWidt, colsWidt.length + 1);
            colsWidt[colsWidt.length - 1] = 80f;
        }

        PdfPTable table = new PdfPTable(colsWidt);
        table.setWidthPercentage(100);

		/*float[] colsWidt = {20f,110f,150f,80f};
		for (PaidServiceRevenue n : paymentRevenue.get(0).getPaidServiceRevenues()) {
			colsWidt = Arrays.copyOf(colsWidt, colsWidt.length + 1);
			colsWidt[colsWidt.length - 1] = 80f;
		}*/

        table = new PdfPTable(colsWidt);
        table.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell(boldFont.process("#"));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("Date"));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("Patient Names"));
        table.addCell(cell);

        for (PaidServiceRevenue dr : paymentRevenue.get(0).getPaidServiceRevenues()) {
            String s = dr.getService();
            if (s.length() > 3 && s.split(" ").length == 1) // if service name is made of letters greater than 3, display the substring from 0 to 4
                s = s.substring(0, 5);
            else if (s.split(" ").length == 2) { // if service name is made of 2 words, display the first word plus the 3 letters of the second
                String[] parts = s.split(" ");
                s = parts[0] + "." + parts[1].substring(0, 3);
            }
            cell = new PdfPCell(boldFont.process("" + s));
            table.addCell(cell);

        }

        cell = new PdfPCell(boldFont.process("TOT.(Due)"));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("TOT.(Paid)"));
        table.addCell(cell);

        int i = 0;
        for (PaymentRevenue dr : paymentRevenue) {
            i++;
            //if(psr!=null&&psr.getPaidAmount().compareTo(BigDecimal.ZERO)!=0){
            cell = new PdfPCell(fontSelector.process("" + i));
            table.addCell(cell);

            cell = new PdfPCell(fontSelector.process("" + dr.getPayment().getDateReceived()));
            table.addCell(cell);

            cell = new PdfPCell(fontSelector.process("" + dr.getBeneficiary().getPatient().getPersonName()));
            table.addCell(cell);

            for (PaidServiceRevenue psr : dr.getPaidServiceRevenues()) {
                cell = new PdfPCell(fontSelector.process("" + formatter.format(psr.getPaidAmount())));
                table.addCell(cell);
            }
            cell = new PdfPCell(boldFont.process("" + formatter.format(dr.getAmount())));
            table.addCell(cell);

            cell = new PdfPCell(boldFont.process("" + formatter.format(dr.getPayment().getAmountPaid())));
            table.addCell(cell);
            //}
        }
        cell = new PdfPCell(boldFont.process(""));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process(""));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("Tot.(Due)"));
        table.addCell(cell);


        for (BigDecimal st : subTotals) {
            cell = new PdfPCell(boldFont.process("" + formatter.format(st)));
            table.addCell(cell);
        }

        cell = new PdfPCell(boldFont.process("" + formatter.format(bigTotal)));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("" + formatter.format(totalPaid)));
        table.addCell(cell);

        document.add(table);
    }

    public void displayDepartRevenues(Document document, List<DepartmentRevenues> departRevenues, FontSelector fontSelector) throws DocumentException {
        document.setPageSize(PageSize.A4.rotate());

        Chunk chk = new Chunk("SERVICES REPORT");
        chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
        chk.setUnderline(0.2f, -2f);
        Paragraph pa = new Paragraph();
        pa.add(chk);
        pa.setAlignment(Element.ALIGN_CENTER);
        document.add(pa);
        document.add(new Paragraph("\n"));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);


        float[] colsWidt = {15f, 45f, 45f};
        for (PaidServiceRevenue n : departRevenues.get(0).getPaidServiceRevenues()) {
            colsWidt = Arrays.copyOf(colsWidt, colsWidt.length + 1);
            colsWidt[colsWidt.length - 1] = 60f;
        }

        table = new PdfPTable(colsWidt);
        table.setWidthPercentage(100);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        FontSelector boldFont = new FontSelector();
        boldFont.addFont(new Font(FontFamily.COURIER, 6, Font.BOLD));

        PdfPCell cell = new PdfPCell(boldFont.process("#"));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("Service"));
        table.addCell(cell);

        for (PaidServiceRevenue dr : departRevenues.get(0).getPaidServiceRevenues()) {
            String s = dr.getService();
            if (s.length() > 3 && s.split(" ").length == 1) // if service name is made of letters greater than 3, display the substring from 0 to 4
                s = s.substring(0, 5);
            else if (s.split(" ").length == 2) { // if service name is made of 2 words, display the first word plus the 3 letters of the second
                String[] parts = s.split(" ");
                s = parts[0] + "." + parts[1].substring(0, 3);
            }
            cell = new PdfPCell(boldFont.process("" + s));
            table.addCell(cell);
        }

        cell = new PdfPCell(boldFont.process("Tot "));
        table.addCell(cell);

        int i = 0;
        for (DepartmentRevenues dr : departRevenues) {
            i++;
            //if(psr!=null&&psr.getPaidAmount().compareTo(BigDecimal.ZERO)!=0){
            cell = new PdfPCell(fontSelector.process("" + i));
            table.addCell(cell);

            cell = new PdfPCell(fontSelector.process("" + dr.getDepartment().getName()));
            table.addCell(cell);

            for (PaidServiceRevenue psr : dr.getPaidServiceRevenues()) {
                cell = new PdfPCell(fontSelector.process("" + psr.getPaidAmount()));
                table.addCell(cell);
            }
            cell = new PdfPCell(fontSelector.process("" + dr.getAmount()));
            table.addCell(cell);
            //}
        }

        document.add(table);
    }

    public void printDepositReport(HttpServletRequest request, HttpServletResponse response, List<Transaction> transactions, String filename) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\""); // file name
        Document document = new Document();

        try {

            FontSelector fontselector = new FontSelector();
            fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
            openFile(request, response, document);
            displayHeader(document, fontselector);
            displayTransactions(document, transactions, fontselector);
            document.add(new Paragraph("\n"));
            String reporter = "Reported by " + Context.getAuthenticatedUser() + " " + Context.getAuthenticatedUser().getPersonName();
            displayFooter(document, reporter, fontselector);

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void displayTransactions(Document document, List<Transaction> transactions, FontSelector fontSelector) throws DocumentException {
        Chunk chk = new Chunk("DEPOSIT REPORT");
        chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
        chk.setUnderline(0.2f, -2f);
        Paragraph pa = new Paragraph();
        pa.add(chk);
        pa.setAlignment(Element.ALIGN_CENTER);
        document.add(pa);
        document.add(new Paragraph("\n"));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100f);


        float[] colsWidt = {5f, 30f, 30f, 30f, 30f, 30f};

        table = new PdfPTable(colsWidt);
        table.setWidthPercentage(100f);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        FontSelector boldFont = new FontSelector();
        boldFont.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));

        PdfPCell cell = new PdfPCell(boldFont.process("#"));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("Date"));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("Collector "));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("Patient Names "));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("Amount "));
        table.addCell(cell);

        cell = new PdfPCell(boldFont.process("Reason "));
        table.addCell(cell);

        int i = 0;
        BigDecimal total = new BigDecimal(0);
        for (Transaction t : transactions) {
            total = total.add(t.getAmount());
            i++;
            //if(psr!=null&&psr.getPaidAmount().compareTo(BigDecimal.ZERO)!=0){
            cell = new PdfPCell(fontSelector.process("" + i));
            table.addCell(cell);

            cell = new PdfPCell(fontSelector.process("" + df.format(t.getTransactionDate())));
            table.addCell(cell);

            cell = new PdfPCell(fontSelector.process("" + t.getCollector().getPersonName()));
            table.addCell(cell);

            cell = new PdfPCell(fontSelector.process("" + t.getPatientAccount().getPatient().getPersonName()));
            table.addCell(cell);

            cell = new PdfPCell(fontSelector.process("" + t.getAmount()));
            table.addCell(cell);

            cell = new PdfPCell(fontSelector.process("" + t.getReason()));
            table.addCell(cell);
            //}
        }
        cell = new PdfPCell(fontSelector.process(""));
        table.addCell(cell);
        cell = new PdfPCell(boldFont.process("TOTAL "));
        table.addCell(cell);
        cell = new PdfPCell(fontSelector.process(""));
        table.addCell(cell);
        cell = new PdfPCell(fontSelector.process(""));
        table.addCell(cell);
        cell = new PdfPCell(boldFont.process("" + total));
        table.addCell(cell);
        cell = new PdfPCell(fontSelector.process(""));
        table.addCell(cell);


        document.add(table);
    }

    public static void exportDataOld(HttpServletRequest request,
                                  HttpServletResponse response,
                                  Insurance insurance,
                                  List<String> columns,
                                  List<AllServicesReportRevenue> listOfAllServicesRevenue)
            throws Exception {

        Date date = new Date();
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        PrintWriter op = response.getWriter();

        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment; filename=\"releve_" + formatter.format(date) + ".csv\"");

        op.println("" + Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME));
        op.println("" + Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS));
        op.println("" + Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL));
        op.println();
        op.println();

        op.println("," + "," + "," + "SUMMARY OF VOUCHERS FOR " + insurance.getName());
        op.println();
        op.println();

        System.out.println("Additional Column count: " + columns.size());

        op.print("#,Admission Date,Closing Date,BENEFICIARY'S NAMES,HEAD HOUSEHOLD'S NAMES,FAMILY'S CODE,LEVEL,Card NUMBER,COMPANY,AGE,BIRTH DATE,GENDER,DOCTOR");
        for (String col : columns) {
            op.print("," + col);
        }
        Float insRate = insurance.getCurrentRate().getRate();

        op.print(",100%");
        op.print("," + insRate + "%");
        op.println("," + (100 - insRate.floatValue()) + "%");

        int i = 0;
        for (AllServicesReportRevenue services : listOfAllServicesRevenue) {

            PatientServiceBillReport report = services.getConsommation();
            //Float insuranceRate = report.getCurrentInsuranceRate();
            Float insuranceRate = insurance.getCurrentRate().getRate();
            Float insuranceDue = services.getAllDueAmounts().floatValue() * insuranceRate / 100;
            Float patientDue = services.getAllDueAmounts().floatValue() * ((100 - insuranceRate) / 100);
            i++;

            op.print(i
                    + "," + formatter.format(report.getAdmissionDate())
                    + "," + formatter.format(report.getClosingDate())
                    + "," + report.getBeneficiaryName()
                    + "," + report.getHouseholdHeadName()
                    + ",'" + report.getFamilyCode()
                    + "," + report.getBeneficiaryLevel()
                    + ",'" + report.getCardNumber()
                    + "," + report.getCompanyName()
                    + "," + report.getAge()
                    + "," + formatter.format(report.getBirthDate())
                    + "," + report.getGender()
                    + "," + report.getDoctorName()

            );

            for (ServiceReportRevenue r : services.getRevenues()) {

                if (r != null && (100 - insuranceRate) != 0.0) {
                    op.print("," + ReportsUtil.roundTwoDecimals(r.getDueAmount().floatValue() * 100 / (100 - insuranceRate)));

                } else if (r != null && (100 - insuranceRate) == 0.0) {

                    BigDecimal amount = new BigDecimal(0);
                    for (PatientServiceBillReport item : r.getBillItems()) {
                        amount = amount.add(BigDecimal.valueOf(item.getServiceBillUnitPrice())
                                .multiply(BigDecimal.valueOf(item.getServiceBillQuantity())));
                    }
                    op.print("," + ReportsUtil.roundTwoDecimals(amount.floatValue()));

                } else {
                    op.print("," + 0);
                }
            }

            op.print("," + ReportsUtil.roundTwoDecimals(services.getAllDueAmounts().doubleValue()) +
                    "," + ReportsUtil.roundTwoDecimals(insuranceDue) +
                    "," + ReportsUtil.roundTwoDecimals(patientDue));

            op.println();
        }
        op.println();

        op.flush();
        op.close();
        System.out.println("Done flushing..");
    }

    //export to excel
    public static void exportData(HttpServletRequest request,
                                  HttpServletResponse response,
                                  Insurance insurance,
                                  List<String> columns,
                                  List<InsuranceReportItem> reportRecords)
            throws Exception {

        Date date = new Date();
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        PrintWriter op = response.getWriter();

        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment; filename=\"releve_" + formatter.format(date) + ".csv\"");

        op.println("" + Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME));
        op.println("" + Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS));
        op.println("" + Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL));
        op.println();
        op.println();

        op.println("," + "," + "," + "SUMMARY OF VOUCHERS FOR " + insurance.getName());
        op.println();
        op.println();

        op.print("#,Admission Date,Closing Date,BENEFICIARY'S NAMES,HEAD HOUSEHOLD'S NAMES,FAMILY'S CODE,LEVEL,Card NUMBER,COMPANY,AGE,BIRTH DATE,GENDER,DOCTOR,MEDICAMENTS,CONSULTATION,HOSPITALISATION,LABORATOIRE,FORMALITES ADMINISTRATIVES,AMBULANCE,CONSOMMABLES,OXYGENOTHERAPIE,IMAGING,PROCED.");

        Float insRate = insurance.getCurrentRate().getRate();

        op.print(",100%");
        op.print("," + insRate + "%");
        op.println("," + (100 - insRate.floatValue()) + "%");

        int i = 0;
        for (InsuranceReportItem reportItem : reportRecords) {

            Float insuranceRate = insurance.getCurrentRate().getRate();
            Float insuranceDue = 0f;
            Float patientDue = 0f;
            i++;

            op.print(i
                    + "," + formatter.format(reportItem.getAdmissionDate())
                    + "," + formatter.format(reportItem.getClosingDate())
                    + "," + reportItem.getBeneficiaryName()
                    + "," + reportItem.getHouseholdHeadName()
                    + ",'" + reportItem.getFamilyCode()
                    + "," + reportItem.getBeneficiaryLevel()
                    + ",'" + reportItem.getCardNumber()
                    + "," + reportItem.getCompanyName()
                    + "," + reportItem.getAge()
                    + "," + formatter.format(reportItem.getBirthDate())
                    + "," + reportItem.getGender()
                    + "," + reportItem.getDoctorName()

                    + "," + ReportsUtil.roundTwoDecimals(reportItem.getMedicament())
                    + "," + ReportsUtil.roundTwoDecimals(reportItem.getConsultation())
                    + "," + ReportsUtil.roundTwoDecimals(reportItem.getHospitalisation())
                    + "," + ReportsUtil.roundTwoDecimals(reportItem.getLaboratoire())
                    + "," + ReportsUtil.roundTwoDecimals(reportItem.getFormaliteAdministratives())
                    + "," + ReportsUtil.roundTwoDecimals(reportItem.getAmbulance())
                    + "," + ReportsUtil.roundTwoDecimals(reportItem.getConsommables())
                    + "," + ReportsUtil.roundTwoDecimals(reportItem.getOxygenotherapie())
                    + "," + ReportsUtil.roundTwoDecimals(reportItem.getImaging())
                    + "," + ReportsUtil.roundTwoDecimals(reportItem.getProced())
            );

            op.print("," + ReportsUtil.roundTwoDecimals(0.00) +
                    "," + ReportsUtil.roundTwoDecimals(insuranceDue) +
                    "," + ReportsUtil.roundTwoDecimals(patientDue));

            op.println();
        }
        op.println();

        op.flush();
        op.close();
        System.out.println("Done flushing..");
    }

    public void epsonPrinter(HttpServletRequest request, HttpServletResponse response, BillPayment payment, String filename) throws Exception {
        Rectangle pagesize = new Rectangle(216f, 1300f);
        Document document = new Document(pagesize, 16f, 16f, 0f, 0f);

        Image image = Image.getInstance(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_LOGO));
        image.scaleToFit(60, 60);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        writer.setBoxSize("art", new Rectangle(0, 0, 2382, 3369));

        HeaderFooter event = new HeaderFooter();
        writer.setPageEvent(event);
        writer.addViewerPreference(PdfName.PRINTSCALING, PdfName.NONE);

        document.open();
        document.setPageSize(PageSize.A4);

        document.addAuthor(Context.getAuthenticatedUser().getPersonName()
                .toString());// the name of the author

        FontSelector fontTitle = new FontSelector();
        fontTitle.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));

        FontSelector fontTotals = new FontSelector();
        fontTotals.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));

        /** ------------- Report title ------------- */

        Chunk chk = new Chunk("Printed on : " + (new Date()));
        chk.setFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
        //Paragraph todayDate = new Paragraph();
        //todayDate.setAlignment(Element.ALIGN_RIGHT);
        //todayDate.add(chk);
        //document.add(todayDate);
        document.add(fontTitle.process("\n"));
        //document.add(fontTitle.process("REPUBLIQUE DU RWANDA\n"));

        //displayHeader(document, fontTitle);
        document.add(fontTitle.process("REPUBLIQUE DU RWANDA" + "\n"));

        document.add(image);
        document.add(fontTitle.process(Context.getAdministrationService().getGlobalProperty(
                BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME) + "\n"));
        document.add(fontTitle.process(Context.getAdministrationService()
                .getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS) + "\n"));
        document.add(fontTitle.process(Context.getAdministrationService().getGlobalProperty(
                BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL) + "\n"));

        /** ------------- End Report title ------------- */
        /** -------------Epson Printer-----------------*/

        Consommation consommation = ConsommationUtil.getConsommationByPatientBill(payment.getPatientBill());
        chk = new Chunk("RECEIPT#" + payment.getBillPaymentId() + " Consommation#" + consommation.getConsommationId() + "-" + payment.getDateReceived());
        chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
        chk.setUnderline(0.2f, -2f);
        Paragraph pa = new Paragraph();
        pa.add(chk);
        pa.setAlignment(Element.ALIGN_CENTER);
        document.add(pa);
        //document.add(new Paragraph("\n"));

        // title row
        FontSelector fontTitleSelector = new FontSelector();
        fontTitleSelector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));

        PdfPTable tableHeader = new PdfPTable(1);
        tableHeader.setWidthPercentage(100f);

        String insuranceDetails = consommation.getBeneficiary().getInsurancePolicy().getInsurance().getName() + "\n" +
                " Card Nbr: " + consommation.getBeneficiary().getInsurancePolicy().getInsuranceCardNo();

        String primarycareId = " Patient_ID: " + consommation.getBeneficiary().getPatient().getPatientIdentifier(patientID);

        String patientDetails = consommation.getBeneficiary().getPatient().getFamilyName() + " "
                + consommation.getBeneficiary().getPatient().getGivenName() + "\n"
                + "( DOB:" + new SimpleDateFormat("dd-MMM-yyyy").format(consommation.getBeneficiary().getPatient().getBirthdate()) + ")";


        document.add(fontTitle.process(insuranceDetails + "\n"));
        document.add(fontTitle.process(primarycareId + "\n"));
        document.add(fontTitle.process(patientDetails + "\n"));
        document.add(fontTitle.process("--------------------\n"));


        // Table of bill items;
        float[] colsWidth = {2f, 15f, 2f, 3.5f, 5f, 5f, 5f};
        PdfPTable table = new PdfPTable(colsWidth);
        table.setWidthPercentage(100f);

        // normal row
        FontSelector fontselector = new FontSelector();
        fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));


        int number = 0;
        PdfPTable serviceTb = new PdfPTable(1);
        serviceTb.setHorizontalAlignment(Element.ALIGN_LEFT);
        serviceTb.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell(fontTitleSelector.process(""));

        NumberFormat formatter = new DecimalFormat("#0.00");
        InsurancePolicy ip = consommation.getBeneficiary().getInsurancePolicy();
        float insuranceRate = ip.getInsurance().getCurrentRate().getRate();
        float patRate = 100f - insuranceRate;
        if (ip.getThirdParty() != null) {
            float thirdPartRate = ip.getThirdParty().getRate();
            patRate = 100f - insuranceRate - thirdPartRate;
        }
        BigDecimal patientRate = new BigDecimal("" + patRate);

        List<PaidServiceBill> paidItems = BillPaymentUtil.getPaidItemsByBillPayment(payment);
        String itemNamep;
        BigDecimal unitPricep;
        BigDecimal unitPrice, paidQty, itemPaidCost = new BigDecimal(0.0);
        BigDecimal totalToBePaidByPatient = new BigDecimal(0.0);
        BigDecimal totalPaid = new BigDecimal(0.0);
        String drudFrequency;

        if (consommation.getGlobalBill().getBillIdentifier().substring(0, 4).equals("bill")) {
            paidItems = BillPaymentUtil.getOldPayments(payment);
        }

        for (PatientServiceBill psbitems : consommation.getBillItems()) {
            if (!psbitems.getVoided()) {
                number = number + 1;
                BigDecimal itemCostp = psbitems.getQuantity().multiply(psbitems.getUnitPrice()).multiply(patientRate).divide(new BigDecimal(100));
                itemNamep = psbitems.getService().getFacilityServicePrice().getName();
                unitPricep = psbitems.getUnitPrice().multiply(patientRate).divide(new BigDecimal(100));
                drudFrequency = psbitems.getDrugFrequency();
                String itemPaymentStatus = null;
                if (psbitems.isPaid()) {
                    itemPaymentStatus = "Paid";
                } else {
                    itemPaymentStatus = "Unpaid";
                }
                String itemDetailsp = number + ")" + itemNamep + " [" + drudFrequency + "] " + formatter.format(unitPricep) + " X " + psbitems.getQuantity() + " = " + formatter.format(itemCostp) + " (" + itemPaymentStatus + ")";

                cell = new PdfPCell(fontTitleSelector.process("" + itemDetailsp));
                cell.setBorder(Rectangle.NO_BORDER);
                serviceTb.addCell(cell);
            }
        }

		/*for (PaidServiceBill paidItem: paidItems) {
			number += 1;

			BigDecimal itemCost = paidItem.getBillItem().getQuantity().multiply(paidItem.getBillItem().getUnitPrice().multiply(patientRate).divide(new BigDecimal(100)));
			totalToBePaidByPatient=totalToBePaidByPatient.add(itemCost);

			BigDecimal paid= paidItem.getBillItem().getPaidQuantity().multiply(itemCost);
			//totalPaid=totalPaid.add(paid);

			itemName = paidItem.getBillItem().getService().getFacilityServicePrice().getName();
			unitPrice = paidItem.getBillItem().getUnitPrice().multiply(patientRate).divide(new BigDecimal(100));
			paidQty = paidItem.getPaidQty();
			itemPaidCost = paid;


			String itemDetails = number+")"+itemName +" "+unitPrice +" X "+paidQty+" = "+formatter.format(unitPrice.multiply(paidQty));


			cell = new PdfPCell(fontTitleSelector.process(""+itemDetails));
			cell.setBorder(Rectangle.NO_BORDER);
			serviceTb.addCell(cell);
		}*/
        document.add(serviceTb);

        document.add(fontTitle.process("--------------------\n"));

        FontSelector boldFont = new FontSelector();
        boldFont.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));

        for (BillPayment pay : consommation.getPatientBill().getPayments()) {
            if (pay.getVoidReason() == null) {

                totalPaid = totalPaid.add(pay.getAmountPaid());
            }
        }

        BigDecimal totalDueAmount = new BigDecimal(0);
        BigDecimal total = new BigDecimal(0);
        for (PatientServiceBill item : consommation.getBillItems()) {
            if (!item.getVoided()) {
                total = total.add(item.getQuantity().multiply(item.getUnitPrice()));
                totalDueAmount = total.multiply(patientRate).divide(new BigDecimal(100));
            }
        }

        String rest = formatter.format((totalDueAmount).subtract(totalPaid));
        document.add(fontTotals.process("Due Amount: " + ReportsUtil.roundTwoDecimals(totalDueAmount.doubleValue()) + "\n"));
        document.add(fontTotals.process("Paid: " + ReportsUtil.roundTwoDecimals(totalPaid.doubleValue()) + "\n"));
        if (consommation.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getFlatFee() != null) {
            document.add(fontTotals.process("Rest :" + "0.00"));
        } else {
            document.add(fontTotals.process("Rest :" + rest));
        }

        // Table of signatures;
        table = new PdfPTable(1);
        table.setWidthPercentage(100f);

        cell = new PdfPCell(fontTitleSelector.process("Patient Signature: " + consommation.getBeneficiary().getPatient().getPersonName() + "........\n"));
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);

        //prestataire/health care provider is still under discussion, so the following codes will help provide space for his/her signature
			/*cell = new PdfPCell(fontTitleSelector.process("Prestataire:............... \n\n"));
			cell = new PdfPCell(fontTitleSelector.process(" \n\n"));
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);*/

        cell = new PdfPCell(fontTitleSelector.process("Cashier Signature: " + Context.getAuthenticatedUser().getPersonName() + "........."));
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);

        document.add(table);

        document.close();

        // Mark the Bill as Printed
        //PatientBillUtil.printBill(pb);
    }

    public void displayFooter(Document document, Patient patient, User user, String other, FontSelector fontselector, User cashier) throws DocumentException {
        // Table of signatures;
        PdfPTable table1 = new PdfPTable(3);
        table1.setWidthPercentage(100f);

        PdfPCell cell = new PdfPCell(fontselector.process(""));
        if (user != null) {
            cell = new PdfPCell(fontselector.process("Cashier Signature \n" + cashier.getPersonName() + "\n\n........................."));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);
        }

        //for report where a patient has to sign on it
        if (patient != null) {
            cell = new PdfPCell(fontselector.process("                      "));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);

            cell = new PdfPCell(fontselector.process("Beneficiary Signature \n" + patient.getPersonName() + "\n\n........................."));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);


        }

        if (other != null) {
            cell = new PdfPCell(fontselector.process("                      "));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);

            cell = new PdfPCell(fontselector.process(other + "\n\n........................."));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);
        }

        document.add(table1);
    }

    public void displayFooter(Document document, Patient patient, User user, String other, FontSelector fontselector) throws DocumentException {
        // Table of signatures;
        PdfPTable table1 = new PdfPTable(3);
        table1.setWidthPercentage(100f);

        PdfPCell cell = new PdfPCell(fontselector.process(""));
        if (user != null) {
            cell = new PdfPCell(fontselector.process("Cashier Signature \n" + user.getPersonName() + "\n\n........................."));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);
        }

        //for report where a patient has to sign on it
        if (patient != null) {
            cell = new PdfPCell(fontselector.process("                      "));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);

            cell = new PdfPCell(fontselector.process("Beneficiary Signature \n" + patient.getPersonName() + "\n\n........................."));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);


        }

        if (other != null) {
            cell = new PdfPCell(fontselector.process("                      "));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);

            cell = new PdfPCell(fontselector.process(other + "\n\n........................."));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);
        }

        document.add(table1);
    }

    public void displayFooter(Document document, Patient patient, User createdBy, User generatedBy, String other, FontSelector fontselector) throws DocumentException {
        // Table of signatures;
        PdfPTable table1 = new PdfPTable(4);
        table1.setWidthPercentage(100f);

        PdfPCell cell = new PdfPCell(fontselector.process(""));
        if (generatedBy != null) {
            cell = new PdfPCell(fontselector.process("Generated by: \n" + generatedBy.getPersonName() + "\n\n........................."));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);
        }


        cell = new PdfPCell(fontselector.process("Discharge by (Doctor): \n" + createdBy.getPersonName() + "\n\n........................."));
        cell.setBorder(Rectangle.NO_BORDER);
        table1.addCell(cell);


        //for report where a patient has to sign on it
        if (patient != null) {

            cell = new PdfPCell(fontselector.process("Beneficiary Names/Signature: \n\n........................."));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);


        }
        cell = new PdfPCell(fontselector.process("Insurance's Staff: \n\n........................."));
        cell.setBorder(Rectangle.NO_BORDER);
        table1.addCell(cell);

        if (other != null) {
            cell = new PdfPCell(fontselector.process("                      "));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);

            cell = new PdfPCell(fontselector.process(other + "\n\n........................."));
            cell.setBorder(Rectangle.NO_BORDER);
            table1.addCell(cell);
        }

        document.add(table1);
    }

    public void displayTransactionEpson(HttpServletResponse response, Transaction transaction, String filename) throws Exception {

        Rectangle pagesize = new Rectangle(200f, 500f);
        Document document = new Document(pagesize, 16f, 16f, 0f, 0f);

        Image image = Image.getInstance(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_LOGO));
        image.scaleToFit(60, 60);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        writer.setBoxSize("art", new Rectangle(0, 0, 1000, 2000));

        HeaderFooter event = new HeaderFooter();
        writer.setPageEvent(event);
        writer.addViewerPreference(PdfName.PRINTSCALING, PdfName.NONE);

        document.open();
        document.setPageSize(PageSize.A4);

        document.addAuthor(Context.getAuthenticatedUser().getPersonName()
                .toString());// the name of the author

        FontSelector fontTitle = new FontSelector();
        fontTitle.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));

        FontSelector fontTotals = new FontSelector();
        fontTotals.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));

        /** ------------- Report title ------------- */

        Chunk chk = new Chunk("Printed on : " + (new Date()));
        chk.setFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
        document.add(fontTitle.process("\n"));
        document.add(fontTitle.process("REPUBLIQUE DU RWANDA" + "\n"));
        document.add(image);
        document.add(fontTitle.process(Context.getAdministrationService().getGlobalProperty(
                BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME) + "\n"));
        document.add(fontTitle.process(Context.getAdministrationService()
                .getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS) + "\n"));
        document.add(fontTitle.process(Context.getAdministrationService().getGlobalProperty(
                BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL) + "\n"));

        /** ------------- End Report title ------------- */

        chk = new Chunk("RECU POUR CAUTION #" + transaction.getTransactionId() + "\n\n");
        chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
        //chk.setUnderline(0.2f, -2f);
        Paragraph pa = new Paragraph();
        pa.add(chk);
        pa.setAlignment(Element.ALIGN_LEFT);
        document.add(pa);

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100f);

        FontSelector fontTitleSelector = new FontSelector();
        fontTitleSelector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));

        FontSelector normal = new FontSelector();
        normal.addFont(new Font(FontFamily.COURIER, 8f, Font.NORMAL));

        PdfPCell c1 = new PdfPCell(normal.process("Amount : " + transaction.getAmount().abs()));
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);

        c1 = new PdfPCell(normal.process("Received from :  " + transaction.getPatientAccount().getPatient().getPersonName()));
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);

        c1 = new PdfPCell(normal.process("Patient_ID:  " + transaction.getPatientAccount().getPatient().getPatientIdentifier(patientID)));
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);

        c1 = new PdfPCell(normal.process("Received by : " + transaction.getCollector().getPersonName()));
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);

        c1 = new PdfPCell(normal.process("For : " + transaction.getReason()));
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);

        c1 = new PdfPCell(normal.process("Receipt No : " + transaction.getTransactionId()));
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);

        c1 = new PdfPCell(normal.process("Date : " + transaction.getCreatedDate()));
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);

        c1 = new PdfPCell(normal.process("Cashier Signature\n : " + transaction.getCollector().getPersonName() + "\n\n........................."));
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);

        c1 = new PdfPCell(normal.process("Beneficiary Signature\n : " + transaction.getPatientAccount().getPatient().getPersonName() + "\n\n........................."));
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);

        document.add(table);

        document.close();
    }

    public void printPaymentPaid(HttpServletRequest request, HttpServletResponse response, BillPayment payment, Consommation consommation, String filename) throws Exception {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        Document document = new Document();

        try {

            FontSelector fontselector = new FontSelector();
            fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
            openFile(request, response, document);
            displayHeader(document, fontselector);
            printerPaidItems(document, payment, consommation, fontselector);
            document.add(new Paragraph("............................................................................................................................................................\n"));
            displayAllItems(document, consommation, fontselector);
            document.add(new Paragraph("\n"));
            // displayFooter(document,consommation.getBeneficiary().getPatient(), fontselector);
            displayFooter(document, consommation.getBeneficiary().getPatient(), Context.getAuthenticatedUser(), null, fontselector, consommation, payment);

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void epsonPrinterPaidItems(HttpServletRequest request, HttpServletResponse response, BillPayment payment, String filename) throws Exception {
        Rectangle pagesize = new Rectangle(216f, 1300f);
        Document document = new Document(pagesize, 16f, 16f, 0f, 0f);

        Image image = Image.getInstance(Context.getAdministrationService().getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_LOGO));
        image.scaleToFit(60, 60);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        writer.setBoxSize("art", new Rectangle(0, 0, 2382, 3369));

        HeaderFooter event = new HeaderFooter();
        writer.setPageEvent(event);
        writer.addViewerPreference(PdfName.PRINTSCALING, PdfName.NONE);

        document.open();
        document.setPageSize(PageSize.A4);

        document.addAuthor(Context.getAuthenticatedUser().getPersonName()
                .toString());// the name of the author

        FontSelector fontTitle = new FontSelector();
        fontTitle.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));

        FontSelector fontTotals = new FontSelector();
        fontTotals.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));

        /** ------------- Report title ------------- */

        Chunk chk = new Chunk("Printed on : " + (new Date()));
        chk.setFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));
        //Paragraph todayDate = new Paragraph();
        //todayDate.setAlignment(Element.ALIGN_RIGHT);
        //todayDate.add(chk);
        //document.add(todayDate);
        document.add(fontTitle.process("\n"));
        //document.add(fontTitle.process("REPUBLIQUE DU RWANDA\n"));

        //displayHeader(document, fontTitle);
        document.add(fontTitle.process("REPUBLIQUE DU RWANDA" + "\n"));

        document.add(image);
        document.add(fontTitle.process(Context.getAdministrationService().getGlobalProperty(
                BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_NAME) + "\n"));
        document.add(fontTitle.process(Context.getAdministrationService()
                .getGlobalProperty(BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_PHYSICAL_ADDRESS) + "\n"));
        document.add(fontTitle.process(Context.getAdministrationService().getGlobalProperty(
                BillingConstants.GLOBAL_PROPERTY_HEALTH_FACILITY_EMAIL) + "\n"));

        /** ------------- End Report title ------------- */
        /** -------------Epson Printer-----------------*/

        Consommation consommation = ConsommationUtil.getConsommationByPatientBill(payment.getPatientBill());
        chk = new Chunk("RECEIPT#" + payment.getBillPaymentId() + " Consommation#" + consommation.getConsommationId() + "-" + payment.getDateReceived());
        chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
        chk.setUnderline(0.2f, -2f);
        Paragraph pa = new Paragraph();
        pa.add(chk);
        pa.setAlignment(Element.ALIGN_CENTER);
        document.add(pa);
        //document.add(new Paragraph("\n"));

        // title row
        FontSelector fontTitleSelector = new FontSelector();
        fontTitleSelector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));

        PdfPTable tableHeader = new PdfPTable(1);
        tableHeader.setWidthPercentage(100f);

        String insuranceDetails = consommation.getBeneficiary().getInsurancePolicy().getInsurance().getName() + "\n" +
                " Card Nbr: " + consommation.getBeneficiary().getInsurancePolicy().getInsuranceCardNo();

        String primarycareId = " Patient_ID: " + consommation.getBeneficiary().getPatient().getPatientIdentifier(patientID);

        String patientDetails = consommation.getBeneficiary().getPatient().getFamilyName() + " "
                + consommation.getBeneficiary().getPatient().getGivenName() + "\n"
                + "( DOB:" + new SimpleDateFormat("dd-MMM-yyyy").format(consommation.getBeneficiary().getPatient().getBirthdate()) + ")";


        document.add(fontTitle.process(insuranceDetails + "\n"));
        document.add(fontTitle.process(primarycareId + "\n"));
        document.add(fontTitle.process(patientDetails + "\n"));
        document.add(fontTitle.process("--------------------\n"));


        // Table of bill items;
        float[] colsWidth = {2f, 15f, 2f, 3.5f, 5f, 5f, 5f};
        PdfPTable table = new PdfPTable(colsWidth);
        table.setWidthPercentage(100f);

        // normal row
        FontSelector fontselector = new FontSelector();
        fontselector.addFont(new Font(FontFamily.COURIER, 8, Font.NORMAL));


        int number = 0;
        PdfPTable serviceTb = new PdfPTable(1);
        serviceTb.setHorizontalAlignment(Element.ALIGN_LEFT);
        serviceTb.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell(fontTitleSelector.process(""));

        NumberFormat formatter = new DecimalFormat("#0.0");
        NumberFormat formatterUnitPrice = new DecimalFormat("#0.0");

        InsurancePolicy ip = consommation.getBeneficiary().getInsurancePolicy();
        float insuranceRate = ip.getInsurance().getCurrentRate().getRate();
        float patRate = 100f - insuranceRate;
        if (ip.getThirdParty() != null) {
            float thirdPartRate = ip.getThirdParty().getRate();
            patRate = 100f - insuranceRate - thirdPartRate;
        }
        BigDecimal patientRate = new BigDecimal("" + patRate);

        List<PaidServiceBill> paidItems = BillPaymentUtil.getPaidItemsByBillPayment(payment);
        String itemName;
        //BigDecimal unitPricep;
        BigDecimal unitPrice, paidQty, itemPaidCost = new BigDecimal(0.0);
        BigDecimal totalToBePaidByPatient = new BigDecimal(0.0);
        BigDecimal totalPaid = new BigDecimal(0.0);
        String drudFrequency;

        if (consommation.getGlobalBill().getBillIdentifier().substring(0, 4).equals("bill")) {
            paidItems = BillPaymentUtil.getOldPayments(payment);
        }

/*		for(PatientServiceBill psbitems : consommation.getBillItems()) {
			if (!psbitems.isVoided()) {
				number = number + 1;
				BigDecimal itemCostp = psbitems.getQuantity().multiply(psbitems.getUnitPrice()).multiply(patientRate).divide(new BigDecimal(100));
				itemNamep = psbitems.getService().getFacilityServicePrice().getName();
				unitPricep = psbitems.getUnitPrice().multiply(patientRate).divide(new BigDecimal(100));
				drudFrequency= psbitems.getDrugFrequency();
				String itemPaymentStatus=null;
				if(psbitems.isPaid()){
					itemPaymentStatus="Paid";
				}
				else {
					itemPaymentStatus="Unpaid";
				}
				String itemDetailsp = number + ")" + itemNamep+" "+drudFrequency+ " " + formatter.format(unitPricep) + " X " + psbitems.getQuantity() + " = " + formatter.format(itemCostp) + " (" + itemPaymentStatus + " )";

				cell = new PdfPCell(fontTitleSelector.process(""+itemDetailsp));
				cell.setBorder(Rectangle.NO_BORDER);
				serviceTb.addCell(cell);
			}
		}*/

        for (PaidServiceBill paidItem : paidItems) {
            number += 1;

            BigDecimal itemCost = paidItem.getBillItem().getQuantity().multiply(paidItem.getBillItem().getUnitPrice().multiply(patientRate).divide(new BigDecimal(100)));
            totalToBePaidByPatient = totalToBePaidByPatient.add(itemCost);

            BigDecimal paid = paidItem.getBillItem().getPaidQuantity().multiply(itemCost);
            //totalPaid=totalPaid.add(paid);

            itemName = paidItem.getBillItem().getService().getFacilityServicePrice().getName();
            unitPrice = paidItem.getBillItem().getUnitPrice().multiply(patientRate).divide(new BigDecimal(100));
            paidQty = paidItem.getPaidQty();
            drudFrequency = paidItem.getBillItem().getDrugFrequency();
            itemPaidCost = paid;
            String itemPaymentStatus = "";
            if (paidItem.getBillItem().isPaid()) {
                itemPaymentStatus = "Paid";
            } else {
                itemPaymentStatus = "Unpaid";
            }

            String itemDetails = number + ")" + itemName + " [" + drudFrequency + "] " + formatterUnitPrice.format(unitPrice) + " X " + paidQty + " = " + formatter.format(unitPrice.multiply(paidQty)) + "(" + itemPaymentStatus + ")";


            cell = new PdfPCell(fontTitleSelector.process("" + itemDetails));
            cell.setBorder(Rectangle.NO_BORDER);
            serviceTb.addCell(cell);
        }
        document.add(serviceTb);

        document.add(fontTitle.process("--------------------\n"));

        FontSelector boldFont = new FontSelector();
        boldFont.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));

        for (BillPayment pay : consommation.getPatientBill().getPayments()) {
            if (pay.getVoidReason() == null) {

                totalPaid = totalPaid.add(pay.getAmountPaid());
            }
        }

        BigDecimal totalDueAmount = new BigDecimal(0);
        BigDecimal total = new BigDecimal(0);
        for (PatientServiceBill item : consommation.getBillItems()) {
            if (!item.getVoided()) {
                total = total.add(item.getQuantity().multiply(item.getUnitPrice()));
                totalDueAmount = total.multiply(patientRate).divide(new BigDecimal(100));
            }
        }

        String rest = formatter.format((totalDueAmount).subtract(totalPaid));
        document.add(fontTotals.process("Due Amount: " + ReportsUtil.roundTwoDecimals(totalDueAmount.doubleValue()) + "\n"));
        document.add(fontTotals.process("Paid: " + ReportsUtil.roundTwoDecimals(totalPaid.doubleValue()) + "\n"));
        if (consommation.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getFlatFee() != null) {
            document.add(fontTotals.process("Rest :" + "0.00"));
        } else {
            document.add(fontTotals.process("Rest :" + rest));
        }

        // Table of signatures;
        table = new PdfPTable(1);
        table.setWidthPercentage(100f);

        cell = new PdfPCell(fontTitleSelector.process("Patient Signature: " + consommation.getBeneficiary().getPatient().getPersonName() + "........\n"));
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);

        //prestataire/health care provider is still under discussion, so the following codes will help provide space for his/her signature
			/*cell = new PdfPCell(fontTitleSelector.process("Prestataire:............... \n\n"));
			cell = new PdfPCell(fontTitleSelector.process(" \n\n"));
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);*/

        cell = new PdfPCell(fontTitleSelector.process("Cashier Signature: " + Context.getAuthenticatedUser().getPersonName() + "........."));
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);

        document.add(table);

        document.close();

        // Mark the Bill as Printed
        //PatientBillUtil.printBill(pb);
    }

    public void printerPaidItems(Document document, BillPayment payment, Consommation consommation, FontSelector fontSelector) throws DocumentException {
        NumberFormat formatter = new DecimalFormat("#0.0");
        NumberFormat formatterUnitPrice = new DecimalFormat("#0.0");

        Float insuranceRate = consommation.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getRate();
        float patRate = 100f - insuranceRate;
        BigDecimal patientRate = new BigDecimal("" + patRate);

        Chunk chk = new Chunk("RECEIPT#" + payment.getBillPaymentId() + "BILL#" + consommation.getConsommationId() + " GB#" + consommation.getGlobalBill().getBillIdentifier() + " - " + payment.getDateReceived() + " Patient_ID: " + consommation.getBeneficiary().getPatient().getPatientIdentifier(patientID));
        chk.setFont(new Font(FontFamily.COURIER, 8, Font.BOLD));
        chk.setUnderline(0.2f, -2f);
        Paragraph pa = new Paragraph();
        pa.add(chk);
        pa.setAlignment(Element.ALIGN_CENTER);
        document.add(pa);
        document.add(new Paragraph("\n"));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100f);


        FontSelector normal = new FontSelector();
        normal.addFont(new Font(FontFamily.COURIER, 8f, Font.NORMAL));

        FontSelector boldFont = new FontSelector();
        boldFont.addFont(new Font(FontFamily.COURIER, 8, Font.BOLD));

        PdfPCell cell = null;

        int number = 0;

        PdfPTable tableLeft = new PdfPTable(1);
        tableLeft.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPTable tableRight = new PdfPTable(1);
        tableRight.setHorizontalAlignment(Element.ALIGN_RIGHT);

        String drugFrequency;
        String itemName;
        BigDecimal unitPrice, paidQty, itemPaidCost = new BigDecimal(0.0);
        BigDecimal totalToBePaidByPatient = new BigDecimal(0.0);
        BigDecimal totalPaid = new BigDecimal(0.0);
        List<PaidServiceBill> paidItems = BillPaymentUtil.getPaidItemsByBillPayment(payment);

        if (consommation.getGlobalBill().getBillIdentifier().substring(0, 4).equals("bill")) {
            paidItems = BillPaymentUtil.getOldPayments(payment);
        }

        for (PaidServiceBill service : paidItems) {
            number++;

            BigDecimal itemCost = service.getBillItem().getQuantity().multiply(service.getBillItem().getUnitPrice().multiply(patientRate).divide(new BigDecimal(100)));
            totalToBePaidByPatient = totalToBePaidByPatient.add(itemCost);

            BigDecimal paid = service.getBillItem().getPaidQuantity().multiply(itemCost);
            //totalPaid=totalPaid.add(paid);
            drugFrequency = service.getBillItem().getDrugFrequency();
            itemName = service.getBillItem().getService().getFacilityServicePrice().getName();
            unitPrice = service.getBillItem().getUnitPrice().multiply(patientRate).divide(new BigDecimal(100));
            paidQty = service.getPaidQty();
            itemPaidCost = paid;
            String itemPaymentStatus = "";
            if (service.getBillItem().isPaid()) {
                itemPaymentStatus = "Paid";
            } else {
                itemPaymentStatus = "Unpaid";
            }

            String itemDetails = number + ")" + itemName + " [" + drugFrequency + "] " + formatterUnitPrice.format(unitPrice) + " X " + paidQty + " = " + formatter.format(unitPrice.multiply(paidQty)) + "(" + itemPaymentStatus + ")";

            cell = new PdfPCell(fontSelector.process("" + itemDetails));
            cell.setBorder(Rectangle.NO_BORDER);
            if (number % 2 != 0) tableRight.addCell(cell);
            else tableLeft.addCell(cell);
        }
        PdfPCell c = new PdfPCell(tableRight);
        c.setBorder(Rectangle.NO_BORDER);
        table.addCell(c);

        c = new PdfPCell(tableLeft);
        c.setBorder(Rectangle.NO_BORDER);
        table.addCell(c);

        document.add(table);

        PdfPTable serviceTotPat = new PdfPTable(4);

        for (BillPayment pay : consommation.getPatientBill().getPayments()) {
            if (pay.getVoidReason() == null) {

                totalPaid = totalPaid.add(pay.getAmountPaid());
            }
        }

        BigDecimal totale = new BigDecimal(0);
        BigDecimal totalDueAmount = new BigDecimal(0);
        for (PatientServiceBill item : consommation.getBillItems()) {
            if (!item.getVoided()) {

                totale = totale.add(item.getQuantity().multiply(item.getUnitPrice()));
                totalDueAmount = totale.multiply(patientRate).divide(new BigDecimal(100));
            }
        }

        PdfPCell c1 = new PdfPCell(boldFont.process("Due Amount: " + formatter.format(totalDueAmount)));

        c1.setBorder(Rectangle.NO_BORDER);
        serviceTotPat.addCell(c1);

        c1 = new PdfPCell(boldFont.process("Paid: " + payment.getAmountPaid()));
        if (consommation.getGlobalBill().getBillIdentifier().substring(0, 4).equals("bill")) {
            c1 = new PdfPCell(boldFont.process("Paid: " + formatter.format(payment.getAmountPaid())));
        }
        c1.setBorder(Rectangle.NO_BORDER);
        serviceTotPat.addCell(c1);

        c1 = new PdfPCell(boldFont.process(""));
        c1.setBorder(Rectangle.NO_BORDER);
        serviceTotPat.addCell(c1);

        String rest = formatter.format((totalDueAmount).subtract(totalPaid));
		/*if(rest.compareTo(BigDecimal.ZERO)<0)
		c1 = new PdfPCell(boldFont.process("Rest: "+0));
		else
			c1 = new PdfPCell(boldFont.process("Rest: "+consommation.getPatientBill().getAmount().subtract(totalPaid)));*/
        if (consommation.getBeneficiary().getInsurancePolicy().getInsurance().getCurrentRate().getFlatFee() != null) {
            c1 = new PdfPCell(boldFont.process("Rest: " + rest));
        } else {
            c1 = new PdfPCell(boldFont.process("Rest: " + rest));
        }
        c1.setBorder(Rectangle.NO_BORDER);
        serviceTotPat.addCell(c1);

        document.add(serviceTotPat);

        //displayFooter(document, consommation.getBeneficiary().getPatient(), fontSelector);
        User user = Context.getAuthenticatedUser();
        displayFooter(document, consommation.getBeneficiary().getPatient(), user, null, fontSelector);
    }
}
