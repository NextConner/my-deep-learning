package com.jtcool.web.controller.wms;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.page.TableDataInfo;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wms/inventoryLog")
public class WmsInventoryLogController extends BaseController {

    @GetMapping("/list")
    public TableDataInfo list() {
        List<Map<String, Object>> list = new ArrayList<>();
        return getDataTable(list);
    }
}
