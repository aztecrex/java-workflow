/**
 * Licensed to Media Science International (MSI) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. MSI
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.msiops.garage.workflow;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.msiops.ground.promise.FunctionX;
import com.msiops.ground.promise.Promise;

public final class PerformsTasks {

    public static <T> T proxyTasks(final Class<T> ifc,
            final Map<String, FunctionX<String, Promise<String>>> taskMap) {

        final PerformsTasks performer = new PerformsTasks(taskMap);

        final Object rval = Proxy.newProxyInstance(ifc.getClassLoader(),
                new Class<?>[] { ifc }, new TaskHandler(performer));
        return ifc.cast(rval);
    }

    private final Map<String, FunctionX<String, Promise<String>>> taskMap;

    public PerformsTasks(
            final Map<String, FunctionX<String, Promise<String>>> taskMap) {

        this.taskMap = Collections.unmodifiableMap(new HashMap<>(taskMap));

    }

    public Promise<String> performTask(final String name, final String arg) {

        final Promise<String> root = Promise.of(arg);
        /*
         * again with the eclipse bug!
         */
        return root.flatMap(v -> this.taskMap.get(name).apply(v));

    }

}
