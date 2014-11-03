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
package com.msiops.garage.workflow;

/**
 * <p>
 * Job specification. A job specification defines the initial tasks to dispatch
 * and how to respond to task results.
 * </p>
 *
 * @param <Z>
 *            task data exchange type.
 */
public interface Job<Z> {

    /**
     * Start a run of the job.
     *
     * @param dispatcher
     */
    void start(TaskDispatcher<Z> dispatcher);

}
