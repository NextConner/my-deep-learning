package com.jtcool.wms.service;

import com.jtcool.wms.domain.WmsInventoryLog;
import java.util.List;
import java.util.Map;

/**
 * 库存流水分析服务接口
 */
public interface IWmsInventoryFlowService {

    /**
     * 获取瀑布图数据
     */
    Map<String, Object> getWaterfallData(Long productId, Long warehouseId, String beginTime, String endTime, String granularity);

    /**
     * 获取库位活动数据
     */
    Map<String, Object> getLocationActivity(Long warehouseId, String beginTime, String endTime, String billType);

    /**
     * 获取多产品趋势数据
     */
    Map<String, Object> getMultiProductTrends(List<Long> productIds, Long warehouseId, String beginTime, String endTime, String granularity);
}
