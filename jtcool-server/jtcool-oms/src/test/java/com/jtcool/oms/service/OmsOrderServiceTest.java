package com.jtcool.oms.service;

import com.jtcool.oms.domain.OmsOrder;
import com.jtcool.oms.mapper.OmsOrderMapper;
import com.jtcool.oms.service.impl.OmsOrderServiceImpl;
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

class OmsOrderServiceTest {

    @Mock
    private OmsOrderMapper omsOrderMapper;

    @InjectMocks
    private OmsOrderServiceImpl omsOrderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSelectOmsOrderList() {
        OmsOrder order = new OmsOrder();
        List<OmsOrder> expected = Arrays.asList(order);
        when(omsOrderMapper.selectOmsOrderList(any())).thenReturn(expected);

        List<OmsOrder> result = omsOrderService.selectOmsOrderList(order);

        assertEquals(expected, result);
        verify(omsOrderMapper).selectOmsOrderList(order);
    }

    @Test
    void testSelectOmsOrderById() {
        OmsOrder order = new OmsOrder();
        order.setOrderId(1L);
        when(omsOrderMapper.selectOmsOrderById(1L)).thenReturn(order);

        OmsOrder result = omsOrderService.selectOmsOrderById(1L);

        assertEquals(order, result);
        verify(omsOrderMapper).selectOmsOrderById(1L);
    }

    @Test
    void testInsertOmsOrder() {
        OmsOrder order = new OmsOrder();
        when(omsOrderMapper.insertOmsOrder(any())).thenReturn(1);

        int result = omsOrderService.insertOmsOrder(order);

        assertEquals(1, result);
        verify(omsOrderMapper).insertOmsOrder(order);
    }

    @Test
    void testUpdateOmsOrder() {
        OmsOrder order = new OmsOrder();
        order.setOrderId(1L);
        when(omsOrderMapper.updateOmsOrder(any())).thenReturn(1);

        int result = omsOrderService.updateOmsOrder(order);

        assertEquals(1, result);
        verify(omsOrderMapper).updateOmsOrder(order);
    }

    @Test
    void testDeleteOmsOrderByIds() {
        Long[] ids = {1L, 2L};
        when(omsOrderMapper.deleteOmsOrderByIds(ids)).thenReturn(2);

        int result = omsOrderService.deleteOmsOrderByIds(ids);

        assertEquals(2, result);
        verify(omsOrderMapper).deleteOmsOrderByIds(ids);
    }

    @Test
    void testUpdateOrderStatus() {
        when(omsOrderMapper.updateOmsOrder(any())).thenReturn(1);

        int result = omsOrderService.updateOrderStatus(1L, "COMPLETED");

        assertEquals(1, result);
        verify(omsOrderMapper).updateOmsOrder(argThat(order ->
            order.getOrderId().equals(1L) && "COMPLETED".equals(order.getOrderStatus())
        ));
    }
}
