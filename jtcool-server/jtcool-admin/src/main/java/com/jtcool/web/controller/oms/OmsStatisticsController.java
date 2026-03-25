package com.jtcool.web.controller.oms;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.page.TableDataInfo;
import com.jtcool.oms.domain.OmsOrderStatistics;
import com.jtcool.oms.mapper.OmsOrderStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/oms/statistics")
public class OmsStatisticsController extends BaseController {

    @Autowired
    private OmsOrderStatisticsMapper omsOrderStatisticsMapper;

    @GetMapping("/list")
    public TableDataInfo list(OmsOrderStatistics stat) {
        startPage();
        List<OmsOrderStatistics> list = omsOrderStatisticsMapper.selectOmsOrderStatisticsList(stat);
        return getDataTable(list);
    }
}
