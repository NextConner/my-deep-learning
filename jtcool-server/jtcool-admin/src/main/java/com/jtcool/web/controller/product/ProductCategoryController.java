package com.jtcool.web.controller.product;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.page.TableDataInfo;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product/category")
public class ProductCategoryController extends BaseController {

    @GetMapping("/list")
    public TableDataInfo list() {
        List<Map<String, Object>> list = new ArrayList<>();
        return getDataTable(list);
    }
}
