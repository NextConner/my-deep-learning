package com.jtcool.oms.service.impl;

import com.jtcool.oms.domain.OmsOrder;
import com.jtcool.oms.mapper.OmsOrderMapper;
import com.jtcool.oms.service.IOmsOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OmsOrderServiceImpl implements IOmsOrderService {
    @Autowired
    private OmsOrderMapper omsOrderMapper;

    @Override
    public List<OmsOrder> selectOmsOrderList(OmsOrder omsOrder) {
        return omsOrderMapper.selectOmsOrderList(omsOrder);
    }

    @Override
    public OmsOrder selectOmsOrderById(Long orderId) {
        return omsOrderMapper.selectOmsOrderById(orderId);
    }

    @Override
    public int insertOmsOrder(OmsOrder omsOrder) {
        return omsOrderMapper.insertOmsOrder(omsOrder);
    }

    @Override
    public int updateOmsOrder(OmsOrder omsOrder) {
        return omsOrderMapper.updateOmsOrder(omsOrder);
    }

    @Override
    public int deleteOmsOrderByIds(Long[] orderIds) {
        return omsOrderMapper.deleteOmsOrderByIds(orderIds);
    }

    @Override
    public int updateOrderStatus(Long orderId, String status) {
        OmsOrder order = new OmsOrder();
        order.setOrderId(orderId);
        order.setOrderStatus(status);
        return omsOrderMapper.updateOmsOrder(order);
    }
}
