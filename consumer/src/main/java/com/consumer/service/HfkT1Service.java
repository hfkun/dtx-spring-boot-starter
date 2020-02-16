package com.consumer.service;

import com.consumer.domain.HfkT1;
import com.consumer.mapper.HfkT1Mapper;
import com.distributedtx.utils.DtxHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HfkT1Service {

    @Autowired
    private HfkT1Mapper hfkT1Mapper;
    @Autowired
    private DtxHttpUtil dtxHttpUtil;

    @Transactional
    public String update(Integer num) {

        hfkT1Mapper.updateById(new HfkT1(1,num,""+num));

        dtxHttpUtil.sendDtxPost("http://localhost:8089/leavebill/update",
                "{\"id\":\"1\",\"type\":"+num+",\"content\":\""+num+"\"}","provider");

//        int a = 10/0;
        return "ok";
    }
}
