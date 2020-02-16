package com.distributedtx.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.distributedtx.constant.DtxConstant;
import com.distributedtx.domain.ProviderTask;
import com.distributedtx.mapper.MasterTaskMapper;
import com.distributedtx.mapper.ProviderTaskMapper;
import com.distributedtx.utils.DtxHttpUtil;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 将提供者任务状态为1（已处理）的，更新到消费者
 */
public class CallbackMasterJob implements Runnable {

    private String consumerUrl; //调用者http地址
    private DtxHttpUtil dtxHttpUtil;

    private MasterTaskMapper masterTaskMapper;
    private ProviderTaskMapper providerTaskMapper;
    public CallbackMasterJob(MasterTaskMapper masterTaskMapper, ProviderTaskMapper providerTaskMapper, String consumerUrl, DtxHttpUtil dtxHttpUtil){
        this.masterTaskMapper = masterTaskMapper;
        this.providerTaskMapper = providerTaskMapper;
        this.consumerUrl = consumerUrl;
        this.dtxHttpUtil = dtxHttpUtil;
    }

    @Override
    public void run() {
        int res = 1;
        LambdaQueryWrapper<ProviderTask> queryWrapper = new LambdaQueryWrapper<ProviderTask>().eq(ProviderTask::getStatus, DtxConstant.DONE);
        while(true){
            try {
                Thread.sleep(5000);

                List<ProviderTask> providerTasks = providerTaskMapper.selectList(queryWrapper);
                if(providerTasks==null || providerTasks.size()==0){
                    continue;
                }
                String[] ids = new String[providerTasks.size()];
                for(int i=0; i<providerTasks.size(); i++){
                    ids[i] = providerTasks.get(i).getId();
                }

                res = 1;
                if(StringUtils.isEmpty(consumerUrl)){ //本地访问
                    masterTaskMapper.updateStatus(ids);
                }else{ //远程访问
                    try {
                        String resp = dtxHttpUtil.sendPost(consumerUrl + "/master/callBackMaster", "{\"ids\":\"" + String.join(",", ids) + "\"}");
                        if(!"1".equals(resp)){
                            res = 0;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        res = 0;
                    }
                }
                if(res == 1){
                    providerTaskMapper.updateStatus(ids);
                }
//                System.out.println("CallbackMasterJob");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
