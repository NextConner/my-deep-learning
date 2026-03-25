package com.jtcool.oms.service;

import com.jtcool.oms.domain.OmsOrder;
import java.util.List;

public interface IOmsOrderService {
    List<OmsOrder> selectOmsOrderList(OmsOrder omsOrder);
    OmsOrder selectOmsOrderById(Long orderId);
    int insertOmsOrder(OmsOrder omsOrder);
    int updateOmsOrder(OmsOrder omsOrder);
    int deleteOmsOrderByIds(Long[] orderIds);
    int updateOrderStatus(Long orderId, String status);

    // 订单工作流方法
    void confirmBySales(Long orderId, String operator);
    void reviewOrder(Long orderId, String operator);
    void confirmByWarehouse(Long orderId, String operator);
    void registerOutbound(Long orderId, String operator);
    void confirmShipment(Long orderId, String operator, String trackingNumber);
    void confirmReceipt(Long orderId, String operator);
}
