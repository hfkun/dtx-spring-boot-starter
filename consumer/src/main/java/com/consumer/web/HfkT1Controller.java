package com.consumer.web;

import com.consumer.service.HfkT1Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class HfkT1Controller {

    @Autowired
    private HfkT1Service hfkT1Service;

    @RequestMapping("update")
    public String update(@RequestParam("num") Integer num) {
        hfkT1Service.update(num);
        return "更新成功";
    }
}
