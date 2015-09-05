package io.sprucehill.spring.annotation;

import io.sprucehill.spring.annotation.util.PropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A specific @Conditional implementation that will match based upon property configuration
 *
 * @author Michael Duergner <michael@sprucehill.io>
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Conditional(PropertyCondition.class)
public @interface ConditionalOnProperty {

    /**
     * The property to look for
     *
     * @return  A String; the property name to work upon
     */
    public String value();

    /**
     * Whether the property should exist or not to fulfill the condition
     *
     * @return  A boolean value; defaults to true
     */
    public boolean exists() default true;

    /**
     * A specific value the property needs to have; has precedence over 'exists' if set to a non empty value
     *
     * @return  A String being the required property value
     */
    public String havingValue() default "";

}
