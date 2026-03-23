package com.jtcool.product.service;

import com.jtcool.product.domain.PrdBrand;
import java.util.List;

public interface IPrdBrandService {
    List<PrdBrand> selectPrdBrandList(PrdBrand prdBrand);
    PrdBrand selectPrdBrandById(Long brandId);
    int insertPrdBrand(PrdBrand prdBrand);
    int updatePrdBrand(PrdBrand prdBrand);
    int deletePrdBrandByIds(Long[] brandIds);
}
