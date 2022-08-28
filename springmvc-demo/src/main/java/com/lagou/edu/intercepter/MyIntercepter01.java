package com.lagou.edu.intercepter;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Jaa
 * @date 2022/1/14 21:38
 */
public class MyIntercepter01 implements HandlerInterceptor {

    /**
     * 会在 handler方法业务逻辑执行之前执行, 使用最多
     * 往往在这里完成权限校验功能
     * @param request
     * @param response
     * @param handler
     * @return 返回值 boolean代表是否方向, true方向, false中止
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("MyIntercepter01 preHandle...");
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    /**
     * 会在 handler方法业务逻辑执行之后尚未跳转页面时执行
     * @param request
     * @param response
     * @param handler
     * @param modelAndView 封装了视图和数据, 此时尚未跳转页面, 可以在这里对数据和视图进行修改
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("MyIntercepter01 postHandle...");
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    /**
     * 页面已经跳转渲染结束之后执行
     * @param request
     * @param response
     * @param handler
     * @param ex 可以在这里捕获异常
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("MyIntercepter01 afterCompletion...");
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
