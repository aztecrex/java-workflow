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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import com.msiops.garage.workflow.eventsourced.Completion;
import com.msiops.garage.workflow.eventsourced.DifferentEventSourcedThing;

/**
 * Transient tests to help formulate the initial specification and API.
 */
public class DifferentEventSourcedConceptTest {

    private DifferentEventSourcedThing doer;

    @Before
    public void setup() {

        final HashMap<String, Function<String, String>> workers = new HashMap<>();
        workers.put("ECHO", Compute::echo);
        workers.put("REVERSE", Compute::reverse);

        this.doer = new DifferentEventSourcedThing(workers);

    }

    @Test
    public void testDependentTasks() throws InterruptedException {

        final AtomicReference<Object> cap = new AtomicReference<>();

        this.doer.reset();
        this.doer.performTask("REVERSE", "Hello")
                .flatMap(v -> this.doer.performTask("REVERSE", v))
                .forEach(cap::set);
        final List<Completion> results1 = this.doer.awaitResults();
        System.out.println(results1);
        this.doer.catchup(results1);

        this.doer.reset();
        this.doer.performTask("REVERSE", "Hello")
                .flatMap(v -> this.doer.performTask("REVERSE", v))
                .forEach(cap::set);
        final List<Completion> results2 = this.doer.awaitResults();
        System.out.println(results2);
        this.doer.catchup(results2);

        assertEquals("Hello", cap.get());
    }

    @Test
    public void testSingleTask() throws InterruptedException {

        final AtomicReference<Object> cap = new AtomicReference<>();

        this.doer.performTask("ECHO", "Hello").forEach(cap::set);

        final List<Completion> results = this.doer.awaitResults();
        assertNull(cap.get());

        this.doer.catchup(results);

        assertEquals("Hello", cap.get());

    }

    @Test
    public void testTwoTasks() throws InterruptedException {

        final AtomicReference<Object> cap1 = new AtomicReference<>();
        final AtomicReference<Object> cap2 = new AtomicReference<>();

        this.doer.reset();
        this.doer.performTask("ECHO", "Hello").forEach(cap1::set);
        this.doer.performTask("REVERSE", "Hello").forEach(cap2::set);
        final List<Completion> results1 = this.doer.awaitResults();
        this.doer.catchup(results1);

        this.doer.reset();
        this.doer.performTask("ECHO", "Hello").forEach(cap1::set);
        this.doer.performTask("REVERSE", "Hello").forEach(cap2::set);
        final List<Completion> results2 = this.doer.awaitResults();
        this.doer.catchup(results2);

        assertEquals("Hello", cap1.get());
        assertEquals("olleH", cap2.get());

    }

}
