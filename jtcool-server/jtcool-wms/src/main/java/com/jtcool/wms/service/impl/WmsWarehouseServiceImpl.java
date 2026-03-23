package com.jtcool.wms.service.impl;

import com.jtcool.wms.domain.WmsWarehouse;
import com.jtcool.wms.mapper.WmsWarehouseMapper;
import com.jtcool.wms.service.IWmsWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WmsWarehouseServiceImpl implements IWmsWarehouseService {

    @Autowired
    private WmsWarehouseMapper wmsWarehouseMapper;

    @Override
    public List<WmsWarehouse> selectWmsWarehouseList(WmsWarehouse wmsWarehouse) {
        return wmsWarehouseMapper.selectWmsWarehouseList(wmsWarehouse);
    }

    @Override
    public WmsWarehouse selectWmsWarehouseById(Long warehouseId) {
        return wmsWarehouseMapper.selectWmsWarehouseById(warehouseId);
    }

    @Override
    public int insertWmsWarehouse(WmsWarehouse wmsWarehouse) {
        return wmsWarehouseMapper.insertWmsWarehouse(wmsWarehouse);
    }

    @Override
    public int updateWmsWarehouse(WmsWarehouse wmsWarehouse) {
        return wmsWarehouseMapper.updateWmsWarehouse(wmsWarehouse);
    }

    @Override
    public int deleteWmsWarehouseByIds(Long[] warehouseIds) {
        return wmsWarehouseMapper.deleteWmsWarehouseByIds(warehouseIds);
    }
}
