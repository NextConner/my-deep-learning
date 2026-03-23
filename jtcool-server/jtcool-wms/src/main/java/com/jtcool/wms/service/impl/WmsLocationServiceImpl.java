package com.jtcool.wms.service.impl;

import com.jtcool.wms.domain.WmsLocation;
import com.jtcool.wms.mapper.WmsLocationMapper;
import com.jtcool.wms.service.IWmsLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WmsLocationServiceImpl implements IWmsLocationService {

    @Autowired
    private WmsLocationMapper wmsLocationMapper;

    @Override
    public List<WmsLocation> selectWmsLocationList(WmsLocation wmsLocation) {
        return wmsLocationMapper.selectWmsLocationList(wmsLocation);
    }

    @Override
    public WmsLocation selectWmsLocationById(Long locationId) {
        return wmsLocationMapper.selectWmsLocationById(locationId);
    }

    @Override
    public List<WmsLocation> selectWmsLocationByAreaId(Long areaId) {
        return wmsLocationMapper.selectWmsLocationByAreaId(areaId);
    }

    @Override
    public int insertWmsLocation(WmsLocation wmsLocation) {
        return wmsLocationMapper.insertWmsLocation(wmsLocation);
    }

    @Override
    public int updateWmsLocation(WmsLocation wmsLocation) {
        return wmsLocationMapper.updateWmsLocation(wmsLocation);
    }

    @Override
    public int deleteWmsLocationByIds(Long[] locationIds) {
        return wmsLocationMapper.deleteWmsLocationByIds(locationIds);
    }
}
