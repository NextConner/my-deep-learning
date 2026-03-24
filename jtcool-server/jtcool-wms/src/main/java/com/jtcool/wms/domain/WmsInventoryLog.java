package com.jtcool.wms.domain;

import java.util.Date;

public class WmsInventoryLog {
    private Long logId;
    private Long productId;
    private String productName;
    private Long warehouseId;
    private String warehouseName;
    private Long areaId;
    private Long locationId;
    private Long shelfId;
    private Long billId;
    private String billNo;
    private String billType;
    private String changeType;
    private Integer changeQuantity;
    private Integer beforeQuantity;
    private Integer afterQuantity;
    private Long operatorId;
    private String operatorName;
    private Date beginTime;
    private Date endTime;
    private Date createTime;
    private String remark;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public Long getShelfId() { return shelfId; }
    public void setShelfId(Long shelfId) { this.shelfId = shelfId; }
    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }
    public String getBillNo() { return billNo; }
    public void setBillNo(String billNo) { this.billNo = billNo; }
    public String getBillType() { return billType; }
    public void setBillType(String billType) { this.billType = billType; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public Integer getChangeQuantity() { return changeQuantity; }
    public void setChangeQuantity(Integer changeQuantity) { this.changeQuantity = changeQuantity; }
    public Integer getBeforeQuantity() { return beforeQuantity; }
    public void setBeforeQuantity(Integer beforeQuantity) { this.beforeQuantity = beforeQuantity; }
    public Integer getAfterQuantity() { return afterQuantity; }
    public void setAfterQuantity(Integer afterQuantity) { this.afterQuantity = afterQuantity; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public Date getBeginTime() { return beginTime; }
    public void setBeginTime(Date beginTime) { this.beginTime = beginTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
