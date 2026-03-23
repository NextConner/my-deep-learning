package com.jtcool.wms.domain;

import com.jtcool.common.core.domain.BaseEntity;

public class WmsWarehouse extends BaseEntity {
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private String warehouseType;
    private String address;
    private Long managerId;
    private String status;
    private String delFlag;

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getWarehouseType() { return warehouseType; }
    public void setWarehouseType(String warehouseType) { this.warehouseType = warehouseType; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
