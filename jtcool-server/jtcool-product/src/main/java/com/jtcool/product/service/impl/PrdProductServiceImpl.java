package com.jtcool.product.service.impl;

import com.jtcool.product.domain.PrdProduct;
import com.jtcool.product.mapper.PrdProductMapper;
import com.jtcool.product.service.IPrdProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PrdProductServiceImpl implements IPrdProductService {
    @Autowired
    private PrdProductMapper prdProductMapper;

    @Override
    public List<PrdProduct> selectPrdProductList(PrdProduct prdProduct) {
        return prdProductMapper.selectPrdProductList(prdProduct);
    }

    @Override
    public PrdProduct selectPrdProductById(Long productId) {
        return prdProductMapper.selectPrdProductById(productId);
    }

    @Override
    public int insertPrdProduct(PrdProduct prdProduct) {
        return prdProductMapper.insertPrdProduct(prdProduct);
    }

    @Override
    public int updatePrdProduct(PrdProduct prdProduct) {
        return prdProductMapper.updatePrdProduct(prdProduct);
    }

    @Override
    public int deletePrdProductByIds(Long[] productIds) {
        return prdProductMapper.deletePrdProductByIds(productIds);
    }
}
