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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class RemoteDoer {

    private final ArrayList<Completion> out = new ArrayList<>();

    private final AtomicLong time = new AtomicLong();

    private final Map<String, Function<String, String>> workmap;

    public RemoteDoer(final Map<String, Function<String, String>> workmap) {
        super();
        this.workmap = Objects.requireNonNull(workmap);
    }

    public void accept(final Request req) {

        final String result = this.workmap.get(req.getTaskName()).apply(
                req.getIn());
        final Completion cplt = new Completion(this.time.incrementAndGet(),
                req.getTimestamp(), result);
        this.out.add(cplt);

    }

    public List<Completion> drain() {

        final List<Completion> rval = new ArrayList<>(this.out);
        this.out.clear();
        return rval;

    }

}
