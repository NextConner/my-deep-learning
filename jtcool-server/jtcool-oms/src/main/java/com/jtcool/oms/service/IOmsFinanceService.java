package com.jtcool.oms.service;

import com.jtcool.oms.domain.OmsFinance;
import com.jtcool.oms.domain.OmsPayment;
import java.util.List;

public interface IOmsFinanceService {
    List<OmsFinance> selectOmsFinanceList(OmsFinance omsFinance);
    OmsFinance selectOmsFinanceById(Long financeId);
    int createFinanceForOrder(Long orderId, String financeType);
    int addPayment(OmsPayment omsPayment);
    int updateInvoice(OmsFinance omsFinance);
}
