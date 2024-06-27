package com.tom.chat.utils;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class SpringBeanUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    public static <T> T getBean(Class<T> beanClass, String qualifier) {
        Assert.notNull(applicationContext, "ApplicationContext is not set.");
        return applicationContext.getBean(beanClass, qualifier);
    }

    public static <T> T getBeanWithGenericType(Class<T> beanClass, String qualifier, Class<?>... genericTypes) {
        Assert.notNull(applicationContext, "ApplicationContext is not set.");
        return applicationContext.getBean(beanClass, genericTypes, qualifier);
    }

    public static <T> T getBeanWithResolvableType(ResolvableType resolvableType, String qualifier) {
        Assert.notNull(applicationContext, "ApplicationContext is not set.");

        if (qualifier != null && !qualifier.isEmpty()) {
            // 如果有限定符，使用限定符获取 bean
            return (T) applicationContext.getBean(qualifier, resolvableType.getRawClass());
        } else {
            // 如果没有限定符，直接获取 bean
            return (T) applicationContext.getBean(resolvableType.getRawClass());
        }
    }


}
