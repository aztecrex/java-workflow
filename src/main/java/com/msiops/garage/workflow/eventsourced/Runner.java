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

import com.msiops.garage.workflow.Job;
import com.msiops.garage.workflow.TaskDispatcher;
import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;

public final class Runner<Z> {

    private final History<Z> history;

    private final Job<Z> job;

    private final ConcurrentMap<Long, Async<Z>> pending = new ConcurrentHashMap<>();

    private final AtomicLong time = new AtomicLong();

    private final AtomicBoolean used = new AtomicBoolean();

    private final CorrelatedTaskDispatcher<? super Z> worker;

    public Runner(final Job<Z> job,
            final CorrelatedTaskDispatcher<? super Z> worker,
            final History<Z> history) {
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
                        if (!this.pending.containsKey(ev.getRequestId())) {
                            this.pending.put(ev.getRequestId(), new Async<>());
                        }
                    } else if (ev instanceof Completion) {
                        this.pending
                                .remove(((Completion<Z>) ev).getRequestId())
                                .succeed(((Completion<Z>) ev).getOut());
                    }
                });

        return !this.pending.isEmpty();

    }

    private final class Doer implements TaskDispatcher<Z> {

        @Override
        public Promise<Z> dispatch(final String name, final Z arg) {

            final long ts = Runner.this.time.incrementAndGet();
            final Async<Z> p = new Async<>();
            Runner.this.pending.put(ts, p);
            Runner.this.history.request(ts, name, arg).ifPresent(
                    req -> {
                        Runner.this.worker.dispatch(req.getRequestId(),
                                req.getTaskName(), req.getIn());
                    });
            return p.promise();

        }
    }
}
