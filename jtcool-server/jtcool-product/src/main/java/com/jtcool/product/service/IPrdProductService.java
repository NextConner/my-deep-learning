package com.jtcool.product.service;

import com.jtcool.product.domain.PrdProduct;
import java.util.List;

public interface IPrdProductService {
    List<PrdProduct> selectPrdProductList(PrdProduct prdProduct);
    PrdProduct selectPrdProductById(Long productId);
    int insertPrdProduct(PrdProduct prdProduct);
    int updatePrdProduct(PrdProduct prdProduct);
    int deletePrdProductByIds(Long[] productIds);
}
