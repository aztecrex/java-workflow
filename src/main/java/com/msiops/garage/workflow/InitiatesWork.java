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
import java.util.Map;
import java.util.Objects;

import com.msiops.ground.promise.FunctionX;
import com.msiops.ground.promise.Promise;

public final class InitiatesWork {

    public static <T> T proxyTasks(final Class<T> ifc,
            final Map<String, FunctionX<String, Promise<String>>> taskMap) {

        final DoesWork doer = new DoesWork(taskMap);
        final InitiatesWork initiator = new InitiatesWork(doer);

        final Object rval = Proxy.newProxyInstance(ifc.getClassLoader(),
                new Class<?>[] { ifc }, new TaskHandler(initiator));
        return ifc.cast(rval);
    }

    private final DoesWork doer;

    public InitiatesWork(final DoesWork doer) {

        this.doer = Objects.requireNonNull(doer);

    }

    public Promise<String> startTask(final String name, final String arg) {

        return this.doer.performTask(name, arg);

    }

}
