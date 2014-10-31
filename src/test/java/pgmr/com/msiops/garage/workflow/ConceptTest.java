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

import org.junit.Before;
import org.junit.Test;

import com.msiops.garage.workflow.DoesWork;
import com.msiops.garage.workflow.InitiatesWork;
import com.msiops.garage.workflow.TaskMapThing;
import com.msiops.garage.workflow.Workflows;
import com.msiops.ground.promise.FunctionX;
import com.msiops.ground.promise.Promise;

/**
 * Transient tests to help formulate the initial specification and API.
 */
public class ConceptTest {

    private InitiatesWork initiator;

    private StringTasks tasks;

    @Before
    public void setup() {

        final FunctionX<String, Promise<String>> pecho = v -> Promise
                .of(Compute.echo(v));
        final FunctionX<String, Promise<String>> preverse = v -> Promise
                .of(Compute.reverse(v));

        final HashMap<String, FunctionX<String, Promise<String>>> workers = new HashMap<>();
        workers.put("ECHO", pecho);
        workers.put("REVERSE", preverse);

        final DoesWork doer = new TaskMapThing(workers);

        this.initiator = new InitiatesWork(doer);

        this.tasks = Workflows.createProxy(StringTasks.class, doer);

    }

    @Test
    public void testChainTasksFromInterface() {

        final AtomicReference<Object> cap = new AtomicReference<>();

        this.tasks.echo("Hello").flatMap(this.tasks::reverse)
                .flatMap(this.tasks::reverse).flatMap(this.tasks::echo)
                .forEach(cap::set);

        assertEquals("Hello", cap.get());

    }

    @Test
    public void testDistinguishTask() {

        final AtomicReference<Object> cap = new AtomicReference<>();

        final Promise<String> task = this.initiator.startTask("REVERSE",
                "Hello");

        task.forEach(cap::set);

        assertEquals("olleH", cap.get());

    }

    @Test
    public void testRequestTaskToBePerformed() {

        final AtomicReference<Object> cap = new AtomicReference<>();

        final Promise<String> task = this.initiator.startTask("ECHO", "Hello");

        task.forEach(cap::set);

        assertEquals("Hello", cap.get());

    }

}
