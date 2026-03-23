package com.jtcool.product.mapper;

import com.jtcool.product.domain.PrdSupplier;
import java.util.List;

public interface PrdSupplierMapper {
    List<PrdSupplier> selectPrdSupplierList(PrdSupplier prdSupplier);
    PrdSupplier selectPrdSupplierById(Long supplierId);
    int insertPrdSupplier(PrdSupplier prdSupplier);
    int updatePrdSupplier(PrdSupplier prdSupplier);
    int deletePrdSupplierById(Long supplierId);
    int deletePrdSupplierByIds(Long[] supplierIds);
}
