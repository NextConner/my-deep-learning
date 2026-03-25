package com.jtcool.wms.service.impl;

import com.jtcool.wms.domain.WmsInventoryLog;
import com.jtcool.wms.mapper.WmsInventoryLogMapper;
import com.jtcool.wms.service.IWmsInventoryFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WmsInventoryFlowServiceImpl implements IWmsInventoryFlowService {

    @Autowired
    private WmsInventoryLogMapper wmsInventoryLogMapper;

    @Override
    public Map<String, Object> getWaterfallData(Long productId, Long warehouseId, String beginTime, String endTime, String granularity) {
        WmsInventoryLog query = new WmsInventoryLog();
        query.setProductId(productId);
        query.setWarehouseId(warehouseId);
        List<WmsInventoryLog> logs = wmsInventoryLogMapper.selectWmsInventoryLogList(query);

        Map<String, Object> result = new HashMap<>();
        result.put("logs", logs);
        return result;
    }

    @Override
    public Map<String, Object> getLocationActivity(Long warehouseId, String beginTime, String endTime, String billType) {
        WmsInventoryLog query = new WmsInventoryLog();
        query.setWarehouseId(warehouseId);
        query.setBillType(billType);
        List<WmsInventoryLog> logs = wmsInventoryLogMapper.selectWmsInventoryLogList(query);

        Map<String, Object> result = new HashMap<>();
        result.put("logs", logs);
        return result;
    }

    @Override
    public Map<String, Object> getMultiProductTrends(List<Long> productIds, Long warehouseId, String beginTime, String endTime, String granularity) {
        List<WmsInventoryLog> allLogs = new ArrayList<>();
        for (Long productId : productIds) {
            WmsInventoryLog query = new WmsInventoryLog();
            query.setProductId(productId);
            query.setWarehouseId(warehouseId);
            List<WmsInventoryLog> logs = wmsInventoryLogMapper.selectWmsInventoryLogList(query);
            allLogs.addAll(logs);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("logs", allLogs);
        return result;
    }
}