package com.jtcool.product.domain;

import com.jtcool.common.core.domain.BaseEntity;

public class PrdBrand extends BaseEntity {
    private Long brandId;
    private String brandName;
    private String brandCode;
    private String logoUrl;
    private String status;
    private String delFlag;

    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public String getBrandCode() { return brandCode; }
    public void setBrandCode(String brandCode) { this.brandCode = brandCode; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
