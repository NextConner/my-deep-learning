package com.jtcool.wms.service;

import com.jtcool.wms.domain.WmsWarehouse;
import java.util.List;

public interface IWmsWarehouseService {
    List<WmsWarehouse> selectWmsWarehouseList(WmsWarehouse wmsWarehouse);
    WmsWarehouse selectWmsWarehouseById(Long warehouseId);
    int insertWmsWarehouse(WmsWarehouse wmsWarehouse);
    int updateWmsWarehouse(WmsWarehouse wmsWarehouse);
    int deleteWmsWarehouseByIds(Long[] warehouseIds);
}
