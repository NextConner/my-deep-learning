package com.jtcool.product.domain;

import com.jtcool.common.core.domain.BaseEntity;
import java.math.BigDecimal;

public class PrdProduct extends BaseEntity {
    private Long productId;
    private String productCode;
    private String productName;
    private Long categoryId;
    private Long brandId;
    private Long supplierId;
    private String specification;
    private String unit;
    private BigDecimal standardPrice;
    private BigDecimal costPrice;
    private Integer minStock;
    private Integer maxStock;
    private Integer warningStock;
    private String productImage;
    private String status;
    private String delFlag;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSpecification() { return specification; }
    public void setSpecification(String specification) { this.specification = specification; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getStandardPrice() { return standardPrice; }
    public void setStandardPrice(BigDecimal standardPrice) { this.standardPrice = standardPrice; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public Integer getMinStock() { return minStock; }
    public void setMinStock(Integer minStock) { this.minStock = minStock; }
    public Integer getMaxStock() { return maxStock; }
    public void setMaxStock(Integer maxStock) { this.maxStock = maxStock; }
    public Integer getWarningStock() { return warningStock; }
    public void setWarningStock(Integer warningStock) { this.warningStock = warningStock; }
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
