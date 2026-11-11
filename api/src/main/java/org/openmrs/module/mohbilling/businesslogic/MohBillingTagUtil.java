/**
 *
 */
package org.openmrs.module.mohbilling.businesslogic;

import org.openmrs.api.context.Context;
import org.openmrs.module.mohbilling.GlobalPropertyConfig;
import org.openmrs.module.mohbilling.model.*;
import org.openmrs.module.mohbilling.service.BillingService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * @author @EMR RBC
 */
public class MohBillingTagUtil {

    private static final int AMOUNT_SCALE = 2;

    public static String getTotalAmountPaidByPatientBill(Integer consommationId) {
        if (null == consommationId) {
            return "0";
        }
        try {
            Consommation consomm = Context.getService(BillingService.class).getConsommation(consommationId);
            if (consomm == null || consomm.getPatientBill() == null) {
                return "0";
            }

            BigDecimal amountPaid = BigDecimal.ZERO;
            Set<BillPayment> allPayments = consomm.getPatientBill().getPayments();
            if (allPayments != null) {
                for (BillPayment billPayment : allPayments) {
                    if (billPayment.getVoidReason() == null && billPayment.getAmountPaid() != null) {
                        amountPaid = amountPaid.add(billPayment.getAmountPaid());
                    }
                }
            }
            return formatAmount(amountPaid);
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    /**
     * Gets the REST of the whole Patient Bill
     *
     * @param patientBillId the patient bill ID
     * @return the REST that is in String
     */
    public static String getTotalAmountNotPaidByPatientBill(Integer consommationId) {

        double amountNotPaid = 0d;

        if (null == consommationId)
            return "";
        else {
            try {
                double amountPaid = 0d;
                Consommation consomm = Context.getService(BillingService.class).getConsommation(consommationId);
                Float insuranceRate = consomm.getBeneficiary().getInsurancePolicy()
                        .getInsurance().getCurrentRate().getRate();
                Float patientRate = (100f - insuranceRate) / 100f;

                double amountDueByPatient = 0.0; //get the due Amount from patientBill

                for (PatientServiceBill psb : consomm.getBillItems()) {
                    Double cost = null;

                    if (psb.getVoided() == false) {
                        cost = psb.getUnitPrice().doubleValue() * psb.getQuantity().doubleValue();
                        amountDueByPatient += cost * patientRate.doubleValue();
                    }
                }

                for (BillPayment bp : consomm.getPatientBill().getPayments()) {
                    if (bp.getVoidReason() == null)
                        amountPaid = amountPaid + bp.getAmountPaid().doubleValue();
                }

                if (consomm.getBeneficiary().getInsurancePolicy().getThirdParty() == null) {
                    amountNotPaid = amountDueByPatient - amountPaid;

                } else {

                    double amountPaidByThirdPart = consomm.getThirdPartyBill().getAmount().doubleValue();

                    amountNotPaid = amountDueByPatient - (amountPaidByThirdPart + amountPaid);

                }

            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

        /** Rounding the value to 2 decimals */
        double roundedAmountNotPaid = Math.round(amountNotPaid * 100);
        roundedAmountNotPaid = roundedAmountNotPaid / 100;

        return "" + roundedAmountNotPaid;
    }

    public static String getAmountPaidByThirdPart(Integer consommationId) {

        Double amountPaidByThirdPart = 0d;
        if (consommationId == null)
            return "";
        else {
            try {

                Consommation consomm = Context.getService(BillingService.class).getConsommation(consommationId);
                amountPaidByThirdPart = +consomm.getThirdPartyBill().getAmount().doubleValue();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return "" + amountPaidByThirdPart;
    }

    public static String getGlobalPaidAmountFromGlobalBill(Integer globalBillId) {
        GlobalBill globalBill = Context.getService(BillingService.class).GetGlobalBill(globalBillId);
        if (globalBill == null) {
            return "0";
        }

        List<Consommation> consommations =
                Context.getService(BillingService.class).getAllConsommationByGlobalBill(globalBill);
        if (consommations == null) {
            return "0";
        }

        BigDecimal allPaidAmount = BigDecimal.ZERO;
        for (Consommation consommation : consommations) {
            if (consommation == null || consommation.getConsommationId() == null) {
                continue;
            }
            String paidAmount = getTotalAmountPaidByPatientBill(consommation.getConsommationId());
            if (paidAmount != null && paidAmount.trim().length() > 0) {
                try {
                    allPaidAmount = allPaidAmount.add(new BigDecimal(paidAmount.trim()));
                } catch (NumberFormatException ignored) {
                    // skip malformed tag output
                }
            }
        }
        return formatAmount(allPaidAmount);
    }

    public static String getServicesByDepartment(Integer departmentId) {
        Department department = DepartementUtil.getDepartement(departmentId);
        List<HopService> services = new ArrayList<HopService>();
        if (GlobalPropertyConfig.getListOfHopServicesByDepartment1(department) != null) {
            String[] servicesByDepartStr = GlobalPropertyConfig.getListOfHopServicesByDepartment1(department).split(
                    ",");
            for (String s : servicesByDepartStr) {
                if (s != null && !s.equals(""))
                    services.add(HopServiceUtil.getHopServiceById(Integer.valueOf(s)));
            }
        }
        return "" + services.size();
    }


    public static String getConsommationStatus(Integer id) {
        return ConsommationUtil.getConsommationStatus(id);
    }

    public static String getTotalPaidByConsommation(Integer consommationId) {
        if (consommationId == null) {
            return "0";
        }
        Consommation c = ConsommationUtil.getConsommation(consommationId);
        if (c == null || c.getPatientBill() == null || c.getPatientBill().getPayments() == null) {
            return "0";
        }
        BigDecimal totalPaid = BigDecimal.ZERO;
        for (BillPayment pay : c.getPatientBill().getPayments()) {
            if (pay.getVoidReason() == null && pay.getAmountPaid() != null) {
                totalPaid = totalPaid.add(pay.getAmountPaid());
            }
        }
        return formatAmount(totalPaid);
    }

    public static String getBillStatus(Long totalPaid, Long dueToPatient) {
        if (totalPaid == null || dueToPatient == null) {
            return "N/A";
        }

        if (dueToPatient <= 0) {
            return "FULLY PAID";
        }
        if (totalPaid == 0) {
            return "UNPAID";
        } else if (totalPaid < dueToPatient) {
            return "PARTLY PAID";
        }

        return "FULLY PAID";
    }

    /**
     * Status helper that preserves cents (preferred over Long-based comparison).
     */
    public static String getBillStatus(Double totalPaid, Double dueToPatient) {
        if (totalPaid == null || dueToPatient == null) {
            return "N/A";
        }
        if (dueToPatient <= 0) {
            return "FULLY PAID";
        }
        if (totalPaid <= 0) {
            return "UNPAID";
        } else if (totalPaid < dueToPatient) {
            return "PARTLY PAID";
        }
        return "FULLY PAID";
    }

    private static String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        return amount.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP).toPlainString();
    }

}
