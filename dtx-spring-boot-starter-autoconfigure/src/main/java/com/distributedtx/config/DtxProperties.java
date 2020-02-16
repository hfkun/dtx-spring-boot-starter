package com.distributedtx.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "dtx")
public class DtxProperties {
    private Integer tryTimes = 3; //提供方重试次数，超过次数需要人工干预
    private String consumerUrl; //如果没有配置，则表示是同一个数据库，不需要远程访问
}
