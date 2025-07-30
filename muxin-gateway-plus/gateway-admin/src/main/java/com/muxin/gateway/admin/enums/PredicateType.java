package com.muxin.gateway.admin.enums;

import com.muxin.gateway.admin.model.vo.ConfigFieldVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 断言类型枚举
 *
 * @author muxin
 */
@Getter
@AllArgsConstructor
public enum PredicateType {
    
    PATH("Path", "路径匹配", "支持Ant风格的路径匹配", Arrays.asList(
        ConfigFieldVO.builder()
            .field("patterns")
            .label("路径模式")
            .type("array")
            .required(true)
            .placeholder("/api/**")
            .description("支持Ant风格的路径匹配")
            .build()
    )),
    
    METHOD("Method", "请求方法匹配", "限制HTTP请求方法", Arrays.asList(
        ConfigFieldVO.builder()
            .field("methods")
            .label("HTTP方法")
            .type("array")
            .required(true)
            .defaultValue(Arrays.asList("GET", "POST"))
            .description("允许的HTTP请求方法")
            .build()
    )),
    
    HEADER("Header", "请求头匹配", "根据请求头进行路由", Arrays.asList(
        ConfigFieldVO.builder()
            .field("header")
            .label("请求头名称")
            .type("string")
            .required(true)
            .placeholder("Authorization")
            .build(),
        ConfigFieldVO.builder()
            .field("regexp")
            .label("匹配正则")
            .type("string")
            .required(false)
            .placeholder("Bearer .*")
            .description("正则表达式，为空则只检查请求头是否存在")
            .build()
    )),
    
    QUERY("Query", "查询参数匹配", "根据查询参数进行路由", Arrays.asList(
        ConfigFieldVO.builder()
            .field("param")
            .label("参数名")
            .type("string")
            .required(true)
            .build(),
        ConfigFieldVO.builder()
            .field("regexp")
            .label("匹配正则")
            .type("string")
            .required(false)
            .build()
    )),
    
    COOKIE("Cookie", "Cookie匹配", "根据Cookie进行路由", Arrays.asList(
        ConfigFieldVO.builder()
            .field("name")
            .label("Cookie名称")
            .type("string")
            .required(true)
            .build(),
        ConfigFieldVO.builder()
            .field("regexp")
            .label("匹配正则")
            .type("string")
            .required(false)
            .build()
    )),
    
    HOST("Host", "主机匹配", "根据请求主机进行路由", Arrays.asList(
        ConfigFieldVO.builder()
            .field("patterns")
            .label("主机模式")
            .type("array")
            .required(true)
            .placeholder("*.example.com")
            .description("支持通配符匹配")
            .build()
    )),
    
    REMOTE_ADDR("RemoteAddr", "客户端地址匹配", "根据客户端IP进行路由", Arrays.asList(
        ConfigFieldVO.builder()
            .field("sources")
            .label("IP地址段")
            .type("array")
            .required(true)
            .placeholder("192.168.0.0/16")
            .description("支持CIDR格式")
            .build()
    )),
    
    BETWEEN("Between", "时间段匹配", "限制访问时间段", Arrays.asList(
        ConfigFieldVO.builder()
            .field("datetime1")
            .label("开始时间")
            .type("datetime")
            .required(true)
            .build(),
        ConfigFieldVO.builder()
            .field("datetime2")
            .label("结束时间")
            .type("datetime")
            .required(true)
            .build()
    ));
    
    private final String type;
    private final String name;
    private final String description;
    private final List<ConfigFieldVO> configFields;
} 