package com.jtcool.wms.mapper;

import com.jtcool.wms.domain.WmsInventoryLog;
import java.util.List;

public interface WmsInventoryLogMapper {
    List<WmsInventoryLog> selectWmsInventoryLogList(WmsInventoryLog wmsInventoryLog);
    int insertWmsInventoryLog(WmsInventoryLog wmsInventoryLog);
}
