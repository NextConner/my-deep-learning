package com.jtcool.wms.mapper;

import com.jtcool.wms.domain.WmsWarehouse;
import java.util.List;

public interface WmsWarehouseMapper {
    List<WmsWarehouse> selectWmsWarehouseList(WmsWarehouse wmsWarehouse);
    WmsWarehouse selectWmsWarehouseById(Long warehouseId);
    int insertWmsWarehouse(WmsWarehouse wmsWarehouse);
    int updateWmsWarehouse(WmsWarehouse wmsWarehouse);
    int deleteWmsWarehouseByIds(Long[] warehouseIds);
}
