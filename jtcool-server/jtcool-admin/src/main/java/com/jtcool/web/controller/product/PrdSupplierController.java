package com.jtcool.web.controller.product;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.domain.AjaxResult;
import com.jtcool.common.core.page.TableDataInfo;
import com.jtcool.product.domain.PrdSupplier;
import com.jtcool.product.service.IPrdSupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/product/supplier")
public class PrdSupplierController extends BaseController {
    @Autowired
    private IPrdSupplierService prdSupplierService;

    @GetMapping("/list")
    public TableDataInfo list(PrdSupplier prdSupplier) {
        startPage();
        List<PrdSupplier> list = prdSupplierService.selectPrdSupplierList(prdSupplier);
        return getDataTable(list);
    }

    @GetMapping("/{supplierId}")
    public AjaxResult getInfo(@PathVariable Long supplierId) {
        return success(prdSupplierService.selectPrdSupplierById(supplierId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody PrdSupplier prdSupplier) {
        return toAjax(prdSupplierService.insertPrdSupplier(prdSupplier));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody PrdSupplier prdSupplier) {
        return toAjax(prdSupplierService.updatePrdSupplier(prdSupplier));
    }

    @DeleteMapping("/{supplierIds}")
    public AjaxResult remove(@PathVariable Long[] supplierIds) {
        return toAjax(prdSupplierService.deletePrdSupplierByIds(supplierIds));
    }
}
