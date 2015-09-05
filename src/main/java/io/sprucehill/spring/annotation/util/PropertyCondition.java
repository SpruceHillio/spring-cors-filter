package io.sprucehill.spring.annotation.util;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * A custom Condition implementation for the @ConditionalOnProperty annotation
 *
 * @author Michael Duergner <michael@sprucehill.io>
 */
public class PropertyCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(PropertyCondition.class.getName());
        Boolean exists = (Boolean) attributes.get("exists");
        String havingValue = (String) attributes.get("havingValue");
        String property = (String) attributes.get("value");

        String propertyValue = context.getEnvironment().getProperty(property);

        if (null != havingValue && !havingValue.isEmpty()) {
            return null != propertyValue && propertyValue.equals(havingValue);
        } else {
            return exists && null != propertyValue || !exists && null == propertyValue;
        }
    }
}
