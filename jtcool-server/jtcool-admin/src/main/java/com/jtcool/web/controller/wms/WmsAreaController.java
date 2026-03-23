package com.jtcool.web.controller.wms;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.domain.AjaxResult;
import com.jtcool.common.core.page.TableDataInfo;
import com.jtcool.wms.domain.WmsArea;
import com.jtcool.wms.service.IWmsAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/wms/area")
public class WmsAreaController extends BaseController {
    @Autowired
    private IWmsAreaService wmsAreaService;

    @GetMapping("/list")
    public TableDataInfo list(WmsArea wmsArea) {
        startPage();
        List<WmsArea> list = wmsAreaService.selectWmsAreaList(wmsArea);
        return getDataTable(list);
    }

    @GetMapping("/{areaId}")
    public AjaxResult getInfo(@PathVariable Long areaId) {
        return success(wmsAreaService.selectWmsAreaById(areaId));
    }

    @GetMapping("/warehouse/{warehouseId}")
    public AjaxResult getByWarehouseId(@PathVariable Long warehouseId) {
        return success(wmsAreaService.selectWmsAreaByWarehouseId(warehouseId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody WmsArea wmsArea) {
        return toAjax(wmsAreaService.insertWmsArea(wmsArea));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody WmsArea wmsArea) {
        return toAjax(wmsAreaService.updateWmsArea(wmsArea));
    }

    @DeleteMapping("/{areaIds}")
    public AjaxResult remove(@PathVariable Long[] areaIds) {
        return toAjax(wmsAreaService.deleteWmsAreaByIds(areaIds));
    }
}
