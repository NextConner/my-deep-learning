package com.jtcool.wms.service.impl;

import com.jtcool.common.utils.RedissonLockUtil;
import com.jtcool.wms.domain.WmsInventory;
import com.jtcool.wms.domain.WmsInventoryLog;
import com.jtcool.wms.mapper.WmsInventoryLogMapper;
import com.jtcool.wms.mapper.WmsInventoryMapper;
import com.jtcool.wms.service.IWmsInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class WmsInventoryServiceImpl implements IWmsInventoryService {

    @Autowired
    private WmsInventoryMapper wmsInventoryMapper;

    @Autowired
    private WmsInventoryLogMapper wmsInventoryLogMapper;

    @Autowired
    private RedissonLockUtil redissonLockUtil;

    @Override
    public List<WmsInventory> selectWmsInventoryList(WmsInventory wmsInventory) {
        return wmsInventoryMapper.selectWmsInventoryList(wmsInventory);
    }

    @Override
    public WmsInventory selectWmsInventoryById(Long inventoryId) {
        return wmsInventoryMapper.selectWmsInventoryById(inventoryId);
    }

    @Override
    public int insertWmsInventory(WmsInventory wmsInventory) {
        if (wmsInventory.getAvailableQuantity() == null) {
            wmsInventory.setAvailableQuantity(wmsInventory.getQuantity());
        }
        return wmsInventoryMapper.insertWmsInventory(wmsInventory);
    }

    @Override
    public int updateWmsInventory(WmsInventory wmsInventory) {
        return wmsInventoryMapper.updateWmsInventory(wmsInventory);
    }

    @Override
    @Transactional
    public void deductInventory(Long productId, Long warehouseId, Long areaId, Long locationId, Long shelfId,
                                Integer quantity, Long billId, String billNo, String billType, Long operatorId) {
        String lockKey = "inventory:" + productId + ":" + warehouseId + ":" + areaId + ":"
                + (locationId == null ? 0 : locationId) + ":" + (shelfId == null ? 0 : shelfId);
        redissonLockUtil.executeWithLock(lockKey, 5, 30, TimeUnit.SECONDS, () -> {
            doDeductInventory(productId, warehouseId, areaId, locationId, shelfId, quantity, billId, billNo, billType, operatorId);
            return null;
        });
    }

    @Override
    @Transactional
    public void addInventory(Long productId, Long warehouseId, Long areaId, Long locationId, Long shelfId,
                             Integer quantity, Long billId, String billNo, String billType, Long operatorId) {
        String lockKey = "inventory:" + productId + ":" + warehouseId + ":" + areaId + ":"
                + (locationId == null ? 0 : locationId) + ":" + (shelfId == null ? 0 : shelfId);
        redissonLockUtil.executeWithLock(lockKey, 5, 30, TimeUnit.SECONDS, () -> {
            doAddInventory(productId, warehouseId, areaId, locationId, shelfId, quantity, billId, billNo, billType, operatorId);
            return null;
        });
    }

    private void doDeductInventory(Long productId, Long warehouseId, Long areaId, Long locationId, Long shelfId,
                                   Integer quantity, Long billId, String billNo, String billType, Long operatorId) {
        WmsInventory inventory = wmsInventoryMapper.selectWmsInventoryByKey(productId, warehouseId, areaId, locationId, shelfId);
        if (inventory == null) {
            throw new RuntimeException("库存不存在");
        }
        if (inventory.getAvailableQuantity() < quantity) {
            throw new RuntimeException("库存不足");
        }
        Integer beforeQty = inventory.getQuantity();
        int rows = wmsInventoryMapper.deductInventory(inventory.getInventoryId(), quantity, inventory.getVersion());
        if (rows == 0) {
            throw new RuntimeException("库存更新失败，请重试");
        }
        insertLog(productId, warehouseId, areaId, locationId, shelfId, billId, billNo, billType,
                "OUT", quantity, beforeQty, beforeQty - quantity, operatorId);
    }

    private void doAddInventory(Long productId, Long warehouseId, Long areaId, Long locationId, Long shelfId,
                                Integer quantity, Long billId, String billNo, String billType, Long operatorId) {
        WmsInventory inventory = wmsInventoryMapper.selectWmsInventoryByKey(productId, warehouseId, areaId, locationId, shelfId);
        Integer beforeQty = 0;
        if (inventory == null) {
            inventory = new WmsInventory();
            inventory.setProductId(productId);
            inventory.setWarehouseId(warehouseId);
            inventory.setAreaId(areaId);
            inventory.setLocationId(locationId);
            inventory.setShelfId(shelfId);
            inventory.setQuantity(quantity);
            inventory.setLockedQuantity(0);
            inventory.setAvailableQuantity(quantity);
            inventory.setVersion(0);
            wmsInventoryMapper.insertWmsInventory(inventory);
        } else {
            beforeQty = inventory.getQuantity();
            inventory.setQuantity(inventory.getQuantity() + quantity);
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
            inventory.setVersion(inventory.getVersion() + 1);
            wmsInventoryMapper.updateWmsInventoryWithVersion(inventory);
        }
        insertLog(productId, warehouseId, areaId, locationId, shelfId, billId, billNo, billType,
                "IN", quantity, beforeQty, beforeQty + quantity, operatorId);
    }

    private void insertLog(Long productId, Long warehouseId, Long areaId, Long locationId, Long shelfId,
                           Long billId, String billNo, String billType, String changeType,
                           Integer changeQty, Integer beforeQty, Integer afterQty, Long operatorId) {
        WmsInventoryLog log = new WmsInventoryLog();
        log.setProductId(productId);
        log.setWarehouseId(warehouseId);
        log.setAreaId(areaId);
        log.setLocationId(locationId);
        log.setShelfId(shelfId);
        log.setBillId(billId);
        log.setBillNo(billNo);
        log.setBillType(billType);
        log.setChangeType(changeType);
        log.setChangeQuantity(changeQty);
        log.setBeforeQuantity(beforeQty);
        log.setAfterQuantity(afterQty);
        log.setOperatorId(operatorId);
        wmsInventoryLogMapper.insertWmsInventoryLog(log);
    }
}
