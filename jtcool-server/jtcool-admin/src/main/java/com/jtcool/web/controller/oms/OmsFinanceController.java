package com.jtcool.web.controller.oms;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.domain.AjaxResult;
import com.jtcool.common.core.page.TableDataInfo;
import com.jtcool.oms.domain.OmsFinance;
import com.jtcool.oms.domain.OmsPayment;
import com.jtcool.oms.service.IOmsFinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/oms/finance")
public class OmsFinanceController extends BaseController {
    @Autowired
    private IOmsFinanceService omsFinanceService;

    @GetMapping("/list")
    public TableDataInfo list(OmsFinance omsFinance) {
        startPage();
        List<OmsFinance> list = omsFinanceService.selectOmsFinanceList(omsFinance);
        return getDataTable(list);
    }

    @GetMapping("/{financeId}")
    public AjaxResult getInfo(@PathVariable Long financeId) {
        return success(omsFinanceService.selectOmsFinanceById(financeId));
    }

    @PostMapping("/payment")
    public AjaxResult addPayment(@RequestBody OmsPayment omsPayment) {
        return toAjax(omsFinanceService.addPayment(omsPayment));
    }

    @PutMapping("/invoice")
    public AjaxResult updateInvoice(@RequestBody OmsFinance omsFinance) {
        return toAjax(omsFinanceService.updateInvoice(omsFinance));
    }
}
