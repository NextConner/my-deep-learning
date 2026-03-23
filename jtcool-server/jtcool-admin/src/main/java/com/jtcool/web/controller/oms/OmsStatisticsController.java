package com.jtcool.web.controller.oms;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.page.TableDataInfo;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/oms/statistics")
public class OmsStatisticsController extends BaseController {

    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) String statType) {
        List<Map<String, Object>> list = new ArrayList<>();
        return getDataTable(list);
    }
}
