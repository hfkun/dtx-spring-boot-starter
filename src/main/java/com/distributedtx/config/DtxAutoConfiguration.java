package com.distributedtx.config;

import com.distributedtx.job.DoProviderTaskJob;
import com.distributedtx.service.MasterTaskService;
import com.distributedtx.utils.DtxHttpUtil;
import com.distributedtx.web.MasterTaskController;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DtxProperties.class)
@ConditionalOnClass(DtxConfiguration.class)
@MapperScan(value={"com.distributedtx.mapper*"})
public class DtxAutoConfiguration {

    @Autowired
    private DtxProperties dtxProperties;

    @Bean
    @ConditionalOnMissingBean(DtxConfiguration.class)
    public DtxConfiguration dtxConfiguration() {
        DtxConfiguration dtxConfiguration = new DtxConfiguration();
        dtxConfiguration.setTryTimes(dtxProperties.getTryTimes());
        dtxConfiguration.setConsumerUrl(dtxProperties.getConsumerUrl());
        return dtxConfiguration;
    }

    @Bean
    public DoProviderTaskJob getDoProviderTaskJob(){
        return new DoProviderTaskJob();
    }

    @Bean
    public MasterTaskController getMasterTaskController(){
        return new MasterTaskController();
    }

    @Bean
    public MasterTaskService getMasterTaskService(){
        return new MasterTaskService();
    }

    @Bean
    public DtxHttpUtil getDtxHttpUtil(){
        return new DtxHttpUtil();
    }
}
