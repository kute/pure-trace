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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 持有所有异步初始化的 bean 以及 method
 */
public class AsyncInitBeanHolder {
    private static final ConcurrentMap<String, String> asyncBeanInfos = new ConcurrentHashMap<>();

    public static void registerAsyncInitBean(String beanId, String methodName) {
        if (beanId == null || methodName == null) {
            return;
        }
        asyncBeanInfos.put(beanId, methodName);
    }

    public static String getAsyncInitMethodName(String beanId) {
        return null == beanId ? null : asyncBeanInfos.get(beanId);
    }
}
