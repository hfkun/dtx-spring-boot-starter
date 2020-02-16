package com.provider;

import com.distributedtx.annotation.EnableDtxProvider;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDtxProvider
@SpringBootApplication
@MapperScan(value={"com.provider.mapper*"})
public class AppProvider {

    public static void main(String[] args) {
        SpringApplication.run(AppProvider.class, args);
    }

}
