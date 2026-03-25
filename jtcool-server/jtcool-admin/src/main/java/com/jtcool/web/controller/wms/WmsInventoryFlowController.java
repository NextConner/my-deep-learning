package com.jtcool.web.controller.wms;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.domain.AjaxResult;
import com.jtcool.wms.service.IWmsInventoryFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wms/inventoryFlow")
public class WmsInventoryFlowController extends BaseController {

    @Autowired
    private IWmsInventoryFlowService inventoryFlowService;

    @GetMapping("/waterfall")
    public AjaxResult getWaterfallData(@RequestParam Long productId,
                                       @RequestParam Long warehouseId,
                                       @RequestParam String beginTime,
                                       @RequestParam String endTime,
                                       @RequestParam(defaultValue = "raw") String granularity) {
        Map<String, Object> data = inventoryFlowService.getWaterfallData(productId, warehouseId, beginTime, endTime, granularity);
        return AjaxResult.success(data);
    }

    @GetMapping("/locationActivity")
    public AjaxResult getLocationActivity(@RequestParam Long warehouseId,
                                          @RequestParam String beginTime,
                                          @RequestParam String endTime,
                                          @RequestParam(required = false) String billType) {
        Map<String, Object> data = inventoryFlowService.getLocationActivity(warehouseId, beginTime, endTime, billType);
        return AjaxResult.success(data);
    }

    @GetMapping("/multiProductTrends")
    public AjaxResult getMultiProductTrends(@RequestParam List<Long> productIds,
                                            @RequestParam(required = false) Long warehouseId,
                                            @RequestParam String beginTime,
                                            @RequestParam String endTime,
                                            @RequestParam(defaultValue = "day") String granularity) {
        Map<String, Object> data = inventoryFlowService.getMultiProductTrends(productIds, warehouseId, beginTime, endTime, granularity);
        return AjaxResult.success(data);
    }
}
