package com.distributedtx.config;

import lombok.Data;

@Data
public class DtxConfiguration {
    private Integer tryTimes;
    private String consumerUrl;
}
