package com.jtcool.wms.mapper;

import com.jtcool.wms.domain.WmsArea;
import java.util.List;

public interface WmsAreaMapper {
    List<WmsArea> selectWmsAreaList(WmsArea wmsArea);
    WmsArea selectWmsAreaById(Long areaId);
    List<WmsArea> selectWmsAreaByWarehouseId(Long warehouseId);
    int insertWmsArea(WmsArea wmsArea);
    int updateWmsArea(WmsArea wmsArea);
    int deleteWmsAreaByIds(Long[] areaIds);
}
