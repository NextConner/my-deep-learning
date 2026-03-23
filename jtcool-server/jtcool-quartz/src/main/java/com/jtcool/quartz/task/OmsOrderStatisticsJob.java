package com.jtcool.quartz.task;

import com.jtcool.oms.domain.OmsOrderStatistics;
import com.jtcool.oms.mapper.OmsOrderStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * OMS订单统计定时任务
 * 每日凌晨2:00执行，汇总前一天的订单数据
 */
@Component("omsOrderStatisticsJob")
public class OmsOrderStatisticsJob {

    @Autowired
    private OmsOrderStatisticsMapper omsOrderStatisticsMapper;

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
        List<Map<String, Object>> rows = omsOrderStatisticsMapper.aggregateOrderStatsByDate(statDate, statType);
        for (Map<String, Object> row : rows) {
            OmsOrderStatistics stat = new OmsOrderStatistics();
            stat.setStatDate(statDate);
            stat.setStatType(statType);
            stat.setCustomerId(toLong(row.get("customer_id")));
            stat.setSalesUserId(toLong(row.get("sales_user_id")));
            stat.setOrderCount(toInt(row.get("order_count")));
            stat.setTotalAmount(toBigDecimal(row.get("total_amount")));
            stat.setPaidAmount(toBigDecimal(row.get("paid_amount")));
            stat.setUnpaidAmount(toBigDecimal(row.get("unpaid_amount")));
            omsOrderStatisticsMapper.upsertOmsOrderStatistics(stat);
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

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        return new BigDecimal(val.toString());
    }
}
