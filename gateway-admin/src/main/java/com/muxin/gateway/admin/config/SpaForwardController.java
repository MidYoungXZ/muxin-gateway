package com.muxin.gateway.admin.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SPA兜底控制器.
 * 将所有非API、非静态资源的前端路由路径转发到 index.html，
 * 由 Vue Router 在客户端处理路由。
 */
@Controller
public class SpaForwardController {

    @RequestMapping(value = {
            "/dashboard",
            "/dashboard/**",
            "/login",
            "/profile",
            "/profile/**",
            "/system/**",
            "/routes/**",
            "/monitor/**",
            "/404",
            "/403"
    })
    public String forward() {
        return "forward:/index.html";
    }
}