package com.jtcool.wms.service.impl;

import com.jtcool.wms.domain.WmsStockBill;
import com.jtcool.wms.domain.WmsStockBillItem;
import com.jtcool.wms.mapper.WmsStockBillMapper;
import com.jtcool.wms.service.IWmsInventoryService;
import com.jtcool.wms.service.IWmsStockBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WmsStockBillServiceImpl implements IWmsStockBillService {

    @Autowired
    private WmsStockBillMapper wmsStockBillMapper;

    @Autowired
    private IWmsInventoryService wmsInventoryService;

    @Override
    public List<WmsStockBill> selectWmsStockBillList(WmsStockBill wmsStockBill) {
        return wmsStockBillMapper.selectWmsStockBillList(wmsStockBill);
    }

    @Override
    public WmsStockBill selectWmsStockBillById(Long billId) {
        return wmsStockBillMapper.selectWmsStockBillWithItems(billId);
    }

    @Override
    @Transactional
    public int insertWmsStockBill(WmsStockBill wmsStockBill) {
        int rows = wmsStockBillMapper.insertWmsStockBill(wmsStockBill);
        if (wmsStockBill.getItems() != null) {
            for (WmsStockBillItem item : wmsStockBill.getItems()) {
                item.setBillId(wmsStockBill.getBillId());
                wmsStockBillMapper.insertWmsStockBillItem(item);
            }
        }
        return rows;
    }

    @Override
    @Transactional
    public int updateWmsStockBill(WmsStockBill wmsStockBill) {
        int rows = wmsStockBillMapper.updateWmsStockBill(wmsStockBill);
        wmsStockBillMapper.deleteWmsStockBillItemsByBillId(wmsStockBill.getBillId());
        if (wmsStockBill.getItems() != null) {
            for (WmsStockBillItem item : wmsStockBill.getItems()) {
                item.setBillId(wmsStockBill.getBillId());
                wmsStockBillMapper.insertWmsStockBillItem(item);
            }
        }
        return rows;
    }

    @Override
    public int deleteWmsStockBillByIds(Long[] billIds) {
        return wmsStockBillMapper.deleteWmsStockBillByIds(billIds);
    }

    @Override
    @Transactional
    public void confirmStockBill(Long billId, Long operatorId) {
        WmsStockBill bill = wmsStockBillMapper.selectWmsStockBillWithItems(billId);
        if (bill == null) {
            throw new RuntimeException("出入库单不存在");
        }
        if ("COMPLETED".equals(bill.getBillStatus())) {
            throw new RuntimeException("出入库单已完成");
        }
        if (bill.getItems() == null || bill.getItems().isEmpty()) {
            throw new RuntimeException("出入库单明细不能为空");
        }

        for (WmsStockBillItem item : bill.getItems()) {
            if (bill.getBillType() != null && bill.getBillType().startsWith("IN_")) {
                wmsInventoryService.addInventory(
                        item.getProductId(), bill.getWarehouseId(), item.getAreaId(), item.getLocationId(), item.getShelfId(),
                        item.getQuantity(), bill.getBillId(), bill.getBillNo(), bill.getBillType(), operatorId
                );
            } else {
                wmsInventoryService.deductInventory(
                        item.getProductId(), bill.getWarehouseId(), item.getAreaId(), item.getLocationId(), item.getShelfId(),
                        item.getQuantity(), bill.getBillId(), bill.getBillNo(), bill.getBillType(), operatorId
                );
            }
        }

        bill.setBillStatus("COMPLETED");
        wmsStockBillMapper.updateWmsStockBill(bill);
    }
}
