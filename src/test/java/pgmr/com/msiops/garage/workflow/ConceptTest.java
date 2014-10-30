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

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;

import com.msiops.garage.workflow.PerformsTasks;
import com.msiops.ground.promise.Promise;

/**
 * Transient tests to help formulate the initial specification and API.
 */
public class ConceptTest {

    private PerformsTasks performer;

    @Test
    public void requestTaskToBePerformed() {

        final AtomicReference<Object> cap = new AtomicReference<>();

        final Promise<String> task = this.performer
                .performTask("ECHO", "Hello");

        task.forEach(cap::set);

        assertEquals("Hello", cap.get());

    }

    @Before
    public void setup() {
        this.performer = new PerformsTasks();
    }

}
