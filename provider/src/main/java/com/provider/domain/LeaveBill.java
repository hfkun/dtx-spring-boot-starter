package com.provider.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LeaveBill {
    private Integer id;
    private String content;
    private Integer type;

    @TableField(exist = false)
    private String msgId;
}
