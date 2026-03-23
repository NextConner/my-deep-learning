package com.jtcool.product.service;

import com.jtcool.product.domain.PrdSupplier;
import java.util.List;

public interface IPrdSupplierService {
    List<PrdSupplier> selectPrdSupplierList(PrdSupplier prdSupplier);
    PrdSupplier selectPrdSupplierById(Long supplierId);
    int insertPrdSupplier(PrdSupplier prdSupplier);
    int updatePrdSupplier(PrdSupplier prdSupplier);
    int deletePrdSupplierByIds(Long[] supplierIds);
}
