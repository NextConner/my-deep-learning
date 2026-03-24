package com.jtcool.web.controller.product;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.domain.AjaxResult;
import com.jtcool.common.core.page.TableDataInfo;
import com.jtcool.product.domain.PrdCategory;
import com.jtcool.product.mapper.PrdCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/product/category")
public class ProductCategoryController extends BaseController {

    @Autowired
    private PrdCategoryMapper prdCategoryMapper;

    @GetMapping("/list")
    public TableDataInfo list(PrdCategory prdCategory) {
        startPage();
        List<PrdCategory> list = prdCategoryMapper.selectPrdCategoryList(prdCategory);
        return getDataTable(list);
    }

    @GetMapping("/{categoryId}")
    public AjaxResult getInfo(@PathVariable Long categoryId) {
        return success(prdCategoryMapper.selectPrdCategoryById(categoryId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody PrdCategory prdCategory) {
        return toAjax(prdCategoryMapper.insertPrdCategory(prdCategory));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody PrdCategory prdCategory) {
        return toAjax(prdCategoryMapper.updatePrdCategory(prdCategory));
    }

    @DeleteMapping("/{categoryId}")
    public AjaxResult remove(@PathVariable Long categoryId) {
        return toAjax(prdCategoryMapper.deletePrdCategoryById(categoryId));
    }
}
