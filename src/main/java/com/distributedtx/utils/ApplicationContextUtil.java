package com.distributedtx.utils;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * 上下文工具
 */
public class ApplicationContextUtil {
    public static ApplicationContext applicationContext;  //上下文
    public static String dtxTransactionalScanPath; //分布式注解扫描包

    /**
     * 主动向Spring容器中注册bean
     *
     * @param force              强迫注册，覆盖
     * @param beanName               BeanName
     * @param clazz              注册的bean的类性
     * @param args               构造方法的必要参数，顺序和类型要求和clazz中定义的一致
     */
    public static <T> T registerBean(boolean force, String beanName, Class<T> clazz, Object... args) {
        if(force){
            removeBean(beanName);
        }else if(applicationContext.containsBean(beanName)) {
            Object bean = applicationContext.getBean(beanName);
            if (bean.getClass().isAssignableFrom(clazz)) { //同一个class
                return (T)bean;
            } else {
                throw new RuntimeException("BeanName 重复 " + beanName);
            }
        }

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        beanDefinitionBuilder.addPropertyValue("providerTaskMapper",applicationContext.getBean("providerTaskMapper"));
        for (Object arg : args) {
            beanDefinitionBuilder.addConstructorArgValue(arg);
        }

        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory)applicationContext.getAutowireCapableBeanFactory();
        defaultListableBeanFactory.registerBeanDefinition(beanName,beanDefinitionBuilder.getBeanDefinition());

        return applicationContext.getBean(beanName, clazz);
    }

    public static void removeBean(String beanName){
        if(applicationContext.containsBean(beanName)) {
            DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory)applicationContext.getAutowireCapableBeanFactory();
            defaultListableBeanFactory.removeBeanDefinition(beanName);
        }
    }
}
