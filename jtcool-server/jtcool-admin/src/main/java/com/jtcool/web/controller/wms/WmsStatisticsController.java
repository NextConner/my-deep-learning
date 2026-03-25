package com.jtcool.web.controller.wms;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.page.TableDataInfo;
import com.jtcool.wms.domain.WmsStockStatistics;
import com.jtcool.wms.mapper.WmsStockStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/wms/statistics")
public class WmsStatisticsController extends BaseController {

    @Autowired
    private WmsStockStatisticsMapper wmsStockStatisticsMapper;

    @GetMapping("/list")
    public TableDataInfo list(WmsStockStatistics stat) {
        startPage();
        List<WmsStockStatistics> list = wmsStockStatisticsMapper.selectWmsStockStatisticsList(stat);
        return getDataTable(list);
    }
}
