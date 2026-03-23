package com.jtcool.wms.service;

import com.jtcool.wms.domain.WmsInventory;
import java.util.List;

public interface IWmsInventoryService {
    List<WmsInventory> selectWmsInventoryList(WmsInventory wmsInventory);
    WmsInventory selectWmsInventoryById(Long inventoryId);
    int insertWmsInventory(WmsInventory wmsInventory);
    int updateWmsInventory(WmsInventory wmsInventory);
    /**
     * 扣减库存（分布式锁 + 乐观锁）
     */
    void deductInventory(Long productId, Long warehouseId, Long areaId, Long locationId, Long shelfId, Integer quantity, Long billId, String billNo, String billType, Long operatorId);
    /**
     * 增加库存
     */
    void addInventory(Long productId, Long warehouseId, Long areaId, Long locationId, Long shelfId, Integer quantity, Long billId, String billNo, String billType, Long operatorId);
}
