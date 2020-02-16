package com.consumer.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public Map<String, Object> exceptionHandler(Exception e) {
        e.printStackTrace();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("errorCode", "500");
        map.put("errorMsg", "糟糕，出错了!");
        return map;
    }

}
