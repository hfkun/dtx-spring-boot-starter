package com.distributedtx.job;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.distributedtx.constant.DtxConstant;
import com.distributedtx.domain.MasterTask;
import com.distributedtx.domain.ProviderTask;
import com.distributedtx.mapper.MasterTaskMapper;
import com.distributedtx.mapper.ProviderTaskMapper;
import com.distributedtx.utils.DtxHttpUtil;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 专门分发任务，提供者任务状态为0
 */
public class SendTask2ProviderJob implements Runnable {
    private String moduleName;
    private String consumerUrl; //调用者http地址
    private DtxHttpUtil dtxHttpUtil;

    private MasterTaskMapper masterTaskMapper;
    private ProviderTaskMapper providerTaskMapper;

    public SendTask2ProviderJob(String moduleName, MasterTaskMapper masterTaskMapper, ProviderTaskMapper providerTaskMapper, String consumerUrl, DtxHttpUtil dtxHttpUtil){
        this.moduleName = moduleName;
        this.masterTaskMapper = masterTaskMapper;
        this.providerTaskMapper = providerTaskMapper;
        this.consumerUrl = consumerUrl;
        this.dtxHttpUtil = dtxHttpUtil;
    }

    @Override
    public void run() {
        LambdaQueryWrapper<MasterTask> queryWrapper = new LambdaQueryWrapper<MasterTask>().eq(MasterTask::getModuleName, this.moduleName)
                .eq(MasterTask::getStatus, DtxConstant.PREPARE).orderByAsc(MasterTask::getAddTime);
        while(true){
            try {
                Thread.sleep(5000);

                List<MasterTask> masterTasks = null;
                if(StringUtils.isEmpty(consumerUrl)){ //本地访问
                    masterTasks = masterTaskMapper.selectList(queryWrapper);
                }else{ //远程访问
                    String resp = dtxHttpUtil.sendPost(consumerUrl+"/master/sendTask2Provider", "{\"moduleName\":\""+this.moduleName+"\"}");
                    if(!StringUtils.isEmpty(resp)){
                        masterTasks = JSONArray.parseArray(resp, MasterTask.class);
                    }
                }

                if(masterTasks==null || masterTasks.size()==0){
                    continue;
                }
                for(MasterTask masterTask:masterTasks){
                    ProviderTask providerTask = providerTaskMapper.selectById(masterTask.getId());
                    if(providerTask != null){
                        continue;
                    }
                    providerTask = new ProviderTask(masterTask.getId(), masterTask.getUrl(), masterTask.getReqStr());
                    providerTaskMapper.insert(providerTask);
                }

//                System.out.println("SendTask2ProviderJob");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
