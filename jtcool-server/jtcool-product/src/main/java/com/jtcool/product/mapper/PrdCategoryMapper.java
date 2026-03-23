package com.jtcool.product.mapper;

import com.jtcool.product.domain.PrdCategory;
import java.util.List;

public interface PrdCategoryMapper {
    List<PrdCategory> selectPrdCategoryList(PrdCategory prdCategory);
    PrdCategory selectPrdCategoryById(Long categoryId);
    int insertPrdCategory(PrdCategory prdCategory);
    int updatePrdCategory(PrdCategory prdCategory);
    int deletePrdCategoryById(Long categoryId);
}
