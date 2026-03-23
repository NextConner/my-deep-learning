package com.jtcool.web.controller.oms;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.domain.AjaxResult;
import com.jtcool.common.core.page.TableDataInfo;
import com.jtcool.oms.domain.OmsCustomer;
import com.jtcool.oms.service.IOmsCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/oms/customer")
public class OmsCustomerController extends BaseController {
    @Autowired
    private IOmsCustomerService omsCustomerService;

    @GetMapping("/list")
    public TableDataInfo list(OmsCustomer omsCustomer) {
        startPage();
        List<OmsCustomer> list = omsCustomerService.selectOmsCustomerList(omsCustomer);
        return getDataTable(list);
    }

    @GetMapping("/{customerId}")
    public AjaxResult getInfo(@PathVariable Long customerId) {
        return success(omsCustomerService.selectOmsCustomerById(customerId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody OmsCustomer omsCustomer) {
        return toAjax(omsCustomerService.insertOmsCustomer(omsCustomer));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody OmsCustomer omsCustomer) {
        return toAjax(omsCustomerService.updateOmsCustomer(omsCustomer));
    }

    @DeleteMapping("/{customerIds}")
    public AjaxResult remove(@PathVariable Long[] customerIds) {
        return toAjax(omsCustomerService.deleteOmsCustomerByIds(customerIds));
    }
}
