package com.jtcool.wms.service;

import com.jtcool.wms.domain.WmsShelf;
import java.util.List;

public interface IWmsShelfService {
    List<WmsShelf> selectWmsShelfList(WmsShelf wmsShelf);
    WmsShelf selectWmsShelfById(Long shelfId);
    List<WmsShelf> selectWmsShelfByLocationId(Long locationId);
    int insertWmsShelf(WmsShelf wmsShelf);
    int updateWmsShelf(WmsShelf wmsShelf);
    int deleteWmsShelfByIds(Long[] shelfIds);
}
