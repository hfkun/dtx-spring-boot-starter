package com.distributedtx.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.distributedtx.constant.DtxConstant;
import com.distributedtx.domain.MasterTask;
import com.distributedtx.mapper.MasterTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class MasterTaskService {

    @Autowired
    private MasterTaskMapper masterTaskMapper;

    public int add(MasterTask masterTask){
        return masterTaskMapper.insert(masterTask);
    }

    public List<MasterTask> selectList(String moduleName){
        LambdaQueryWrapper<MasterTask> queryWrapper = new LambdaQueryWrapper<MasterTask>().eq(MasterTask::getModuleName, moduleName)
                .eq(MasterTask::getStatus, DtxConstant.PREPARE).orderByAsc(MasterTask::getAddTime);

        return masterTaskMapper.selectList(queryWrapper);
    }

    public int callBackMaster(String[] ids){
        return masterTaskMapper.updateStatus(ids);
    }

}
