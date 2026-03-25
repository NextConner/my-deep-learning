package com.jtcool.wms.domain;

import java.util.Date;

public class WmsStockStatistics {
    private Long statId;
    private Date statDate;
    private Date beginTime;
    private Date endTime;
    private String statType;
    private Long warehouseId;
    private Long areaId;
    private Long locationId;
    private Long productId;
    private Long customerId;
    private Long salesUserId;
    private Integer inQuantity;
    private Integer outQuantity;
    private Date createTime;

    public Long getStatId() { return statId; }
    public void setStatId(Long statId) { this.statId = statId; }
    public Date getStatDate() { return statDate; }
    public void setStatDate(Date statDate) { this.statDate = statDate; }
    public Date getBeginTime() { return beginTime; }
    public void setBeginTime(Date beginTime) { this.beginTime = beginTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public String getStatType() { return statType; }
    public void setStatType(String statType) { this.statType = statType; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getSalesUserId() { return salesUserId; }
    public void setSalesUserId(Long salesUserId) { this.salesUserId = salesUserId; }
    public Integer getInQuantity() { return inQuantity; }
    public void setInQuantity(Integer inQuantity) { this.inQuantity = inQuantity; }
    public Integer getOutQuantity() { return outQuantity; }
    public void setOutQuantity(Integer outQuantity) { this.outQuantity = outQuantity; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
