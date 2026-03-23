package com.jtcool.product.mapper;

import com.jtcool.product.domain.PrdBrand;
import java.util.List;

public interface PrdBrandMapper {
    List<PrdBrand> selectPrdBrandList(PrdBrand prdBrand);
    PrdBrand selectPrdBrandById(Long brandId);
    int insertPrdBrand(PrdBrand prdBrand);
    int updatePrdBrand(PrdBrand prdBrand);
    int deletePrdBrandById(Long brandId);
    int deletePrdBrandByIds(Long[] brandIds);
}
