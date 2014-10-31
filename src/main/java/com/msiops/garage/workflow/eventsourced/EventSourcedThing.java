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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import com.msiops.garage.workflow.DoesWork;
import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;

public class EventSourcedThing implements DoesWork {

    private final Map<Long, Async<String>> pending = new HashMap<>();

    private final Consumer<Request> sink;

    private final AtomicLong time = new AtomicLong();

    public EventSourcedThing(final Consumer<Request> sink) {

        this.sink = Objects.requireNonNull(sink);

    }

    public void catchup(final List<Completion> history) {

        history.forEach(ev -> {
            this.pending.remove(ev.getRequestTimestamp()).succeed(ev.getOut());
        });

    }

    @Override
    public Promise<String> performTask(final String name, final String arg) {

        final Request req = new Request(this.time.incrementAndGet(),
                Objects.requireNonNull(name), Objects.requireNonNull(arg));
        this.sink.accept(req);
        final Async<String> async = new Async<>();
        this.pending.put(req.getTimestamp(), async);
        return async.promise();

    }

}
