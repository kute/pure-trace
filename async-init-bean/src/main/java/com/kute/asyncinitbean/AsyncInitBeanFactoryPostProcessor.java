/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kute.asyncinitbean;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 扫描所有 带@AsyncInitBean注解的 bean，解析所有 bean 的 init 方法
 */
@Slf4j
public class AsyncInitBeanFactoryPostProcessor implements BeanFactoryPostProcessor{

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 在 beanfactory 标准初始化完成后，所有的 bean 已加载但是还未初始化 进行调用
        log.info("AsyncInitBeanFactoryPostProcessor postProcessBeanFactory 1");
        Arrays.stream(beanFactory.getBeanDefinitionNames())
                .collect(Collectors.toMap(Function.identity(), beanFactory::getBeanDefinition))
                .forEach((key, value) -> scanAsyncInitBeanDefinition(key, value, beanFactory));
        log.info("AsyncInitBeanFactoryPostProcessor postProcessBeanFactory 2");
    }

    /**
     * {@link ScannedGenericBeanDefinition}
     * {@link AnnotatedGenericBeanDefinition}
     * {@link GenericBeanDefinition}
     * {@link org.springframework.beans.factory.support.ChildBeanDefinition}
     * {@link org.springframework.beans.factory.support.RootBeanDefinition}
     */
    private void scanAsyncInitBeanDefinition(String beanId, BeanDefinition beanDefinition,
                                             ConfigurableListableBeanFactory beanFactory) {
        if (BeanDefinitionUtil.isFromConfigurationSource(beanDefinition)) {
            if (beanId.endsWith("Bean")) {
                log.info("scanAsyncInitBeanDefinition 1 for bean={}", beanId);
            }
            scanAsyncInitBeanDefinitionOnMethod(beanId, (AnnotatedBeanDefinition) beanDefinition);
        } else {
            Class<?> beanClassType = BeanDefinitionUtil.resolveBeanClassType(beanDefinition);
            if (beanId.endsWith("Bean")) {
                log.info("scanAsyncInitBeanDefinition 2 for bean={}", beanId);
            }
            if (beanClassType == null) {
                log.warn("Bean class type cant be resolved from bean of {}", beanId);
                return;
            }
            scanAsyncInitBeanDefinitionOnClass(beanId, beanClassType, beanDefinition, beanFactory);
        }
    }

    private void scanAsyncInitBeanDefinitionOnMethod(String beanId,
                                                     AnnotatedBeanDefinition beanDefinition) {
        Class<?> returnType;
        Class<?> declaringClass; // 也就是当前 @Configuration 的类
        List<Method> candidateMethods = new ArrayList<>();

        // 返回创建当前 bean 的工厂方法，如果是通过 @Bean 注解定义的，这个 方法元数据 就是@Bean 注解所在的这个方法
        MethodMetadata methodMetadata = beanDefinition.getFactoryMethodMetadata();
        try {
            returnType = ClassUtils.forName(methodMetadata.getReturnTypeName(), null);
            declaringClass = ClassUtils.forName(methodMetadata.getDeclaringClassName(), null);
        } catch (Throwable throwable) {
            // it's impossible to catch throwable here
            log.error("", throwable);
            return;
        }
        if (methodMetadata instanceof StandardMethodMetadata) {
            candidateMethods.add(((StandardMethodMetadata) methodMetadata).getIntrospectedMethod());
        } else {
            for (Method m : declaringClass.getDeclaredMethods()) {
                // check methodName and return type
                if (!m.getName().equals(methodMetadata.getMethodName())
                        || !m.getReturnType().getTypeName().equals(methodMetadata.getReturnTypeName())) {
                    continue;
                }

                // check bean method
                if (!AnnotatedElementUtils.hasAnnotation(m, Bean.class)) {
                    continue;
                }

                Bean bean = m.getAnnotation(Bean.class);
                Set<String> beanNames = new HashSet<>();
                beanNames.add(m.getName());
                if (bean != null) {
                    beanNames.addAll(Arrays.asList(bean.name()));
                    beanNames.addAll(Arrays.asList(bean.value()));
                }

                // check bean name
                if (!beanNames.contains(beanId)) {
                    continue;
                }

                candidateMethods.add(m);
            }
        }

        if (candidateMethods.size() == 1) {
            AsyncInitBean AsyncInitBeanAnnotation = candidateMethods.get(0).getAnnotation(
                    AsyncInitBean.class);
            if (AsyncInitBeanAnnotation == null) {
                // AsyncInitBean 注解在 @Bean定义的方法 或者  class 上
                AsyncInitBeanAnnotation = returnType.getAnnotation(AsyncInitBean.class);
            }
            registerAsyncInitBean(beanId, AsyncInitBeanAnnotation, beanDefinition);
        } else if (candidateMethods.size() > 1) {
            for (Method m : candidateMethods) {
                if (AnnotatedElementUtils.hasAnnotation(m, AsyncInitBean.class)
                        || AnnotatedElementUtils.hasAnnotation(returnType, AsyncInitBean.class)) {
                    throw new FatalBeanException(declaringClass.getCanonicalName());
                }
            }
        }
    }

    private void scanAsyncInitBeanDefinitionOnClass(String beanId, Class<?> beanClass,
                                                    BeanDefinition beanDefinition,
                                                    ConfigurableListableBeanFactory beanFactory) {
        AsyncInitBean AsyncInitBeanAnnotation = AnnotationUtils.findAnnotation(beanClass,
                AsyncInitBean.class);
        registerAsyncInitBean(beanId, AsyncInitBeanAnnotation, beanDefinition);
    }

    @SuppressWarnings("unchecked")
    private void registerAsyncInitBean(String beanId, AsyncInitBean AsyncInitBeanAnnotation,
                                       BeanDefinition beanDefinition) {
        if (AsyncInitBeanAnnotation == null) {
            return;
        }

        if (AsyncInitBeanAnnotation.value()) {
            log.info("registerAsyncInitBean 1 for bean={} ,method={}", beanId, beanDefinition.getInitMethodName());
            AsyncInitBeanHolder.registerAsyncInitBean(beanId,
                    beanDefinition.getInitMethodName());
        }

    }

}
