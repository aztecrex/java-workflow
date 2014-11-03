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
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class WorkSimulator implements AutoCloseable {

    private final Map<String, Function<String, String>> computations;

    private final BiConsumer<Long, String> dest;

    private final ExecutorService exec = Executors.newCachedThreadPool();

    private final Random RNG = new Random();

    public WorkSimulator(
            final Map<String, Function<String, String>> computations,
            final BiConsumer<Long, String> dest) {
        super();
        this.computations = new HashMap<>(computations);
        this.dest = Objects.requireNonNull(dest);
    }

    @Override
    public void close() throws Exception {
        this.exec.shutdown();
    }

    public void submit(final long id, final String name, final String arg) {

        this.exec.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(WorkSimulator.this.RNG.nextInt(100) + 10L);
                    WorkSimulator.this.dest.accept(id,
                            WorkSimulator.this.computations.get(name)
                                    .apply(arg));
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

}
