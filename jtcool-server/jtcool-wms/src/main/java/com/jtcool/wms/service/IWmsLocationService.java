package com.jtcool.wms.service;

import com.jtcool.wms.domain.WmsLocation;
import java.util.List;

public interface IWmsLocationService {
    List<WmsLocation> selectWmsLocationList(WmsLocation wmsLocation);
    WmsLocation selectWmsLocationById(Long locationId);
    List<WmsLocation> selectWmsLocationByAreaId(Long areaId);
    int insertWmsLocation(WmsLocation wmsLocation);
    int updateWmsLocation(WmsLocation wmsLocation);
    int deleteWmsLocationByIds(Long[] locationIds);
}
