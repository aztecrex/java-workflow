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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

final class TaskHandler implements InvocationHandler {

    private final InitiatesWork initiator;

    TaskHandler(final InitiatesWork p) {
        this.initiator = p;
    }

    @Override
    public Object invoke(final Object proxy, final Method method,
            final Object[] args) throws Throwable {

        if (Object.class == method.getDeclaringClass()) {
            final String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@"
                        + Integer.toHexString(System.identityHashCode(proxy))
                        + ", with InvocationHandler " + this;
            } else {
                throw new UnsupportedOperationException(
                        "Object method not handled: " + method);
            }
        } else {

            final Task spec = method.getAnnotation(Task.class);
            if (spec != null) {
                return this.initiator.startTask(spec.name(), (String) args[0]);
            } else {
                throw new UnsupportedOperationException("Not marked as task: "
                        + method);
            }

        }

    }

}
