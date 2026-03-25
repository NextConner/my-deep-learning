package com.jtcool.wms.service;

import com.jtcool.wms.domain.WmsLocation;
import com.jtcool.wms.mapper.WmsLocationMapper;
import com.jtcool.wms.service.impl.WmsLocationServiceImpl;
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

class WmsLocationServiceTest {

    @Mock
    private WmsLocationMapper wmsLocationMapper;

    @InjectMocks
    private WmsLocationServiceImpl wmsLocationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSelectWmsLocationList() {
        WmsLocation location = new WmsLocation();
        List<WmsLocation> expected = Arrays.asList(location);
        when(wmsLocationMapper.selectWmsLocationList(any())).thenReturn(expected);

        List<WmsLocation> result = wmsLocationService.selectWmsLocationList(location);

        assertEquals(expected, result);
        verify(wmsLocationMapper).selectWmsLocationList(location);
    }

    @Test
    void testSelectWmsLocationById() {
        WmsLocation location = new WmsLocation();
        location.setLocationId(1L);
        when(wmsLocationMapper.selectWmsLocationById(1L)).thenReturn(location);

        WmsLocation result = wmsLocationService.selectWmsLocationById(1L);

        assertEquals(location, result);
        verify(wmsLocationMapper).selectWmsLocationById(1L);
    }

    @Test
    void testSelectWmsLocationByAreaId() {
        List<WmsLocation> expected = Arrays.asList(new WmsLocation());
        when(wmsLocationMapper.selectWmsLocationByAreaId(1L)).thenReturn(expected);

        List<WmsLocation> result = wmsLocationService.selectWmsLocationByAreaId(1L);

        assertEquals(expected, result);
        verify(wmsLocationMapper).selectWmsLocationByAreaId(1L);
    }

    @Test
    void testInsertWmsLocation() {
        WmsLocation location = new WmsLocation();
        when(wmsLocationMapper.insertWmsLocation(any())).thenReturn(1);

        int result = wmsLocationService.insertWmsLocation(location);

        assertEquals(1, result);
        verify(wmsLocationMapper).insertWmsLocation(location);
    }

    @Test
    void testUpdateWmsLocation() {
        WmsLocation location = new WmsLocation();
        location.setLocationId(1L);
        when(wmsLocationMapper.updateWmsLocation(any())).thenReturn(1);

        int result = wmsLocationService.updateWmsLocation(location);

        assertEquals(1, result);
        verify(wmsLocationMapper).updateWmsLocation(location);
    }

    @Test
    void testDeleteWmsLocationByIds() {
        Long[] ids = {1L, 2L};
        when(wmsLocationMapper.deleteWmsLocationByIds(ids)).thenReturn(2);

        int result = wmsLocationService.deleteWmsLocationByIds(ids);

        assertEquals(2, result);
        verify(wmsLocationMapper).deleteWmsLocationByIds(ids);
    }
}
