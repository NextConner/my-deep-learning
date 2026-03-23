package com.jtcool.oms.service.impl;

import com.jtcool.oms.domain.OmsFinance;
import com.jtcool.oms.domain.OmsOrder;
import com.jtcool.oms.domain.OmsPayment;
import com.jtcool.oms.mapper.OmsFinanceMapper;
import com.jtcool.oms.mapper.OmsOrderMapper;
import com.jtcool.oms.mapper.OmsPaymentMapper;
import com.jtcool.oms.service.IOmsFinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class OmsFinanceServiceImpl implements IOmsFinanceService {
    @Autowired
    private OmsFinanceMapper omsFinanceMapper;
    @Autowired
    private OmsPaymentMapper omsPaymentMapper;
    @Autowired
    private OmsOrderMapper omsOrderMapper;

    @Override
    public List<OmsFinance> selectOmsFinanceList(OmsFinance omsFinance) {
        return omsFinanceMapper.selectOmsFinanceList(omsFinance);
    }

    @Override
    public OmsFinance selectOmsFinanceById(Long financeId) {
        return omsFinanceMapper.selectOmsFinanceById(financeId);
    }

    @Override
    @Transactional
    public int createFinanceForOrder(Long orderId, String financeType) {
        OmsOrder order = omsOrderMapper.selectOmsOrderById(orderId);
        OmsFinance finance = new OmsFinance();
        finance.setOrderId(orderId);
        finance.setFinanceType(financeType);
        finance.setTotalAmount(order.getFinalAmount());
        return omsFinanceMapper.insertOmsFinance(finance);
    }

    @Override
    @Transactional
    public int addPayment(OmsPayment omsPayment) {
        omsPaymentMapper.insertOmsPayment(omsPayment);
        OmsFinance finance = omsFinanceMapper.selectOmsFinanceById(omsPayment.getFinanceId());
        BigDecimal newPaid = finance.getPaidAmount().add(omsPayment.getPaymentAmount());
        BigDecimal newUnpaid = finance.getTotalAmount().subtract(newPaid);
        finance.setPaidAmount(newPaid);
        finance.setUnpaidAmount(newUnpaid);
        finance.setPaymentStatus(newUnpaid.compareTo(BigDecimal.ZERO) <= 0 ? "PAID" : "PARTIAL");
        return omsFinanceMapper.updateOmsFinance(finance);
    }

    @Override
    @Transactional
    public int updateInvoice(OmsFinance omsFinance) {
        omsFinance.setInvoiceStatus("ISSUED");
        return omsFinanceMapper.updateOmsFinance(omsFinance);
    }
}
