/*
 * Copyright 2017 The Embulk project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.embulk.base.restclient;

import org.embulk.config.ConfigSource;
import org.embulk.config.TaskSource;

public abstract class RestClientPluginBase<T extends RestClientTaskBase> {
    protected final T loadConfig(final ConfigSource config, final Class<T> taskClass) {
        return config.loadConfig(taskClass);
    }

    protected final T loadTask(final TaskSource taskSource, final Class<T> taskClass) {
        return taskSource.loadTask(taskClass);
    }
}
