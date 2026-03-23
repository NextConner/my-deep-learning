package com.jtcool.product.service.impl;

import com.jtcool.product.domain.PrdBrand;
import com.jtcool.product.mapper.PrdBrandMapper;
import com.jtcool.product.service.IPrdBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PrdBrandServiceImpl implements IPrdBrandService {
    @Autowired
    private PrdBrandMapper prdBrandMapper;

    @Override
    public List<PrdBrand> selectPrdBrandList(PrdBrand prdBrand) {
        return prdBrandMapper.selectPrdBrandList(prdBrand);
    }

    @Override
    public PrdBrand selectPrdBrandById(Long brandId) {
        return prdBrandMapper.selectPrdBrandById(brandId);
    }

    @Override
    public int insertPrdBrand(PrdBrand prdBrand) {
        return prdBrandMapper.insertPrdBrand(prdBrand);
    }

    @Override
    public int updatePrdBrand(PrdBrand prdBrand) {
        return prdBrandMapper.updatePrdBrand(prdBrand);
    }

    @Override
    public int deletePrdBrandByIds(Long[] brandIds) {
        return prdBrandMapper.deletePrdBrandByIds(brandIds);
    }
}
