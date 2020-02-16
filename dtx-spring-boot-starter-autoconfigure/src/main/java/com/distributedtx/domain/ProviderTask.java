package com.distributedtx.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class ProviderTask {
    @TableId
    private String id;
    private String url;
    private String reqStr;
    private Integer status = 0;  // 0待处理 ， 1已处理，2已回调
    private Date addTime = new Date();
    private Date processTime;
    private Integer tryTimes = 0; //重试次数

    public ProviderTask(String id, String url, String reqStr){
        this.id = id;
        this.url = url;
        this.reqStr = reqStr;
    }
}
