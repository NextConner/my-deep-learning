package com.jtcool.oms.service;

import com.jtcool.oms.domain.OmsOrderFlow;
import com.jtcool.oms.mapper.OmsOrderFlowMapper;
import com.jtcool.oms.service.impl.OmsOrderFlowServiceImpl;
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

class OmsOrderFlowServiceTest {

    @Mock
    private OmsOrderFlowMapper omsOrderFlowMapper;

    @InjectMocks
    private OmsOrderFlowServiceImpl omsOrderFlowService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInsertOmsOrderFlow() {
        OmsOrderFlow orderFlow = new OmsOrderFlow();
        when(omsOrderFlowMapper.insertOmsOrderFlow(any())).thenReturn(1);

        int result = omsOrderFlowService.insertOmsOrderFlow(orderFlow);

        assertEquals(1, result);
        verify(omsOrderFlowMapper).insertOmsOrderFlow(orderFlow);
    }

    @Test
    void testSelectOmsOrderFlowByOrderId() {
        List<OmsOrderFlow> expected = Arrays.asList(new OmsOrderFlow());
        when(omsOrderFlowMapper.selectOmsOrderFlowByOrderId(1L)).thenReturn(expected);

        List<OmsOrderFlow> result = omsOrderFlowService.selectOmsOrderFlowByOrderId(1L);

        assertEquals(expected, result);
        verify(omsOrderFlowMapper).selectOmsOrderFlowByOrderId(1L);
    }
}
