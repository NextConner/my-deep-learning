package com.jtcool.web.controller.wms;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.domain.AjaxResult;
import com.jtcool.common.core.page.TableDataInfo;
import com.jtcool.wms.domain.WmsInventory;
import com.jtcool.wms.service.IWmsInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/wms/inventory")
public class WmsInventoryController extends BaseController {
    @Autowired
    private IWmsInventoryService wmsInventoryService;

    @GetMapping("/list")
    public TableDataInfo list(WmsInventory wmsInventory) {
        startPage();
        List<WmsInventory> list = wmsInventoryService.selectWmsInventoryList(wmsInventory);
        return getDataTable(list);
    }

    @GetMapping("/{inventoryId}")
    public AjaxResult getInfo(@PathVariable Long inventoryId) {
        return success(wmsInventoryService.selectWmsInventoryById(inventoryId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody WmsInventory wmsInventory) {
        return toAjax(wmsInventoryService.insertWmsInventory(wmsInventory));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody WmsInventory wmsInventory) {
        return toAjax(wmsInventoryService.updateWmsInventory(wmsInventory));
    }
}
