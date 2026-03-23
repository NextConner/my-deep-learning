package com.jtcool.oms.domain;

import java.util.Date;

public class OmsOrderFlow {
    private Long flowId;
    private Long orderId;
    private String flowStatus;
    private String actionType;
    private Long operatorId;
    private String operatorName;
    private Date actionTime;
    private String rejectReason;
    private String shippingPhotos;
    private String remark;

    public Long getFlowId() { return flowId; }
    public void setFlowId(Long flowId) { this.flowId = flowId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getFlowStatus() { return flowStatus; }
    public void setFlowStatus(String flowStatus) { this.flowStatus = flowStatus; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public Date getActionTime() { return actionTime; }
    public void setActionTime(Date actionTime) { this.actionTime = actionTime; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public String getShippingPhotos() { return shippingPhotos; }
    public void setShippingPhotos(String shippingPhotos) { this.shippingPhotos = shippingPhotos; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
