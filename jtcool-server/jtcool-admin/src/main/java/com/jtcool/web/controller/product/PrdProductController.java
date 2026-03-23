package com.jtcool.web.controller.product;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.domain.AjaxResult;
import com.jtcool.common.core.page.TableDataInfo;
import com.jtcool.product.domain.PrdProduct;
import com.jtcool.product.service.IPrdProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/product/product")
public class PrdProductController extends BaseController {
    @Autowired
    private IPrdProductService prdProductService;

    @GetMapping("/list")
    public TableDataInfo list(PrdProduct prdProduct) {
        startPage();
        List<PrdProduct> list = prdProductService.selectPrdProductList(prdProduct);
        return getDataTable(list);
    }

    @GetMapping("/{productId}")
    public AjaxResult getInfo(@PathVariable Long productId) {
        return success(prdProductService.selectPrdProductById(productId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody PrdProduct prdProduct) {
        return toAjax(prdProductService.insertPrdProduct(prdProduct));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody PrdProduct prdProduct) {
        return toAjax(prdProductService.updatePrdProduct(prdProduct));
    }

    @DeleteMapping("/{productIds}")
    public AjaxResult remove(@PathVariable Long[] productIds) {
        return toAjax(prdProductService.deletePrdProductByIds(productIds));
    }
}
