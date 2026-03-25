package com.jtcool.web.controller.oms;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.domain.AjaxResult;
import com.jtcool.common.core.page.TableDataInfo;
import com.jtcool.common.utils.SecurityUtils;
import com.jtcool.oms.domain.OmsOrder;
import com.jtcool.oms.domain.OmsOrderFlow;
import com.jtcool.oms.service.IOmsOrderFlowService;
import com.jtcool.oms.service.IOmsOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/oms/order")
public class OmsOrderController extends BaseController {
    @Autowired
    private IOmsOrderService omsOrderService;

    @Autowired
    private IOmsOrderFlowService orderFlowService;

    @GetMapping("/list")
    public TableDataInfo list(OmsOrder omsOrder) {
        startPage();
        List<OmsOrder> list = omsOrderService.selectOmsOrderList(omsOrder);
        return getDataTable(list);
    }

    @GetMapping("/{orderId}")
    public AjaxResult getInfo(@PathVariable Long orderId) {
        return success(omsOrderService.selectOmsOrderById(orderId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody OmsOrder omsOrder) {
        return toAjax(omsOrderService.insertOmsOrder(omsOrder));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody OmsOrder omsOrder) {
        return toAjax(omsOrderService.updateOmsOrder(omsOrder));
    }

    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds) {
        return toAjax(omsOrderService.deleteOmsOrderByIds(orderIds));
    }

    @PutMapping("/status/{orderId}/{status}")
    public AjaxResult updateStatus(@PathVariable Long orderId, @PathVariable String status) {
        return toAjax(omsOrderService.updateOrderStatus(orderId, status));
    }

    @PostMapping("/workflow/{orderId}/sales-confirm")
    public AjaxResult confirmBySales(@PathVariable Long orderId) {
        String operator = SecurityUtils.getUsername();
        omsOrderService.confirmBySales(orderId, operator);
        return success("销售确认成功");
    }

    @PostMapping("/workflow/{orderId}/review")
    public AjaxResult reviewOrder(@PathVariable Long orderId) {
        String operator = SecurityUtils.getUsername();
        omsOrderService.reviewOrder(orderId, operator);
        return success("订单审核通过");
    }

    @PostMapping("/workflow/{orderId}/warehouse-confirm")
    public AjaxResult confirmByWarehouse(@PathVariable Long orderId) {
        String operator = SecurityUtils.getUsername();
        omsOrderService.confirmByWarehouse(orderId, operator);
        return success("仓库确认成功");
    }

    @PostMapping("/workflow/{orderId}/register-outbound")
    public AjaxResult registerOutbound(@PathVariable Long orderId) {
        String operator = SecurityUtils.getUsername();
        omsOrderService.registerOutbound(orderId, operator);
        return success("登记出库成功");
    }

    @PostMapping("/workflow/{orderId}/confirm-shipment")
    public AjaxResult confirmShipment(@PathVariable Long orderId, @RequestParam String trackingNumber) {
        String operator = SecurityUtils.getUsername();
        omsOrderService.confirmShipment(orderId, operator, trackingNumber);
        return success("确认发货成功");
    }

    @PostMapping("/workflow/{orderId}/confirm-receipt")
    public AjaxResult confirmReceipt(@PathVariable Long orderId) {
        String operator = SecurityUtils.getUsername();
        omsOrderService.confirmReceipt(orderId, operator);
        return success("确认签收成功");
    }

    @GetMapping("/workflow/{orderId}/flow")
    public AjaxResult getOrderFlow(@PathVariable Long orderId) {
        List<OmsOrderFlow> flows = orderFlowService.selectOmsOrderFlowByOrderId(orderId);
        return success(flows);
    }
}
