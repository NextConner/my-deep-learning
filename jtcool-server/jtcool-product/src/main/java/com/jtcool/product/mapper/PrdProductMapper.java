package com.jtcool.product.mapper;

import com.jtcool.product.domain.PrdProduct;
import java.util.List;

public interface PrdProductMapper {
    List<PrdProduct> selectPrdProductList(PrdProduct prdProduct);
    PrdProduct selectPrdProductById(Long productId);
    int insertPrdProduct(PrdProduct prdProduct);
    int updatePrdProduct(PrdProduct prdProduct);
    int deletePrdProductById(Long productId);
    int deletePrdProductByIds(Long[] productIds);
}
