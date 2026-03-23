package com.jtcool.web.controller.wms;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.domain.AjaxResult;
import com.jtcool.common.core.page.TableDataInfo;
import com.jtcool.wms.domain.WmsWarehouse;
import com.jtcool.wms.service.IWmsWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/wms/warehouse")
public class WmsWarehouseController extends BaseController {
    @Autowired
    private IWmsWarehouseService wmsWarehouseService;

    @GetMapping("/list")
    public TableDataInfo list(WmsWarehouse wmsWarehouse) {
        startPage();
        List<WmsWarehouse> list = wmsWarehouseService.selectWmsWarehouseList(wmsWarehouse);
        return getDataTable(list);
    }

    @GetMapping("/{warehouseId}")
    public AjaxResult getInfo(@PathVariable Long warehouseId) {
        return success(wmsWarehouseService.selectWmsWarehouseById(warehouseId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody WmsWarehouse wmsWarehouse) {
        return toAjax(wmsWarehouseService.insertWmsWarehouse(wmsWarehouse));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody WmsWarehouse wmsWarehouse) {
        return toAjax(wmsWarehouseService.updateWmsWarehouse(wmsWarehouse));
    }

    @DeleteMapping("/{warehouseIds}")
    public AjaxResult remove(@PathVariable Long[] warehouseIds) {
        return toAjax(wmsWarehouseService.deleteWmsWarehouseByIds(warehouseIds));
    }
}
