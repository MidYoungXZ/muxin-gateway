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
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/21 10:55
 */
@Data
@EqualsAndHashCode
public class RouteDefinition {


    private String id;

    private List<PredicateDefinition> predicates = new ArrayList<>();


    private List<FilterDefinition> filters = new ArrayList<>();

    private URI uri;

    private Map<String, Object> metadata = new HashMap<>();

    private int order = 0;

    public RouteDefinition() {
    }

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
