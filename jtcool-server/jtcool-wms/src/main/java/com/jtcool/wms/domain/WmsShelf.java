package com.jtcool.wms.domain;

import com.jtcool.common.core.domain.BaseEntity;

public class WmsShelf extends BaseEntity {
    private Long shelfId;
    private Long locationId;
    private String shelfCode;
    private String shelfType;
    private String status;
    private String delFlag;

    public Long getShelfId() { return shelfId; }
    public void setShelfId(Long shelfId) { this.shelfId = shelfId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public String getShelfCode() { return shelfCode; }
    public void setShelfCode(String shelfCode) { this.shelfCode = shelfCode; }
    public String getShelfType() { return shelfType; }
    public void setShelfType(String shelfType) { this.shelfType = shelfType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
