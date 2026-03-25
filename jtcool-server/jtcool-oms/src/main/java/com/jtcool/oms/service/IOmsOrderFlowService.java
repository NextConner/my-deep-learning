package com.jtcool.oms.service;

import com.jtcool.oms.domain.OmsOrderFlow;
import java.util.List;

public interface IOmsOrderFlowService {
    int insertOmsOrderFlow(OmsOrderFlow omsOrderFlow);
    List<OmsOrderFlow> selectOmsOrderFlowByOrderId(Long orderId);
}
