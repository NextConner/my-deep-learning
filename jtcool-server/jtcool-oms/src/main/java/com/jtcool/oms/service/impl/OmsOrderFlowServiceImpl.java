package com.jtcool.oms.service.impl;

import com.jtcool.oms.domain.OmsOrderFlow;
import com.jtcool.oms.mapper.OmsOrderFlowMapper;
import com.jtcool.oms.service.IOmsOrderFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OmsOrderFlowServiceImpl implements IOmsOrderFlowService {
    @Autowired
    private OmsOrderFlowMapper omsOrderFlowMapper;

    @Override
    public int insertOmsOrderFlow(OmsOrderFlow omsOrderFlow) {
        return omsOrderFlowMapper.insertOmsOrderFlow(omsOrderFlow);
    }

    @Override
    public List<OmsOrderFlow> selectOmsOrderFlowByOrderId(Long orderId) {
        return omsOrderFlowMapper.selectOmsOrderFlowByOrderId(orderId);
    }
}
