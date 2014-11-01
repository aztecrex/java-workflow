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
import java.util.Optional;

public class History {

    private boolean haveMore = false;

    private long seen = 0;

    private final ArrayList<Event> store = new ArrayList<>();

    public Completion complete(final long reqTime, final String result) {

        final Completion rval = new Completion(0L, reqTime, result);
        synchronized (this.store) {
            this.store.add(rval);
            this.haveMore = true;
            this.store.notifyAll();
        }
        return rval;

    }

    public List<Event> playback() {

        final List<Event> rval;
        synchronized (this.store) {
            rval = new ArrayList<>(this.store);
            this.haveMore = false;
        }
        return rval;

    }

    public void poll() {

        synchronized (this.store) {

            while (!this.haveMore) {
                try {
                    this.store.wait();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            this.haveMore = false;
        }
    }

    public Optional<Request> request(final long time, final String name,
            final String arg) {

        synchronized (this.store) {

            if (time > this.seen + 1) {
                throw new IllegalStateException("time moves too quickly");
            }

            if (time > this.seen) {
                final Request rval = new Request(time, name, arg);
                this.seen = time;
                this.store.add(rval);
                return Optional.of(rval);
            }
        }

        return Optional.empty();

    }

}
