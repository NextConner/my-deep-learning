package com.jtcool.oms.service;

import com.jtcool.oms.domain.OmsFinance;
import com.jtcool.oms.domain.OmsPayment;
import com.jtcool.oms.mapper.OmsFinanceMapper;
import com.jtcool.oms.mapper.OmsPaymentMapper;
import com.jtcool.oms.service.impl.OmsFinanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OmsFinanceServiceTest {

    @Mock
    private OmsFinanceMapper omsFinanceMapper;

    @Mock
    private OmsPaymentMapper omsPaymentMapper;

    @InjectMocks
    private OmsFinanceServiceImpl omsFinanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSelectOmsFinanceList() {
        OmsFinance finance = new OmsFinance();
        List<OmsFinance> expected = Arrays.asList(finance);
        when(omsFinanceMapper.selectOmsFinanceList(any())).thenReturn(expected);

        List<OmsFinance> result = omsFinanceService.selectOmsFinanceList(finance);

        assertEquals(expected, result);
        verify(omsFinanceMapper).selectOmsFinanceList(finance);
    }

    @Test
    void testSelectOmsFinanceById() {
        OmsFinance finance = new OmsFinance();
        finance.setFinanceId(1L);
        when(omsFinanceMapper.selectOmsFinanceById(1L)).thenReturn(finance);

        OmsFinance result = omsFinanceService.selectOmsFinanceById(1L);

        assertEquals(finance, result);
        verify(omsFinanceMapper).selectOmsFinanceById(1L);
    }

    @Test
    void testAddPayment() {
        OmsPayment payment = new OmsPayment();
        when(omsPaymentMapper.insertOmsPayment(any())).thenReturn(1);
        when(omsFinanceMapper.updateOmsFinance(any())).thenReturn(1);

        int result = omsFinanceService.addPayment(payment);

        assertEquals(1, result);
        verify(omsPaymentMapper).insertOmsPayment(payment);
    }

    @Test
    void testUpdateInvoice() {
        OmsFinance finance = new OmsFinance();
        finance.setFinanceId(1L);
        when(omsFinanceMapper.updateOmsFinance(any())).thenReturn(1);

        int result = omsFinanceService.updateInvoice(finance);

        assertEquals(1, result);
        verify(omsFinanceMapper).updateOmsFinance(finance);
    }
}
