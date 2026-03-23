package com.jtcool.wms.mapper;

import com.jtcool.wms.domain.WmsInventory;
import java.util.List;

public interface WmsInventoryMapper {
    List<WmsInventory> selectWmsInventoryList(WmsInventory wmsInventory);
    WmsInventory selectWmsInventoryById(Long inventoryId);
    WmsInventory selectWmsInventoryByKey(Long productId, Long warehouseId, Long areaId, Long locationId, Long shelfId);
    int insertWmsInventory(WmsInventory wmsInventory);
    int updateWmsInventory(WmsInventory wmsInventory);
    int updateWmsInventoryWithVersion(WmsInventory wmsInventory);
    int lockInventory(Long inventoryId, Integer quantity);
    int unlockInventory(Long inventoryId, Integer quantity);
    int deductInventory(Long inventoryId, Integer quantity, Integer version);
}
