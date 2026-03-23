package com.jtcool.oms.domain;

import com.jtcool.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;

public class OmsPayment extends BaseEntity {
    private Long paymentId;
    private Long financeId;
    private Long orderId;
    private BigDecimal paymentAmount;
    private Date paymentDate;
    private String paymentMethod;
    private String paymentAccount;
    private String voucherNo;
    private String delFlag;

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public Long getFinanceId() { return financeId; }
    public void setFinanceId(Long financeId) { this.financeId = financeId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }
    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentAccount() { return paymentAccount; }
    public void setPaymentAccount(String paymentAccount) { this.paymentAccount = paymentAccount; }
    public String getVoucherNo() { return voucherNo; }
    public void setVoucherNo(String voucherNo) { this.voucherNo = voucherNo; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
