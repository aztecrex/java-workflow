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
package com.msiops.garage.workflow.eventsourced;

import java.util.Objects;

public final class Completion<Z> extends Event<Z> {

    private final Z out;

    public Completion(final long requestId, final Z out) {
        super(requestId);
        this.out = Objects.requireNonNull(out);
    }

    public Z getOut() {
        return this.out;
    }

    @Override
    public String toString() {
        return new StringBuilder("cplt:").append(this.getRequestId())
                .append(',').append(this.out).toString();
    }

}
