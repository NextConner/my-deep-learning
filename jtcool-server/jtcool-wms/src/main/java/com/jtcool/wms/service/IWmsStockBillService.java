package com.jtcool.wms.service;

import com.jtcool.wms.domain.WmsStockBill;
import java.util.List;

public interface IWmsStockBillService {
    List<WmsStockBill> selectWmsStockBillList(WmsStockBill wmsStockBill);
    WmsStockBill selectWmsStockBillById(Long billId);
    int insertWmsStockBill(WmsStockBill wmsStockBill);
    int updateWmsStockBill(WmsStockBill wmsStockBill);
    int deleteWmsStockBillByIds(Long[] billIds);
    /**
     * 确认出入库单，执行库存变更
     */
    void confirmStockBill(Long billId, Long operatorId);
}
