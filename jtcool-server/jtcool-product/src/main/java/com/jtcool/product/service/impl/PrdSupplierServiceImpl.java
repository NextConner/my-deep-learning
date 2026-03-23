package com.jtcool.product.service.impl;

import com.jtcool.product.domain.PrdSupplier;
import com.jtcool.product.mapper.PrdSupplierMapper;
import com.jtcool.product.service.IPrdSupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PrdSupplierServiceImpl implements IPrdSupplierService {
    @Autowired
    private PrdSupplierMapper prdSupplierMapper;

    @Override
    public List<PrdSupplier> selectPrdSupplierList(PrdSupplier prdSupplier) {
        return prdSupplierMapper.selectPrdSupplierList(prdSupplier);
    }

    @Override
    public PrdSupplier selectPrdSupplierById(Long supplierId) {
        return prdSupplierMapper.selectPrdSupplierById(supplierId);
    }

    @Override
    public int insertPrdSupplier(PrdSupplier prdSupplier) {
        return prdSupplierMapper.insertPrdSupplier(prdSupplier);
    }

    @Override
    public int updatePrdSupplier(PrdSupplier prdSupplier) {
        return prdSupplierMapper.updatePrdSupplier(prdSupplier);
    }

    @Override
    public int deletePrdSupplierByIds(Long[] supplierIds) {
        return prdSupplierMapper.deletePrdSupplierByIds(supplierIds);
    }
}
