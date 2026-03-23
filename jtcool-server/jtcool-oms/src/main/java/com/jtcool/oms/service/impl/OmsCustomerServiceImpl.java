package com.jtcool.oms.service.impl;

import com.jtcool.oms.domain.OmsCustomer;
import com.jtcool.oms.mapper.OmsCustomerMapper;
import com.jtcool.oms.service.IOmsCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OmsCustomerServiceImpl implements IOmsCustomerService {
    @Autowired
    private OmsCustomerMapper omsCustomerMapper;

    @Override
    public List<OmsCustomer> selectOmsCustomerList(OmsCustomer omsCustomer) {
        return omsCustomerMapper.selectOmsCustomerList(omsCustomer);
    }

    @Override
    public OmsCustomer selectOmsCustomerById(Long customerId) {
        return omsCustomerMapper.selectOmsCustomerById(customerId);
    }

    @Override
    public int insertOmsCustomer(OmsCustomer omsCustomer) {
        return omsCustomerMapper.insertOmsCustomer(omsCustomer);
    }

    @Override
    public int updateOmsCustomer(OmsCustomer omsCustomer) {
        return omsCustomerMapper.updateOmsCustomer(omsCustomer);
    }

    @Override
    public int deleteOmsCustomerByIds(Long[] customerIds) {
        return omsCustomerMapper.deleteOmsCustomerByIds(customerIds);
    }
}
