package com.muxin.gateway.core.route;

import com.muxin.gateway.core.filter.FilterDefinition;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.tokenizeToStringArray;

/**
 * 定义了一个路由定义类，用于存储路由的详细信息，包括ID、谓词、过滤器、URI、元数据和顺序。
 * 该类提供了从字符串解析路由定义的功能，并支持通过这些属性进行路由配置。
 *
 * @author Administrator
 * @date 2024/11/21 10:55
 */
@Data
@EqualsAndHashCode
public class RouteDefinition {

    /**
     * 路由的唯一标识符。
     */
    private String id;

    /**
     * 路由的谓词定义列表。
     */
    private List<PredicateDefinition> predicates = new ArrayList<>();

    /**
     * 路由的过滤器定义列表。
     */
    private List<FilterDefinition> filters = new ArrayList<>();

    /**
     * 路由的目标URI。
     */
    private URI uri;

    /**
     * 路由的元数据。
     */
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 路由的顺序，用于排序。
     */
    private int order = 0;

    /**
     * 无参构造函数。
     */
    public RouteDefinition() {
    }

    /**
     * 从字符串构造路由定义。
     * 字符串格式为 "id=uri,predicate1,predicate2"，其中 uri 是目标URI，predicate1 和 predicate2 是谓词定义。
     *
     * @param text 路由定义的字符串
     * @throws BeanDefinitionValidationException 如果字符串格式不正确
     */
    public RouteDefinition(String text) {
        int eqIdx = text.indexOf('=');
        if (eqIdx <= 0) {
            throw new BeanDefinitionValidationException(
                    "Unable to parse RouteDefinition text '" + text + "'" + ", must be of the form name=value");
        }

        setId(text.substring(0, eqIdx));

        String[] args = tokenizeToStringArray(text.substring(eqIdx + 1), ",");

        setUri(URI.create(args[0]));

        for (int i = 1; i < args.length; i++) {
            this.predicates.add(new PredicateDefinition(args[i]));
        }
    }

}
