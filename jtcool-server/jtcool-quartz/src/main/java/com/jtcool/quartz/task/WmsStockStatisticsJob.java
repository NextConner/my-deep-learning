package com.jtcool.quartz.task;

import com.jtcool.wms.domain.WmsStockStatistics;
import com.jtcool.wms.mapper.WmsStockStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * WMS出入库统计定时任务
 * 每日凌晨3:00执行，汇总前一天的出入库数据
 */
@Component("wmsStockStatisticsJob")
public class WmsStockStatisticsJob {

    @Autowired
    private WmsStockStatisticsMapper wmsStockStatisticsMapper;

    public void aggregateDaily() {
        Date yesterday = getYesterday();
        aggregate(yesterday, "DAILY");
    }

    public void aggregateMonthly() {
        Date lastMonth = getLastMonth();
        aggregate(lastMonth, "MONTHLY");
    }

    public void aggregateQuarterly() {
        Date lastQuarter = getLastQuarter();
        aggregate(lastQuarter, "QUARTERLY");
    }

    public void aggregateYearly() {
        Date lastYear = getLastYear();
        aggregate(lastYear, "YEARLY");
    }

    private void aggregate(Date statDate, String statType) {
        List<Map<String, Object>> rows = wmsStockStatisticsMapper.aggregateStockStatsByDate(statDate, statType);
        for (Map<String, Object> row : rows) {
            WmsStockStatistics stat = new WmsStockStatistics();
            stat.setStatDate(statDate);
            stat.setStatType(statType);
            stat.setWarehouseId(toLong(row.get("warehouse_id")));
            stat.setAreaId(toLong(row.get("area_id")));
            stat.setLocationId(toLong(row.get("location_id")));
            stat.setProductId(toLong(row.get("product_id")));
            stat.setCustomerId(toLong(row.get("customer_id")));
            stat.setSalesUserId(toLong(row.get("sales_user_id")));
            stat.setInQuantity(toInt(row.get("in_quantity")));
            stat.setOutQuantity(toInt(row.get("out_quantity")));
            wmsStockStatisticsMapper.upsertWmsStockStatistics(stat);
        }
    }

    private Date getYesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }

    private Date getLastMonth() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        return cal.getTime();
    }

    private Date getLastQuarter() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -3);
        return cal.getTime();
    }

    private Date getLastYear() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        return cal.getTime();
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Long) return (Long) val;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }

    private Integer toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof Number) return ((Number) val).intValue();
        return Integer.parseInt(val.toString());
    }
}
