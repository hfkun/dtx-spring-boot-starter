package com.distributedtx.config;

import com.distributedtx.job.CallbackMasterJob;
import com.distributedtx.job.DoProviderTaskJob;
import com.distributedtx.job.SendTask2ProviderJob;
import com.distributedtx.mapper.MasterTaskMapper;
import com.distributedtx.mapper.ProviderTaskMapper;
import com.distributedtx.utils.ApplicationContextUtil;
import com.distributedtx.utils.DtxHttpUtil;
import com.distributedtx.utils.ProxyCreater;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.net.URL;

@EnableConfigurationProperties({ DtxProperties.class })
public class DtxProviderAutoConfiguration implements ApplicationContextAware/*,WebMvcConfigurer*/ {
    @Value("${spring.application.name}")
    private String moduleName;
    @Autowired
    private DtxProperties dtxProperties;
    @Autowired
    private MasterTaskMapper masterTaskMapper;
    @Autowired
    private ProviderTaskMapper providerTaskMapper;
    @Autowired
    private DoProviderTaskJob doProviderTaskJob;
    @Autowired
    private DtxHttpUtil dtxHttpUtil;

    @Bean
    public DtxProviderAutoConfiguration dtxProviderConfiguration() throws Exception {
        init();
        start();
        return new DtxProviderAutoConfiguration();
    }

    /**
     * 初始化需要扫描的事物注解
     * 通过字节码操作，实现事物
     */
    public void init() throws Exception {
        ProxyCreater proxyCreater = new ProxyCreater(ApplicationContextUtil.dtxTransactionalScanPath);
        proxyCreater.initDtxProxy();

        System.out.println("dtxProvider init");
    }

    public void start(){
        new Thread(new SendTask2ProviderJob(moduleName, masterTaskMapper, providerTaskMapper, dtxProperties.getConsumerUrl(), dtxHttpUtil)).start();
        new Thread(new CallbackMasterJob(masterTaskMapper, providerTaskMapper, dtxProperties.getConsumerUrl(), dtxHttpUtil)).start();

        doProviderTaskJob.setTryTimes(dtxProperties.getTryTimes());
        new Thread(doProviderTaskJob).start();
        System.out.println("dtxProvider start");
    }
    // 分布式事物拦截器
    /*@Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DtxInterceptor()).addPathPatterns("/**");
    }*/
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtil.applicationContext = applicationContext;
    }
}
