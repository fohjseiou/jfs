package com.jerry.controller;

import com.jerry.response.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RequestMapping("/")
@RestController
public class TestController {
    @GetMapping("/test")
    public CommonResponse<?> test(){
        return CommonResponse.success();
    }
}
