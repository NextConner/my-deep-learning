package com.jtcool.wms.service;

import com.jtcool.wms.domain.WmsInventoryLog;
import com.jtcool.wms.mapper.WmsInventoryLogMapper;
import com.jtcool.wms.service.impl.WmsInventoryFlowServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WmsInventoryFlowServiceTest {

    @Mock
    private WmsInventoryLogMapper wmsInventoryLogMapper;

    @InjectMocks
    private WmsInventoryFlowServiceImpl wmsInventoryFlowService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetWaterfallData() {
        List<WmsInventoryLog> logs = Arrays.asList(new WmsInventoryLog());
        when(wmsInventoryLogMapper.selectWmsInventoryLogList(any())).thenReturn(logs);

        Map<String, Object> result = wmsInventoryFlowService.getWaterfallData(1L, 1L, "2024-01-01", "2024-12-31", "day");

        assertNotNull(result);
        assertEquals(logs, result.get("logs"));
        verify(wmsInventoryLogMapper).selectWmsInventoryLogList(any());
    }

    @Test
    void testGetLocationActivity() {
        List<WmsInventoryLog> logs = Arrays.asList(new WmsInventoryLog());
        when(wmsInventoryLogMapper.selectWmsInventoryLogList(any())).thenReturn(logs);

        Map<String, Object> result = wmsInventoryFlowService.getLocationActivity(1L, "2024-01-01", "2024-12-31", "IN");

        assertNotNull(result);
        assertEquals(logs, result.get("logs"));
        verify(wmsInventoryLogMapper).selectWmsInventoryLogList(any());
    }

    @Test
    void testGetMultiProductTrends() {
        List<WmsInventoryLog> logs = Arrays.asList(new WmsInventoryLog());
        when(wmsInventoryLogMapper.selectWmsInventoryLogList(any())).thenReturn(logs);

        Map<String, Object> result = wmsInventoryFlowService.getMultiProductTrends(Arrays.asList(1L, 2L), 1L, "2024-01-01", "2024-12-31", "day");

        assertNotNull(result);
        assertNotNull(result.get("logs"));
        verify(wmsInventoryLogMapper, times(2)).selectWmsInventoryLogList(any());
    }
}
