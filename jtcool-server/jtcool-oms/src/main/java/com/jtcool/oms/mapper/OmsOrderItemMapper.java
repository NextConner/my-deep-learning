package com.jtcool.oms.mapper;

import com.jtcool.oms.domain.OmsOrderItem;
import java.util.List;

public interface OmsOrderItemMapper {
    List<OmsOrderItem> selectOmsOrderItemByOrderId(Long orderId);
    int insertOmsOrderItem(OmsOrderItem omsOrderItem);
    int deleteOmsOrderItemByOrderId(Long orderId);
}
