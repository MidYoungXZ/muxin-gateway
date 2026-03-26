package com.muxin.gateway.core.route.predicate;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BetweenPredicate implements Predicate {

    public static final String TYPE = "Between";

    private final ZonedDateTime datetime1;
    private final ZonedDateTime datetime2;
    private final Map<String, Object> config;

    public BetweenPredicate(ZonedDateTime datetime1, ZonedDateTime datetime2) {
        if (datetime1 == null || datetime2 == null) {
            throw new IllegalArgumentException("时间参数不能为空");
        }
        if (datetime1.isAfter(datetime2)) {
            this.datetime1 = datetime2;
            this.datetime2 = datetime1;
        } else {
            this.datetime1 = datetime1;
            this.datetime2 = datetime2;
        }
        this.config = new HashMap<>();
        this.config.put("datetime1", this.datetime1.toString());
        this.config.put("datetime2", this.datetime2.toString());
    }

    public BetweenPredicate(PredicateDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("PredicateDefinition不能为空");
        }
        
        this.datetime1 = parseDateTime(definition.getStringArg("datetime1"));
        this.datetime2 = parseDateTime(definition.getStringArg("datetime2"));
        this.config = definition.getArgs() != null ? definition.getArgs() : new HashMap<>();
        
        if (datetime1 == null || datetime2 == null) {
            throw new IllegalArgumentException("datetime1和datetime2参数不能为空");
        }
    }

    private ZonedDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            return ZonedDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e1) {
            try {
                return ZonedDateTime.parse(value + "T00:00:00+08:00", DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e2) {
                log.warn("[BetweenPredicate] 无法解析时间: {}", value);
                return null;
            }
        }
    }

    @Override
    public boolean test(HttpServerExchange exchange) {
        if (exchange == null) {
            log.warn("[BetweenPredicate] exchange为空");
            return false;
        }

        ZonedDateTime now;
        ZonedDateTime start = datetime1;
        ZonedDateTime end = datetime2;
        
        if (start.isAfter(end)) {
            ZonedDateTime temp = start;
            start = end;
            end = temp;
        }
        
        now = ZonedDateTime.now();
        
        boolean inRange = !now.isBefore(start) && !now.isAfter(end);
        
        if (log.isDebugEnabled()) {
            log.debug("[BetweenPredicate] 当前时间 {}, 时间范围 {} - {}, 匹配结果: {}", 
                     now, start, end, inRange);
        }
        
        return inRange;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "BetweenPredicate";
    }

    @Override
    public Map<String, Object> getConfig() {
        return config;
    }

    public ZonedDateTime getStart() {
        return datetime1;
    }

    public ZonedDateTime getEnd() {
        return datetime2;
    }

    public static class Factory implements PredicateFactory {

        @Override
        public Predicate createPredicate(PredicateDefinition definition) {
            return new BetweenPredicate(definition);
        }

        @Override
        public String getSupportedPredicateName() {
            return TYPE;
        }

        @Override
        public void validateConfig(PredicateDefinition definition) {
            String datetime1 = definition.getStringArg("datetime1");
            String datetime2 = definition.getStringArg("datetime2");
            if (datetime1 == null || datetime2 == null) {
                throw new IllegalArgumentException("BetweenPredicate 必须配置 datetime1 和 datetime2 参数");
            }
        }
    }
}