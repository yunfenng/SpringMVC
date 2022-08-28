package com.lagou.demo.service.impl;

import com.lagou.demo.service.IDemoService;
import com.lagou.edu.mvcframework.annotations.LagouService;

/**
 * @author Jaa
 * @date 2022/1/15 21:17
 */
@LagouService
public class DemoServiceImpl implements IDemoService {
    @Override
    public String get(String name) {
        System.out.println("service 实现类中的name参数：" + name);
        return name;
    }
}
