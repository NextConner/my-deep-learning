package com.jtcool.wms.service.impl;

import com.jtcool.wms.domain.WmsArea;
import com.jtcool.wms.mapper.WmsAreaMapper;
import com.jtcool.wms.service.IWmsAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WmsAreaServiceImpl implements IWmsAreaService {

    @Autowired
    private WmsAreaMapper wmsAreaMapper;

    @Override
    public List<WmsArea> selectWmsAreaList(WmsArea wmsArea) {
        return wmsAreaMapper.selectWmsAreaList(wmsArea);
    }

    @Override
    public WmsArea selectWmsAreaById(Long areaId) {
        return wmsAreaMapper.selectWmsAreaById(areaId);
    }

    @Override
    public List<WmsArea> selectWmsAreaByWarehouseId(Long warehouseId) {
        return wmsAreaMapper.selectWmsAreaByWarehouseId(warehouseId);
    }

    @Override
    public int insertWmsArea(WmsArea wmsArea) {
        return wmsAreaMapper.insertWmsArea(wmsArea);
    }

    @Override
    public int updateWmsArea(WmsArea wmsArea) {
        return wmsAreaMapper.updateWmsArea(wmsArea);
    }

    @Override
    public int deleteWmsAreaByIds(Long[] areaIds) {
        return wmsAreaMapper.deleteWmsAreaByIds(areaIds);
    }
}
