package com.jtcool.wms.domain;

import com.jtcool.common.core.domain.BaseEntity;
import java.util.Date;
import java.util.List;

public class WmsStockBill extends BaseEntity {
    private Long billId;
    private String billNo;
    private String billType;
    private Date billDate;
    private Long warehouseId;
    private Long relatedOrderId;
    private Long customerId;
    private Long supplierId;
    private String billStatus;
    private Long operatorId;
    private String delFlag;
    private List<WmsStockBillItem> items;

    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }
    public String getBillNo() { return billNo; }
    public void setBillNo(String billNo) { this.billNo = billNo; }
    public String getBillType() { return billType; }
    public void setBillType(String billType) { this.billType = billType; }
    public Date getBillDate() { return billDate; }
    public void setBillDate(Date billDate) { this.billDate = billDate; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getRelatedOrderId() { return relatedOrderId; }
    public void setRelatedOrderId(Long relatedOrderId) { this.relatedOrderId = relatedOrderId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getBillStatus() { return billStatus; }
    public void setBillStatus(String billStatus) { this.billStatus = billStatus; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<WmsStockBillItem> getItems() { return items; }
    public void setItems(List<WmsStockBillItem> items) { this.items = items; }
}
