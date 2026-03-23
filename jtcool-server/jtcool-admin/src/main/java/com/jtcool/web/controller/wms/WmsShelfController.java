package com.jtcool.web.controller.wms;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.domain.AjaxResult;
import com.jtcool.common.core.page.TableDataInfo;
import com.jtcool.wms.domain.WmsShelf;
import com.jtcool.wms.service.IWmsShelfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/wms/shelf")
public class WmsShelfController extends BaseController {
    @Autowired
    private IWmsShelfService wmsShelfService;

    @GetMapping("/list")
    public TableDataInfo list(WmsShelf wmsShelf) {
        startPage();
        List<WmsShelf> list = wmsShelfService.selectWmsShelfList(wmsShelf);
        return getDataTable(list);
    }

    @GetMapping("/{shelfId}")
    public AjaxResult getInfo(@PathVariable Long shelfId) {
        return success(wmsShelfService.selectWmsShelfById(shelfId));
    }

    @GetMapping("/location/{locationId}")
    public AjaxResult getByLocationId(@PathVariable Long locationId) {
        return success(wmsShelfService.selectWmsShelfByLocationId(locationId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody WmsShelf wmsShelf) {
        return toAjax(wmsShelfService.insertWmsShelf(wmsShelf));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody WmsShelf wmsShelf) {
        return toAjax(wmsShelfService.updateWmsShelf(wmsShelf));
    }

    @DeleteMapping("/{shelfIds}")
    public AjaxResult remove(@PathVariable Long[] shelfIds) {
        return toAjax(wmsShelfService.deleteWmsShelfByIds(shelfIds));
    }
}
