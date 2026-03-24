package com.jtcool.web.controller.wms;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.page.TableDataInfo;
import com.jtcool.wms.domain.WmsInventoryLog;
import com.jtcool.wms.mapper.WmsInventoryLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/wms/inventoryLog")
public class WmsInventoryLogController extends BaseController {

    @Autowired
    private WmsInventoryLogMapper wmsInventoryLogMapper;

    @GetMapping("/list")
    public TableDataInfo list(WmsInventoryLog wmsInventoryLog) {
        startPage();
        List<WmsInventoryLog> list = wmsInventoryLogMapper.selectWmsInventoryLogList(wmsInventoryLog);
        return getDataTable(list);
    }
}
