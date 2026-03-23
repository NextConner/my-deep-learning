package com.jtcool.product.domain;

import com.jtcool.common.core.domain.BaseEntity;

public class PrdCategory extends BaseEntity {
    private Long categoryId;
    private Long parentId;
    private String ancestors;
    private String categoryName;
    private String categoryCode;
    private Integer orderNum;
    private String status;
    private String delFlag;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getAncestors() { return ancestors; }
    public void setAncestors(String ancestors) { this.ancestors = ancestors; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
