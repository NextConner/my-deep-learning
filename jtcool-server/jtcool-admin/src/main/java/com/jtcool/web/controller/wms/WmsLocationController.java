package com.jtcool.web.controller.wms;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.domain.AjaxResult;
import com.jtcool.common.core.page.TableDataInfo;
import com.jtcool.wms.domain.WmsLocation;
import com.jtcool.wms.service.IWmsLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/wms/location")
public class WmsLocationController extends BaseController {
    @Autowired
    private IWmsLocationService wmsLocationService;

    @GetMapping("/list")
    public TableDataInfo list(WmsLocation wmsLocation) {
        startPage();
        List<WmsLocation> list = wmsLocationService.selectWmsLocationList(wmsLocation);
        return getDataTable(list);
    }

    @GetMapping("/{locationId}")
    public AjaxResult getInfo(@PathVariable Long locationId) {
        return success(wmsLocationService.selectWmsLocationById(locationId));
    }

    @GetMapping("/area/{areaId}")
    public AjaxResult getByAreaId(@PathVariable Long areaId) {
        return success(wmsLocationService.selectWmsLocationByAreaId(areaId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody WmsLocation wmsLocation) {
        return toAjax(wmsLocationService.insertWmsLocation(wmsLocation));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody WmsLocation wmsLocation) {
        return toAjax(wmsLocationService.updateWmsLocation(wmsLocation));
    }

    @DeleteMapping("/{locationIds}")
    public AjaxResult remove(@PathVariable Long[] locationIds) {
        return toAjax(wmsLocationService.deleteWmsLocationByIds(locationIds));
    }
}
