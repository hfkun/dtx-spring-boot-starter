package com.distributedtx.constant;

public class DtxConstant {
    /** 请求事物任务关键字 */
    public static final String URL_SUFFIX = "distributedtx";

    /** 待处理 */
    public static final Integer PREPARE = 0;
    /** 已处理 */
    public static final Integer DONE = 1;
    /** 已回调 */
    public static final Integer CALLBACK = 2;
}
