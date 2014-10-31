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
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class TaskSimulator {

    private static Random RNG = new Random();

    private final BiConsumer<Long, String> capture;

    private final long delay = RNG.nextInt(200) + 100;

    private final long id;

    private final String result;

    public TaskSimulator(final long id, final Function<String, String> f,
            final String arg, final BiConsumer<Long, String> capture) {

        this.result = Objects.requireNonNull(f.apply(arg));
        this.capture = Objects.requireNonNull(capture);
        this.id = id;
    }

    public void runOn(final ExecutorService exec) {

        exec.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(TaskSimulator.this.delay);
                    TaskSimulator.this.capture.accept(TaskSimulator.this.id,
                            TaskSimulator.this.result);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException();
                }
            }
        });

    }

}
