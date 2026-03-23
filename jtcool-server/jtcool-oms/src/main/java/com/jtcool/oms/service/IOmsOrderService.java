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
}
