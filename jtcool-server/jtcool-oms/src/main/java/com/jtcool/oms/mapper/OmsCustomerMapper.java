package com.jtcool.oms.mapper;

import com.jtcool.oms.domain.OmsCustomer;
import java.util.List;

public interface OmsCustomerMapper {
    List<OmsCustomer> selectOmsCustomerList(OmsCustomer omsCustomer);
    OmsCustomer selectOmsCustomerById(Long customerId);
    int insertOmsCustomer(OmsCustomer omsCustomer);
    int updateOmsCustomer(OmsCustomer omsCustomer);
    int deleteOmsCustomerByIds(Long[] customerIds);
}
