package com.jtcool.oms.service.impl;

import com.jtcool.common.exception.ServiceException;
import com.jtcool.oms.domain.OmsOrder;
import com.jtcool.oms.domain.OmsOrderFlow;
import com.jtcool.oms.domain.enums.OrderStatusEnum;
import com.jtcool.oms.mapper.OmsOrderMapper;
import com.jtcool.oms.service.IOmsOrderFlowService;
import com.jtcool.oms.service.IOmsOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

@Service
public class OmsOrderServiceImpl implements IOmsOrderService {
    @Autowired
    private OmsOrderMapper omsOrderMapper;

    @Autowired
    private IOmsOrderFlowService orderFlowService;

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

    @Transactional
    private void transitionStatus(Long orderId, OrderStatusEnum targetStatus, String operator, String remark) {
        OmsOrder order = selectOmsOrderById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getOrderStatus());
        if (!currentStatus.canTransitionTo(targetStatus.getCode())) {
            throw new ServiceException("无效的状态流转: " + currentStatus.getDescription() + " -> " + targetStatus.getDescription());
        }

        order.setOrderStatus(targetStatus.getCode());
        updateOmsOrder(order);

        OmsOrderFlow flow = new OmsOrderFlow();
        flow.setOrderId(orderId);
        flow.setFlowStatus(targetStatus.getCode());
        flow.setOperatorName(operator);
        flow.setActionTime(new Date());
        flow.setRemark(remark);
        orderFlowService.insertOmsOrderFlow(flow);

        if (targetStatus == OrderStatusEnum.WAREHOUSE_CONFIRMED) {
            createWmsOutboundBill(order);
        }
    }

    private void createWmsOutboundBill(OmsOrder order) {
        // TODO: WMS集成 - 创建出库单
        // 需要在 pom.xml 中添加 jtcool-wms 依赖后实现
    }

    @Override
    public void confirmBySales(Long orderId, String operator) {
        transitionStatus(orderId, OrderStatusEnum.SALES_CONFIRMED, operator, "销售确认");
    }

    @Override
    public void reviewOrder(Long orderId, String operator) {
        transitionStatus(orderId, OrderStatusEnum.ORDER_REVIEWED, operator, "订单审核通过");
    }

    @Override
    public void confirmByWarehouse(Long orderId, String operator) {
        transitionStatus(orderId, OrderStatusEnum.WAREHOUSE_CONFIRMED, operator, "仓库确认");
    }

    @Override
    public void registerOutbound(Long orderId, String operator) {
        transitionStatus(orderId, OrderStatusEnum.OUTBOUND_REGISTERED, operator, "登记出库");
    }

    @Override
    public void confirmShipment(Long orderId, String operator, String trackingNumber) {
        transitionStatus(orderId, OrderStatusEnum.SHIPMENT_CONFIRMED, operator, "确认发货，物流单号：" + trackingNumber);
    }

    @Override
    public void confirmReceipt(Long orderId, String operator) {
        transitionStatus(orderId, OrderStatusEnum.CUSTOMER_RECEIVED, operator, "客户签收");
    }
}
