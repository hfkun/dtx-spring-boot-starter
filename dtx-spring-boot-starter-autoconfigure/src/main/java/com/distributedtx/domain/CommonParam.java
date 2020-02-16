package com.distributedtx.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * 通用参数，为了获取taskId
 * controller 和 service 方法中必须要加一个参数包含有msgId方法（建议放在参数列表的第一个位置）
 * 如果原来业务中不需要参数，为了传递消息，加此类
 */
@Data
public class CommonParam {
    @TableField(exist = false)
    private String msgId;
}
