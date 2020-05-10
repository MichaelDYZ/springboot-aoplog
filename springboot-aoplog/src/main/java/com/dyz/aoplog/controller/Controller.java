package com.dyz.aoplog.controller;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dyz
 * @version 1.0
 * @date 2020/5/8 16:42
 */
@RestController
public class Controller {

    /**
     * 切面日志测试
     * @param name
     * @return
     */
    @GetMapping("/test")
    public String test(String name) {
        return "访问者："+name;
    }
}
