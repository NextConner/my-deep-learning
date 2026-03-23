package com.jtcool.web.controller.wms;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.page.TableDataInfo;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wms/statistics")
public class WmsStatisticsController extends BaseController {

    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) String statType) {
        List<Map<String, Object>> list = new ArrayList<>();
        return getDataTable(list);
    }
}
