package com.distributedtx.web;

import com.alibaba.fastjson.JSONObject;
import com.distributedtx.domain.MasterTask;
import com.distributedtx.service.MasterTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/master")
public class MasterTaskController {

    @Autowired
    private MasterTaskService masterTaskService;

    @PostMapping("/sendTask2Provider")
    public List<MasterTask> sendTask2Provider(@RequestBody JSONObject jsonObject){
        String moduleName = jsonObject.getString("moduleName");
        return masterTaskService.selectList(moduleName);
    }

    @PostMapping("/callBackMaster")
    public int callBackMaster(@RequestBody JSONObject jsonObject){
        Integer res = 1;
        try{
            String ids = jsonObject.getString("ids");
            masterTaskService.callBackMaster(ids.split(","));
        }catch (Exception e){
            e.printStackTrace();
            res = 0;
        }
        return res;
    }
}
