package com.jtcool.wms.mapper;

import com.jtcool.wms.domain.WmsShelf;
import java.util.List;

public interface WmsShelfMapper {
    List<WmsShelf> selectWmsShelfList(WmsShelf wmsShelf);
    WmsShelf selectWmsShelfById(Long shelfId);
    List<WmsShelf> selectWmsShelfByLocationId(Long locationId);
    int insertWmsShelf(WmsShelf wmsShelf);
    int updateWmsShelf(WmsShelf wmsShelf);
    int deleteWmsShelfByIds(Long[] shelfIds);
}
