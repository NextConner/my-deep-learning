package com.jtcool.wms.mapper;

import com.jtcool.wms.domain.WmsStockStatistics;
import org.apache.ibatis.annotations.Param;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface WmsStockStatisticsMapper {
    List<WmsStockStatistics> selectWmsStockStatisticsList(WmsStockStatistics stat);
    int insertWmsStockStatistics(WmsStockStatistics stat);
    int upsertWmsStockStatistics(WmsStockStatistics stat);
    List<Map<String, Object>> aggregateStockStatsByDate(@Param("statDate") Date statDate, @Param("statType") String statType);
}
