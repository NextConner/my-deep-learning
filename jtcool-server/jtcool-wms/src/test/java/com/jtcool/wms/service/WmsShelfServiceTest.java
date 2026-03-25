package com.jtcool.wms.service;

import com.jtcool.wms.domain.WmsShelf;
import com.jtcool.wms.mapper.WmsShelfMapper;
import com.jtcool.wms.service.impl.WmsShelfServiceImpl;
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

class WmsShelfServiceTest {

    @Mock
    private WmsShelfMapper wmsShelfMapper;

    @InjectMocks
    private WmsShelfServiceImpl wmsShelfService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSelectWmsShelfList() {
        WmsShelf shelf = new WmsShelf();
        List<WmsShelf> expected = Arrays.asList(shelf);
        when(wmsShelfMapper.selectWmsShelfList(any())).thenReturn(expected);

        List<WmsShelf> result = wmsShelfService.selectWmsShelfList(shelf);

        assertEquals(expected, result);
        verify(wmsShelfMapper).selectWmsShelfList(shelf);
    }

    @Test
    void testSelectWmsShelfById() {
        WmsShelf shelf = new WmsShelf();
        shelf.setShelfId(1L);
        when(wmsShelfMapper.selectWmsShelfById(1L)).thenReturn(shelf);

        WmsShelf result = wmsShelfService.selectWmsShelfById(1L);

        assertEquals(shelf, result);
        verify(wmsShelfMapper).selectWmsShelfById(1L);
    }

    @Test
    void testSelectWmsShelfByLocationId() {
        List<WmsShelf> expected = Arrays.asList(new WmsShelf());
        when(wmsShelfMapper.selectWmsShelfByLocationId(1L)).thenReturn(expected);

        List<WmsShelf> result = wmsShelfService.selectWmsShelfByLocationId(1L);

        assertEquals(expected, result);
        verify(wmsShelfMapper).selectWmsShelfByLocationId(1L);
    }

    @Test
    void testInsertWmsShelf() {
        WmsShelf shelf = new WmsShelf();
        when(wmsShelfMapper.insertWmsShelf(any())).thenReturn(1);

        int result = wmsShelfService.insertWmsShelf(shelf);

        assertEquals(1, result);
        verify(wmsShelfMapper).insertWmsShelf(shelf);
    }

    @Test
    void testUpdateWmsShelf() {
        WmsShelf shelf = new WmsShelf();
        shelf.setShelfId(1L);
        when(wmsShelfMapper.updateWmsShelf(any())).thenReturn(1);

        int result = wmsShelfService.updateWmsShelf(shelf);

        assertEquals(1, result);
        verify(wmsShelfMapper).updateWmsShelf(shelf);
    }

    @Test
    void testDeleteWmsShelfByIds() {
        Long[] ids = {1L, 2L};
        when(wmsShelfMapper.deleteWmsShelfByIds(ids)).thenReturn(2);

        int result = wmsShelfService.deleteWmsShelfByIds(ids);

        assertEquals(2, result);
        verify(wmsShelfMapper).deleteWmsShelfByIds(ids);
    }
}
