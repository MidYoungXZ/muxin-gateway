package com.muxin.gateway.core.route.predicate;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.util.StringUtils.tokenizeToStringArray;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/21 10:53
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PredicateDefinition {

    private String name;

    private Map<String, String> args = new LinkedHashMap<>();

    public PredicateDefinition() {
    }

    public PredicateDefinition(String text) {
        int eqIdx = text.indexOf('=');
        if (eqIdx <= 0) {
            throw new BeanDefinitionValidationException(
                    "Unable to parse PredicateDefinition text '" + text + "'" + ", must be of the form name=value");
        }
        setName(text.substring(0, eqIdx));

        String[] args = tokenizeToStringArray(text.substring(eqIdx + 1), ",");

        for (String arg : args) {
            this.args.put(UUID.randomUUID().toString(), arg);
        }
    }
}