package com.jtcool.oms.mapper;

import com.jtcool.oms.domain.OmsOrderStatistics;
import org.apache.ibatis.annotations.Param;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface OmsOrderStatisticsMapper {
    List<OmsOrderStatistics> selectOmsOrderStatisticsList(OmsOrderStatistics stat);
    int insertOmsOrderStatistics(OmsOrderStatistics stat);
    int upsertOmsOrderStatistics(OmsOrderStatistics stat);
    List<Map<String, Object>> aggregateOrderStatsByDate(@Param("statDate") Date statDate, @Param("statType") String statType);
}
