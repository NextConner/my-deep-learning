package com.jtcool.oms.mapper;

import com.jtcool.oms.domain.OmsFinance;
import java.util.List;

public interface OmsFinanceMapper {
    List<OmsFinance> selectOmsFinanceList(OmsFinance omsFinance);
    OmsFinance selectOmsFinanceById(Long financeId);
    OmsFinance selectOmsFinanceByOrderId(Long orderId);
    int insertOmsFinance(OmsFinance omsFinance);
    int updateOmsFinance(OmsFinance omsFinance);
}
