package com.jtcool.product.service;

import com.jtcool.product.domain.PrdProduct;
import com.jtcool.product.mapper.PrdProductMapper;
import com.jtcool.product.service.impl.PrdProductServiceImpl;
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

class PrdProductServiceTest {

    @Mock
    private PrdProductMapper prdProductMapper;

    @InjectMocks
    private PrdProductServiceImpl prdProductService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSelectPrdProductList() {
        PrdProduct product = new PrdProduct();
        List<PrdProduct> expected = Arrays.asList(product);
        when(prdProductMapper.selectPrdProductList(any())).thenReturn(expected);

        List<PrdProduct> result = prdProductService.selectPrdProductList(product);

        assertEquals(expected, result);
        verify(prdProductMapper).selectPrdProductList(product);
    }

    @Test
    void testSelectPrdProductById() {
        PrdProduct product = new PrdProduct();
        product.setProductId(1L);
        when(prdProductMapper.selectPrdProductById(1L)).thenReturn(product);

        PrdProduct result = prdProductService.selectPrdProductById(1L);

        assertEquals(product, result);
        verify(prdProductMapper).selectPrdProductById(1L);
    }

    @Test
    void testInsertPrdProduct() {
        PrdProduct product = new PrdProduct();
        when(prdProductMapper.insertPrdProduct(any())).thenReturn(1);

        int result = prdProductService.insertPrdProduct(product);

        assertEquals(1, result);
        verify(prdProductMapper).insertPrdProduct(product);
    }

    @Test
    void testUpdatePrdProduct() {
        PrdProduct product = new PrdProduct();
        product.setProductId(1L);
        when(prdProductMapper.updatePrdProduct(any())).thenReturn(1);

        int result = prdProductService.updatePrdProduct(product);

        assertEquals(1, result);
        verify(prdProductMapper).updatePrdProduct(product);
    }

    @Test
    void testDeletePrdProductByIds() {
        Long[] ids = {1L, 2L};
        when(prdProductMapper.deletePrdProductByIds(ids)).thenReturn(2);

        int result = prdProductService.deletePrdProductByIds(ids);

        assertEquals(2, result);
        verify(prdProductMapper).deletePrdProductByIds(ids);
    }
}
