package com.jtcool.oms.mapper;

import com.jtcool.oms.domain.OmsOrderFlow;
import java.util.List;

public interface OmsOrderFlowMapper {
    List<OmsOrderFlow> selectOmsOrderFlowByOrderId(Long orderId);
    int insertOmsOrderFlow(OmsOrderFlow omsOrderFlow);
}
