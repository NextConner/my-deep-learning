package com.jtcool.product.service;

import com.jtcool.product.domain.PrdSupplier;
import com.jtcool.product.mapper.PrdSupplierMapper;
import com.jtcool.product.service.impl.PrdSupplierServiceImpl;
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

class PrdSupplierServiceTest {

    @Mock
    private PrdSupplierMapper prdSupplierMapper;

    @InjectMocks
    private PrdSupplierServiceImpl prdSupplierService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSelectPrdSupplierList() {
        PrdSupplier supplier = new PrdSupplier();
        List<PrdSupplier> expected = Arrays.asList(supplier);
        when(prdSupplierMapper.selectPrdSupplierList(any())).thenReturn(expected);

        List<PrdSupplier> result = prdSupplierService.selectPrdSupplierList(supplier);

        assertEquals(expected, result);
        verify(prdSupplierMapper).selectPrdSupplierList(supplier);
    }

    @Test
    void testSelectPrdSupplierById() {
        PrdSupplier supplier = new PrdSupplier();
        supplier.setSupplierId(1L);
        when(prdSupplierMapper.selectPrdSupplierById(1L)).thenReturn(supplier);

        PrdSupplier result = prdSupplierService.selectPrdSupplierById(1L);

        assertEquals(supplier, result);
        verify(prdSupplierMapper).selectPrdSupplierById(1L);
    }

    @Test
    void testInsertPrdSupplier() {
        PrdSupplier supplier = new PrdSupplier();
        when(prdSupplierMapper.insertPrdSupplier(any())).thenReturn(1);

        int result = prdSupplierService.insertPrdSupplier(supplier);

        assertEquals(1, result);
        verify(prdSupplierMapper).insertPrdSupplier(supplier);
    }

    @Test
    void testUpdatePrdSupplier() {
        PrdSupplier supplier = new PrdSupplier();
        supplier.setSupplierId(1L);
        when(prdSupplierMapper.updatePrdSupplier(any())).thenReturn(1);

        int result = prdSupplierService.updatePrdSupplier(supplier);

        assertEquals(1, result);
        verify(prdSupplierMapper).updatePrdSupplier(supplier);
    }

    @Test
    void testDeletePrdSupplierByIds() {
        Long[] ids = {1L, 2L};
        when(prdSupplierMapper.deletePrdSupplierByIds(ids)).thenReturn(2);

        int result = prdSupplierService.deletePrdSupplierByIds(ids);

        assertEquals(2, result);
        verify(prdSupplierMapper).deletePrdSupplierByIds(ids);
    }
}
