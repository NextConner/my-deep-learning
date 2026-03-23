package com.jtcool.oms.mapper;

import com.jtcool.oms.domain.OmsOrder;
import java.util.List;

public interface OmsOrderMapper {
    List<OmsOrder> selectOmsOrderList(OmsOrder omsOrder);
    OmsOrder selectOmsOrderById(Long orderId);
    int insertOmsOrder(OmsOrder omsOrder);
    int updateOmsOrder(OmsOrder omsOrder);
    int deleteOmsOrderByIds(Long[] orderIds);
}
