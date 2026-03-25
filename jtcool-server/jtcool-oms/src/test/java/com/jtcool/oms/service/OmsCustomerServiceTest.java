package com.jtcool.oms.service;

import com.jtcool.oms.domain.OmsCustomer;
import com.jtcool.oms.mapper.OmsCustomerMapper;
import com.jtcool.oms.service.impl.OmsCustomerServiceImpl;
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

class OmsCustomerServiceTest {

    @Mock
    private OmsCustomerMapper omsCustomerMapper;

    @InjectMocks
    private OmsCustomerServiceImpl omsCustomerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSelectOmsCustomerList() {
        OmsCustomer customer = new OmsCustomer();
        List<OmsCustomer> expected = Arrays.asList(customer);
        when(omsCustomerMapper.selectOmsCustomerList(any())).thenReturn(expected);

        List<OmsCustomer> result = omsCustomerService.selectOmsCustomerList(customer);

        assertEquals(expected, result);
        verify(omsCustomerMapper).selectOmsCustomerList(customer);
    }

    @Test
    void testSelectOmsCustomerById() {
        OmsCustomer customer = new OmsCustomer();
        customer.setCustomerId(1L);
        when(omsCustomerMapper.selectOmsCustomerById(1L)).thenReturn(customer);

        OmsCustomer result = omsCustomerService.selectOmsCustomerById(1L);

        assertEquals(customer, result);
        verify(omsCustomerMapper).selectOmsCustomerById(1L);
    }

    @Test
    void testInsertOmsCustomer() {
        OmsCustomer customer = new OmsCustomer();
        when(omsCustomerMapper.insertOmsCustomer(any())).thenReturn(1);

        int result = omsCustomerService.insertOmsCustomer(customer);

        assertEquals(1, result);
        verify(omsCustomerMapper).insertOmsCustomer(customer);
    }

    @Test
    void testUpdateOmsCustomer() {
        OmsCustomer customer = new OmsCustomer();
        customer.setCustomerId(1L);
        when(omsCustomerMapper.updateOmsCustomer(any())).thenReturn(1);

        int result = omsCustomerService.updateOmsCustomer(customer);

        assertEquals(1, result);
        verify(omsCustomerMapper).updateOmsCustomer(customer);
    }

    @Test
    void testDeleteOmsCustomerByIds() {
        Long[] ids = {1L, 2L};
        when(omsCustomerMapper.deleteOmsCustomerByIds(ids)).thenReturn(2);

        int result = omsCustomerService.deleteOmsCustomerByIds(ids);

        assertEquals(2, result);
        verify(omsCustomerMapper).deleteOmsCustomerByIds(ids);
    }
}
