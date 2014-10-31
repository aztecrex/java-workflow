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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import com.msiops.garage.workflow.DoesWork;
import com.msiops.ground.promise.Async;
import com.msiops.ground.promise.Promise;

public class DifferentEventSourcedThing implements DoesWork {

    private final ExecutorService exec = Executors.newCachedThreadPool();

    boolean haveResults = false;

    private final Map<Long, Async<String>> pending = new HashMap<>();

    private final List<Completion> results = new ArrayList<>();

    private long time = 0, seenTime = 0;

    private final Map<String, Function<String, String>> workmap;

    public DifferentEventSourcedThing(
            final Map<String, Function<String, String>> workmap) {
        this.workmap = new HashMap<>(workmap);
    }

    /**
     * Blocks, simulating a long poll on the event source.
     *
     * @throws InterruptedException
     */
    public List<Completion> awaitResults() throws InterruptedException {

        synchronized (this.results) {
            while (!this.haveResults) {
                this.results.wait();
            }
            this.haveResults = false;
            return new ArrayList<>(this.results);
        }

    }

    public void catchup(final List<Completion> history) {

        history.forEach(ev -> {
            final Async<String> p;
            synchronized (this.results) {
                p = this.pending.remove(ev.getRequestTimestamp());
            }
            p.succeed(ev.getOut());
        });

    }

    private void onResult(final long requestId, final String result) {

        synchronized (this.results) {
            this.results.add(new Completion(0L, requestId, result));
            this.haveResults = true;
            this.results.notifyAll();
        }

    }

    @Override
    public Promise<String> performTask(final String name, final String arg) {

        final Async<String> async = new Async<>();
        final Optional<TaskSimulator> mtsim;
        synchronized (this.results) {
            this.time += 1;
            this.pending.put(this.time, async);
            if (this.time > this.seenTime) {
                this.seenTime = this.time;
                mtsim = Optional.of(new TaskSimulator(this.time, this.workmap
                        .get(name), arg, this::onResult));
            } else {
                mtsim = Optional.empty();
            }
        }
        mtsim.ifPresent(sim -> sim.runOn(this.exec));
        return async.promise();

    }

    public void reset() {
        synchronized (this.results) {
            this.pending.clear();
            this.time = 0;
        }
    }

}
