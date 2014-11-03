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

public final class Request<Z> extends Event<Z> {

    private final Z in;

    private final String taskName;

    public Request(final long timestamp, final String taskName, final Z in) {
        super(timestamp);
        this.taskName = Objects.requireNonNull(taskName);
        this.in = Objects.requireNonNull(in);
    }

    public Z getIn() {
        return this.in;
    }

    public String getTaskName() {
        return this.taskName;
    }

    @Override
    public String toString() {
        return new StringBuilder("req:").append(this.getRequestId())
                .append(',').append(this.in).toString();
    }

}
