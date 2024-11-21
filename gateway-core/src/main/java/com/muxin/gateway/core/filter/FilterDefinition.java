package com.muxin.gateway.core.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.util.StringUtils.tokenizeToStringArray;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/21 10:51
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class FilterDefinition {

    private String name;

    private Map<String, String> args = new LinkedHashMap<>();

    public FilterDefinition() {
    }

    public FilterDefinition(String text) {
        int eqIdx = text.indexOf('=');
        if (eqIdx <= 0) {
            setName(text);
            return;
        }
        setName(text.substring(0, eqIdx));

        String[] args = tokenizeToStringArray(text.substring(eqIdx + 1), ",");
        for (String arg : args) {
            this.args.put(UUID.randomUUID().toString(), arg);
        }
    }

}
