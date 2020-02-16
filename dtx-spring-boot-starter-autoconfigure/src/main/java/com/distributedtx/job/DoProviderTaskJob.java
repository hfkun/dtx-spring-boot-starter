package com.distributedtx.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.distributedtx.constant.DtxConstant;
import com.distributedtx.domain.ProviderTask;
import com.distributedtx.mapper.ProviderTaskMapper;
import com.distributedtx.utils.DtxHttpUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 处理任务
 */
@Data
public class DoProviderTaskJob implements Runnable{
    private Integer tryTimes;
//    private final Pattern urlPattern = Pattern.compile("(http|https):\\/\\/(.+?)(:?)(\\d*?)/.+");
    @Autowired
    private ProviderTaskMapper providerTaskMapper;

    @Autowired
    private DtxHttpUtil dtxHttpUtil;
//    @Autowired
//    private ApplicationContext applicationContext;
//    @Autowired
//    private Environment environment;

    public DoProviderTaskJob(){}
    public DoProviderTaskJob(Integer tryTimes){
        this.tryTimes = tryTimes;
    }

    @Override
    public void run() {

        LambdaQueryWrapper<ProviderTask> queryWrapper = new LambdaQueryWrapper<ProviderTask>().eq(ProviderTask::getStatus, DtxConstant.PREPARE)
                .le(ProviderTask::getTryTimes, tryTimes);
        ProviderTask providerTask = null;
        while(true){
            try {
                Thread.sleep(5000);

                List<ProviderTask> providerTasks = providerTaskMapper.selectList(queryWrapper);
                if(providerTasks==null || providerTasks.size()==0){
                    continue;
                }
                for(int i=0; i<providerTasks.size(); i++){
                    providerTask = providerTasks.get(i);
                    try{
                        String reqStr = providerTask.getReqStr();
                        reqStr = reqStr.substring(1);
                        if(reqStr.length()>2){
                            reqStr = "{\"msgId\":\""+providerTask.getId()+"\","+reqStr;
                        }else{
                            reqStr = "{\"msgId\":\""+providerTask.getId()+"\""+reqStr;
                        }
                        dtxHttpUtil.sendPost(providerTask.getUrl(), reqStr);
                    }catch (Exception e){
                        providerTaskMapper.updateFail(providerTask.getId());
                    }
                }

                System.out.println("DoProviderTaskJob");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
