package com.jtcool.wms.service;

import com.jtcool.wms.domain.WmsArea;
import com.jtcool.wms.mapper.WmsAreaMapper;
import com.jtcool.wms.service.impl.WmsAreaServiceImpl;
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

class WmsAreaServiceTest {

    @Mock
    private WmsAreaMapper wmsAreaMapper;

    @InjectMocks
    private WmsAreaServiceImpl wmsAreaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSelectWmsAreaList() {
        WmsArea area = new WmsArea();
        List<WmsArea> expected = Arrays.asList(area);
        when(wmsAreaMapper.selectWmsAreaList(any())).thenReturn(expected);

        List<WmsArea> result = wmsAreaService.selectWmsAreaList(area);

        assertEquals(expected, result);
        verify(wmsAreaMapper).selectWmsAreaList(area);
    }

    @Test
    void testSelectWmsAreaById() {
        WmsArea area = new WmsArea();
        area.setAreaId(1L);
        when(wmsAreaMapper.selectWmsAreaById(1L)).thenReturn(area);

        WmsArea result = wmsAreaService.selectWmsAreaById(1L);

        assertEquals(area, result);
        verify(wmsAreaMapper).selectWmsAreaById(1L);
    }

    @Test
    void testSelectWmsAreaByWarehouseId() {
        List<WmsArea> expected = Arrays.asList(new WmsArea());
        when(wmsAreaMapper.selectWmsAreaByWarehouseId(1L)).thenReturn(expected);

        List<WmsArea> result = wmsAreaService.selectWmsAreaByWarehouseId(1L);

        assertEquals(expected, result);
        verify(wmsAreaMapper).selectWmsAreaByWarehouseId(1L);
    }

    @Test
    void testInsertWmsArea() {
        WmsArea area = new WmsArea();
        when(wmsAreaMapper.insertWmsArea(any())).thenReturn(1);

        int result = wmsAreaService.insertWmsArea(area);

        assertEquals(1, result);
        verify(wmsAreaMapper).insertWmsArea(area);
    }

    @Test
    void testUpdateWmsArea() {
        WmsArea area = new WmsArea();
        area.setAreaId(1L);
        when(wmsAreaMapper.updateWmsArea(any())).thenReturn(1);

        int result = wmsAreaService.updateWmsArea(area);

        assertEquals(1, result);
        verify(wmsAreaMapper).updateWmsArea(area);
    }

    @Test
    void testDeleteWmsAreaByIds() {
        Long[] ids = {1L, 2L};
        when(wmsAreaMapper.deleteWmsAreaByIds(ids)).thenReturn(2);

        int result = wmsAreaService.deleteWmsAreaByIds(ids);

        assertEquals(2, result);
        verify(wmsAreaMapper).deleteWmsAreaByIds(ids);
    }
}
