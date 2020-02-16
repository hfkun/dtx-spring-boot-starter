package com.provider.service;

import com.distributedtx.annotation.DtxParam;
import com.distributedtx.annotation.DtxTransactional;
import com.distributedtx.domain.CommonParam;
import com.provider.domain.LeaveBill;
import com.provider.mapper.LeaveBillMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.sql.SQLOutput;
import java.util.List;
import java.util.Map;

@Service
public class LeaveBillService {
    @Autowired
    private LeaveBillMapper leaveBillMapper;

    @DtxTransactional
    public int updateById(LeaveBill leaveBill){
        return leaveBillMapper.updateById(leaveBill);
    }

}
