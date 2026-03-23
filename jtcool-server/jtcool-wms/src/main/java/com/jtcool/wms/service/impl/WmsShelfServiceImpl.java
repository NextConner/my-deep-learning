package com.jtcool.wms.service.impl;

import com.jtcool.wms.domain.WmsShelf;
import com.jtcool.wms.mapper.WmsShelfMapper;
import com.jtcool.wms.service.IWmsShelfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WmsShelfServiceImpl implements IWmsShelfService {

    @Autowired
    private WmsShelfMapper wmsShelfMapper;

    @Override
    public List<WmsShelf> selectWmsShelfList(WmsShelf wmsShelf) {
        return wmsShelfMapper.selectWmsShelfList(wmsShelf);
    }

    @Override
    public WmsShelf selectWmsShelfById(Long shelfId) {
        return wmsShelfMapper.selectWmsShelfById(shelfId);
    }

    @Override
    public List<WmsShelf> selectWmsShelfByLocationId(Long locationId) {
        return wmsShelfMapper.selectWmsShelfByLocationId(locationId);
    }

    @Override
    public int insertWmsShelf(WmsShelf wmsShelf) {
        return wmsShelfMapper.insertWmsShelf(wmsShelf);
    }

    @Override
    public int updateWmsShelf(WmsShelf wmsShelf) {
        return wmsShelfMapper.updateWmsShelf(wmsShelf);
    }

    @Override
    public int deleteWmsShelfByIds(Long[] shelfIds) {
        return wmsShelfMapper.deleteWmsShelfByIds(shelfIds);
    }
}
