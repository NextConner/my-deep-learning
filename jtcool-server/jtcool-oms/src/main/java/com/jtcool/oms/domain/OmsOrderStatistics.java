package com.jtcool.oms.domain;

import java.math.BigDecimal;
import java.util.Date;

public class OmsOrderStatistics {
    private Long statId;
    private Date statDate;
    private String statType;
    private Long customerId;
    private Long salesUserId;
    private Integer orderCount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal unpaidAmount;
    private Date createTime;

    public Long getStatId() { return statId; }
    public void setStatId(Long statId) { this.statId = statId; }
    public Date getStatDate() { return statDate; }
    public void setStatDate(Date statDate) { this.statDate = statDate; }
    public String getStatType() { return statType; }
    public void setStatType(String statType) { this.statType = statType; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getSalesUserId() { return salesUserId; }
    public void setSalesUserId(Long salesUserId) { this.salesUserId = salesUserId; }
    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public BigDecimal getUnpaidAmount() { return unpaidAmount; }
    public void setUnpaidAmount(BigDecimal unpaidAmount) { this.unpaidAmount = unpaidAmount; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
