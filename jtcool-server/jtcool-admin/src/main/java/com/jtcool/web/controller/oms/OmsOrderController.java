package com.jtcool.web.controller.oms;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.domain.AjaxResult;
import com.jtcool.common.core.page.TableDataInfo;
import com.jtcool.oms.domain.OmsOrder;
import com.jtcool.oms.service.IOmsOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/oms/order")
public class OmsOrderController extends BaseController {
    @Autowired
    private IOmsOrderService omsOrderService;

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
}
