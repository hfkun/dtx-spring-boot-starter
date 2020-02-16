package com.provider.web;

import com.provider.domain.LeaveBill;
import com.provider.service.LeaveBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leavebill")
public class LeaveBillController {

    @Autowired
    private LeaveBillService leaveBillService;

    @PostMapping("/update")
    public String update(@RequestBody LeaveBill bill){
        leaveBillService.updateById(bill);
        return "更新成功";
    }

    @GetMapping("/test")
    public String test(){
        return "测试成功";
    }
}
