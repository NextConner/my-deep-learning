package com.jtcool.web.controller.product;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.domain.AjaxResult;
import com.jtcool.common.core.page.TableDataInfo;
import com.jtcool.product.domain.PrdBrand;
import com.jtcool.product.service.IPrdBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/product/brand")
public class PrdBrandController extends BaseController {
    @Autowired
    private IPrdBrandService prdBrandService;

    @GetMapping("/list")
    public TableDataInfo list(PrdBrand prdBrand) {
        startPage();
        List<PrdBrand> list = prdBrandService.selectPrdBrandList(prdBrand);
        return getDataTable(list);
    }

    @GetMapping("/{brandId}")
    public AjaxResult getInfo(@PathVariable Long brandId) {
        return success(prdBrandService.selectPrdBrandById(brandId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody PrdBrand prdBrand) {
        return toAjax(prdBrandService.insertPrdBrand(prdBrand));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody PrdBrand prdBrand) {
        return toAjax(prdBrandService.updatePrdBrand(prdBrand));
    }

    @DeleteMapping("/{brandIds}")
    public AjaxResult remove(@PathVariable Long[] brandIds) {
        return toAjax(prdBrandService.deletePrdBrandByIds(brandIds));
    }
}
