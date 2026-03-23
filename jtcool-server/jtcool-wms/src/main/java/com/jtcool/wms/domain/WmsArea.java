package com.jtcool.wms.domain;

import com.jtcool.common.core.domain.BaseEntity;

public class WmsArea extends BaseEntity {
    private Long areaId;
    private Long warehouseId;
    private String areaCode;
    private String areaName;
    private String status;
    private String delFlag;

    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getAreaCode() { return areaCode; }
    public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
