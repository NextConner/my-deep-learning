package com.jtcool.product.service;

import com.jtcool.product.domain.PrdBrand;
import com.jtcool.product.mapper.PrdBrandMapper;
import com.jtcool.product.service.impl.PrdBrandServiceImpl;
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

class PrdBrandServiceTest {

    @Mock
    private PrdBrandMapper prdBrandMapper;

    @InjectMocks
    private PrdBrandServiceImpl prdBrandService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSelectPrdBrandList() {
        PrdBrand brand = new PrdBrand();
        List<PrdBrand> expected = Arrays.asList(brand);
        when(prdBrandMapper.selectPrdBrandList(any())).thenReturn(expected);

        List<PrdBrand> result = prdBrandService.selectPrdBrandList(brand);

        assertEquals(expected, result);
        verify(prdBrandMapper).selectPrdBrandList(brand);
    }

    @Test
    void testSelectPrdBrandById() {
        PrdBrand brand = new PrdBrand();
        brand.setBrandId(1L);
        when(prdBrandMapper.selectPrdBrandById(1L)).thenReturn(brand);

        PrdBrand result = prdBrandService.selectPrdBrandById(1L);

        assertEquals(brand, result);
        verify(prdBrandMapper).selectPrdBrandById(1L);
    }

    @Test
    void testInsertPrdBrand() {
        PrdBrand brand = new PrdBrand();
        when(prdBrandMapper.insertPrdBrand(any())).thenReturn(1);

        int result = prdBrandService.insertPrdBrand(brand);

        assertEquals(1, result);
        verify(prdBrandMapper).insertPrdBrand(brand);
    }

    @Test
    void testUpdatePrdBrand() {
        PrdBrand brand = new PrdBrand();
        brand.setBrandId(1L);
        when(prdBrandMapper.updatePrdBrand(any())).thenReturn(1);

        int result = prdBrandService.updatePrdBrand(brand);

        assertEquals(1, result);
        verify(prdBrandMapper).updatePrdBrand(brand);
    }

    @Test
    void testDeletePrdBrandByIds() {
        Long[] ids = {1L, 2L};
        when(prdBrandMapper.deletePrdBrandByIds(ids)).thenReturn(2);

        int result = prdBrandService.deletePrdBrandByIds(ids);

        assertEquals(2, result);
        verify(prdBrandMapper).deletePrdBrandByIds(ids);
    }
}
