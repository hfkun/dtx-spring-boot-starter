package com.distributedtx.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class MasterTask {
    @TableId(type = IdType.ID_WORKER_STR)
    private String id;
    private String url;
    private String reqStr;
    private Integer status = 0;  // 0待处理 ， 1已处理
    private Date addTime = new Date();
    private Date processTime;
    private String moduleName;

    public MasterTask(String url, String reqStr, String moduleName){
        this.url = url;
        this.reqStr = reqStr;
        this.moduleName = moduleName;
    }
}
