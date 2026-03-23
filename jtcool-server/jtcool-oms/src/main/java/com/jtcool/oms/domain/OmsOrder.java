package com.jtcool.oms.domain;

import com.jtcool.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class OmsOrder extends BaseEntity {
    private Long orderId;
    private String orderNo;
    private Long customerId;
    private Date orderDate;
    private Date deliveryDate;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String orderStatus;
    private Long salesUserId;
    private String requireShippingPhoto;
    private String delFlag;
    private List<OmsOrderItem> orderItems;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public Date getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(Date deliveryDate) { this.deliveryDate = deliveryDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public Long getSalesUserId() { return salesUserId; }
    public void setSalesUserId(Long salesUserId) { this.salesUserId = salesUserId; }
    public String getRequireShippingPhoto() { return requireShippingPhoto; }
    public void setRequireShippingPhoto(String requireShippingPhoto) { this.requireShippingPhoto = requireShippingPhoto; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<OmsOrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OmsOrderItem> orderItems) { this.orderItems = orderItems; }
}
