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
package pgmr.com.msiops.garage.workflow;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import com.msiops.garage.workflow.Job;
import com.msiops.garage.workflow.TaskDispatcher;
import com.msiops.garage.workflow.Workflows;
import com.msiops.garage.workflow.eventsourced.History;
import com.msiops.garage.workflow.eventsourced.Runner;

public class EventSourcedConceptTest {

    private History<String> history;

    private WorkSimulator worker;

    @Before
    public void setup() {

        this.history = new History<>();

        final HashMap<String, Function<String, String>> workspec = new HashMap<>();
        workspec.put("ECHO", Compute::echo);
        workspec.put("REVERSE", Compute::reverse);
        workspec.put("APPEND", Compute.makeAppend(" World"));

        this.worker = new WorkSimulator(workspec, this.history::complete);

    }

    @Test
    public void testDependentTasks() {

        final AtomicReference<Object> cap1 = new AtomicReference<>();
        final AtomicReference<Object> cap2 = new AtomicReference<>();
        final AtomicReference<Object> cap3 = new AtomicReference<>();

        runToComplete(new Job<String>() {
            @Override
            public void start(final TaskDispatcher<String> dispatcher) {
                dispatcher.dispatch("ECHO", "Hello")
                        .flatMap(v -> dispatcher.dispatch("REVERSE", v))
                        .flatMap(v -> dispatcher.dispatch("REVERSE", v))
                        .flatMap(v -> dispatcher.dispatch("ECHO", v))
                        .forEach(cap1::set);
                dispatcher.dispatch("ECHO", "Hello")
                        .flatMap(v -> dispatcher.dispatch("ECHO", v))
                        .flatMap(v -> dispatcher.dispatch("ECHO", v))
                        .forEach(cap2::set);
                dispatcher.dispatch("APPEND", "Hello")
                        .flatMap(v -> dispatcher.dispatch("REVERSE", v))
                        .flatMap(v -> dispatcher.dispatch("REVERSE", v))
                        .forEach(cap3::set);
            }
        });

        assertEquals("Hello", cap1.get());
        assertEquals("Hello", cap2.get());
        assertEquals("Hello World", cap3.get());

    }

    @Test
    public void testOneTask() {

        final AtomicReference<Object> cap = new AtomicReference<>();

        runToComplete(new Job<String>() {
            @Override
            public void start(final TaskDispatcher<String> dispatcher) {
                dispatcher.dispatch("ECHO", "Hello").forEach(cap::set);
            }
        });

        assertEquals("Hello", cap.get());

    }

    @Test
    public void testSugarred() {
        final AtomicReference<Object> cap1 = new AtomicReference<>();
        final AtomicReference<Object> cap2 = new AtomicReference<>();
        final AtomicReference<Object> cap3 = new AtomicReference<>();

        runToComplete(new Job<String>() {
            @Override
            public void start(final TaskDispatcher<String> dispatcher) {

                final StringTasks sugar = Workflows.createProxy(
                        StringTasks.class, String.class, dispatcher);

                sugar.echo("Hello").flatMap(sugar::reverse)
                        .flatMap(sugar::reverse).flatMap(sugar::echo)
                        .forEach(cap1::set);
                sugar.echo("Hello").flatMap(sugar::echo).flatMap(sugar::echo)
                        .forEach(cap2::set);

                /*
                 * mixed dispatcher + sugar. woohoo!
                 */
                dispatcher.dispatch("APPEND", "Hello").flatMap(sugar::reverse)
                        .flatMap(sugar::reverse).forEach(cap3::set);
            }
        });

        assertEquals("Hello", cap1.get());
        assertEquals("Hello", cap2.get());
        assertEquals("Hello World", cap3.get());
    }

    @Test
    public void testTwoTasks() {

        final AtomicReference<Object> cap1 = new AtomicReference<>();
        final AtomicReference<Object> cap2 = new AtomicReference<>();

        runToComplete(new Job<String>() {
            @Override
            public void start(final TaskDispatcher<String> dispatcher) {
                dispatcher.dispatch("ECHO", "Hello").forEach(cap1::set);
                dispatcher.dispatch("REVERSE", "Hello").forEach(cap2::set);
            }
        });

        assertEquals("Hello", cap1.get());
        assertEquals("olleH", cap2.get());

    }

    private void runToComplete(final Job<String> job) {
        final Runner<String> init = new Runner<>(job, this.worker, this.history);
        boolean more = init.go();
        while (more) {
            this.history.poll();
            final Runner<String> r = new Runner<>(job, this.worker,
                    this.history);
            more = r.go();
        }
    }

}
