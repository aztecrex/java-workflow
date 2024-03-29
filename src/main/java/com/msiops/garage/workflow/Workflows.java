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

public interface Workflows {

    public static <I, Z> I createProxy(final Class<I> ifc,
            final Class<Z> argClazz, final TaskDispatcher<Z> dispatcher) {

        final Object rval = Proxy
                .newProxyInstance(ifc.getClassLoader(), new Class<?>[] { ifc },
                        new TaskHandler<>(argClazz, dispatcher));
        return ifc.cast(rval);

    }

}
