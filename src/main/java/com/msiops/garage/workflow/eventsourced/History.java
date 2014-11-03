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

public class History<Z> {

    private boolean haveMore = false;

    private long seen = 0;

    private final ArrayList<Event<Z>> store = new ArrayList<>();

    public Completion<Z> complete(final long requestId, final Z result) {

        final Completion<Z> rval = new Completion<>(requestId, result);
        synchronized (this.store) {
            this.store.add(rval);
            this.haveMore = true;
            this.store.notifyAll();
        }
        return rval;

    }

    public List<Event<Z>> playback() {

        final List<Event<Z>> rval;
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

    public Optional<Request<Z>> request(final long requestId,
            final String name, final Z arg) {

        synchronized (this.store) {

            if (requestId > this.seen + 1) {
                throw new IllegalStateException("time moves too quickly");
            }

            if (requestId > this.seen) {
                final Request<Z> rval = new Request<>(requestId, name, arg);
                this.seen = requestId;
                this.store.add(rval);
                return Optional.of(rval);
            }
        }

        return Optional.empty();

    }

}
