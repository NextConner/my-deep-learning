package com.jtcool.oms.mapper;

import com.jtcool.oms.domain.OmsPayment;
import java.util.List;

public interface OmsPaymentMapper {
    List<OmsPayment> selectOmsPaymentByFinanceId(Long financeId);
    int insertOmsPayment(OmsPayment omsPayment);
}
