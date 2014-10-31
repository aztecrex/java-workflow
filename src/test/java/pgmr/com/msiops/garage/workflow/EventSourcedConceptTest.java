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
import com.msiops.garage.workflow.eventsourced.EventSourcedThing;
import com.msiops.garage.workflow.eventsourced.RemoteDoer;

/**
 * Transient tests to help formulate the initial specification and API.
 */
public class EventSourcedConceptTest {

    private EventSourcedThing doer;

    private RemoteDoer remdoer;

    @Before
    public void setup() {

        final HashMap<String, Function<String, String>> workers = new HashMap<>();
        workers.put("ECHO", Compute::echo);
        workers.put("REVERSE", Compute::reverse);

        this.remdoer = new RemoteDoer(workers);

        this.doer = new EventSourcedThing(this.remdoer::accept);

    }

    @Test
    public void testDependentRequests() {

        final AtomicReference<Object> cap = new AtomicReference<>();

        this.doer.performTask("REVERSE", "Hello")
                .flatMap(v -> this.doer.performTask("REVERSE", v))
                .forEach(cap::set);

        assertNull(cap.get());

        final List<Completion> results1 = this.remdoer.drain();
        this.doer.catchup(results1);

        assertNull(cap.get());

        final List<Completion> results2 = this.remdoer.drain();
        this.doer.catchup(results2);

        assertEquals("Hello", cap.get());

        final List<Completion> results3 = this.remdoer.drain();
        assertTrue(results3.isEmpty());

    }

    @Test
    public void testParallelChains() {

        final AtomicReference<Object> cap1 = new AtomicReference<>();
        final AtomicReference<Object> cap2 = new AtomicReference<>();

        this.doer.performTask("REVERSE", "Hello")
                .flatMap(v -> this.doer.performTask("REVERSE", v))
                .forEach(cap1::set);

        this.doer.performTask("ECHO", "Hello")
                .flatMap(v -> this.doer.performTask("ECHO", v))
                .forEach(cap2::set);

        assertNull(cap1.get());
        assertNull(cap2.get());

        final List<Completion> results1 = this.remdoer.drain();
        this.doer.catchup(results1);

        assertNull(cap1.get());
        assertNull(cap2.get());

        final List<Completion> results2 = this.remdoer.drain();
        this.doer.catchup(results2);

        assertEquals("Hello", cap1.get());
        assertEquals("Hello", cap2.get());

        final List<Completion> results3 = this.remdoer.drain();
        assertTrue(results3.isEmpty());
    }

    @Test
    public void testRunToEnd() {

        final AtomicReference<Object> cap1 = new AtomicReference<>();
        final AtomicReference<Object> cap2 = new AtomicReference<>();

        this.doer.performTask("REVERSE", "Hello")
                .flatMap(v -> this.doer.performTask("REVERSE", v))
                .flatMap(v -> this.doer.performTask("REVERSE", v))
                .flatMap(v -> this.doer.performTask("REVERSE", v))
                .forEach(cap1::set);

        this.doer.performTask("ECHO", "Hello")
                .flatMap(v -> this.doer.performTask("ECHO", v))
                .forEach(cap2::set);

        boolean done = false;
        do {

            final List<Completion> results = this.remdoer.drain();
            if (results.isEmpty()) {
                done = true;
            } else {
                this.doer.catchup(results);
            }

        } while (!done);

        assertEquals("Hello", cap1.get());
        assertEquals("Hello", cap2.get());
    }

    @Test
    public void testSimpleEventSourced() {

        final AtomicReference<Object> cap = new AtomicReference<>();

        this.doer.performTask("ECHO", "Hello").forEach(cap::set);

        final List<Completion> results = this.remdoer.drain();

        assertNull(cap.get());

        this.doer.catchup(results);

        assertEquals("Hello", cap.get());

    }

    @Test
    public void testTwoRequests() {

        final AtomicReference<Object> cap1 = new AtomicReference<>();
        final AtomicReference<Object> cap2 = new AtomicReference<>();

        this.doer.performTask("ECHO", "Hello").forEach(cap1::set);
        this.doer.performTask("REVERSE", "Hello").forEach(cap2::set);

        final List<Completion> results = this.remdoer.drain();

        assertNull(cap1.get());
        assertNull(cap2.get());

        this.doer.catchup(results);

        assertEquals("Hello", cap1.get());
        assertEquals("olleH", cap2.get());
    }

}
