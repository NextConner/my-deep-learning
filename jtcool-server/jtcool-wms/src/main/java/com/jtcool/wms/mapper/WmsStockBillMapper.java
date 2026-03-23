package com.jtcool.wms.mapper;

import com.jtcool.wms.domain.WmsStockBill;
import com.jtcool.wms.domain.WmsStockBillItem;
import java.util.List;

public interface WmsStockBillMapper {
    List<WmsStockBill> selectWmsStockBillList(WmsStockBill wmsStockBill);
    WmsStockBill selectWmsStockBillById(Long billId);
    WmsStockBill selectWmsStockBillWithItems(Long billId);
    int insertWmsStockBill(WmsStockBill wmsStockBill);
    int insertWmsStockBillItem(WmsStockBillItem item);
    int updateWmsStockBill(WmsStockBill wmsStockBill);
    int deleteWmsStockBillByIds(Long[] billIds);
    int deleteWmsStockBillItemsByBillId(Long billId);
}
