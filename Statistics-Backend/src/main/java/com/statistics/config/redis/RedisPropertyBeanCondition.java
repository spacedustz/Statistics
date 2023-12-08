package com.statistics.config.redis;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

public class RedisPropertyBeanCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String redisEnable = context.getEnvironment().getProperty("spring.data.redis.enabled");

        if (StringUtils.hasText(redisEnable) &&
                (redisEnable.equalsIgnoreCase("Y") ||
                        redisEnable.equalsIgnoreCase("YES") ||
                        redisEnable.equalsIgnoreCase("TRUE") ||
                        redisEnable.equalsIgnoreCase("T"))) {
            return true;
        }

        return false;
    }
}
