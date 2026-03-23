package com.jtcool.wms.domain;

import java.util.Date;

public class WmsInventory {
    private Long inventoryId;
    private Long productId;
    private Long warehouseId;
    private Long areaId;
    private Long locationId;
    private Long shelfId;
    private Integer quantity;
    private Integer lockedQuantity;
    private Integer availableQuantity;
    private Integer version;
    private String delFlag;
    private Date createTime;
    private Date updateTime;

    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public Long getShelfId() { return shelfId; }
    public void setShelfId(Long shelfId) { this.shelfId = shelfId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getLockedQuantity() { return lockedQuantity; }
    public void setLockedQuantity(Integer lockedQuantity) { this.lockedQuantity = lockedQuantity; }
    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
