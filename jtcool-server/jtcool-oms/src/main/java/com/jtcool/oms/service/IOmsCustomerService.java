package com.jtcool.oms.service;

import com.jtcool.oms.domain.OmsCustomer;
import java.util.List;

public interface IOmsCustomerService {
    List<OmsCustomer> selectOmsCustomerList(OmsCustomer omsCustomer);
    OmsCustomer selectOmsCustomerById(Long customerId);
    int insertOmsCustomer(OmsCustomer omsCustomer);
    int updateOmsCustomer(OmsCustomer omsCustomer);
    int deleteOmsCustomerByIds(Long[] customerIds);
}
