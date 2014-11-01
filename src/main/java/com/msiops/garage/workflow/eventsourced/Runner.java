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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.msiops.garage.workflow.DoesWork;
import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;

public final class Runner {

    private final History history;

    private final Job job;

    private final ConcurrentMap<Long, Async<String>> pending = new ConcurrentHashMap<>();

    private final AtomicLong time = new AtomicLong();

    private final AtomicBoolean used = new AtomicBoolean();

    private final WorkSimulator worker;

    public Runner(final Job job, final WorkSimulator worker,
            final History history) {
        this.job = Objects.requireNonNull(job);
        this.history = Objects.requireNonNull(history);
        this.worker = Objects.requireNonNull(worker);

    }

    public boolean go() {

        if (!this.used.compareAndSet(false, true)) {
            throw new IllegalStateException("already run");
        }
        this.job.start(new Doer());
        this.history.playback().forEach(
                ev -> {
                    if (ev instanceof Request) {
                        if (!this.pending.containsKey(ev.getTimestamp())) {
                            this.pending.put(ev.getTimestamp(), new Async<>());
                        }
                    } else if (ev instanceof Completion) {
                        this.pending.remove(
                                ((Completion) ev).getRequestTimestamp())
                                .succeed(((Completion) ev).getOut());
                    }
                });

        return !this.pending.isEmpty();

    }

    private final class Doer implements DoesWork {

        @Override
        public Promise<String> performTask(final String name, final String arg) {

            final long ts = Runner.this.time.incrementAndGet();
            final Async<String> p = new Async<>();
            Runner.this.pending.put(ts, p);
            Runner.this.history.request(ts, name, arg).ifPresent(
                    req -> {
                        Runner.this.worker.submit(req.getTimestamp(),
                                req.getTaskName(), req.getIn());
                    });
            return p.promise();

        }
    }
}
