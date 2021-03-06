/*
 * Copyright 2016 WSO2 Inc. (http://wso2.org)
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
package org.wso2.carbon.metrics.core.config.model;

import org.wso2.carbon.metrics.core.Level;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configurations for each metric level
 */
public class MetricsLevelConfig {

    /**
     * The root level configured for Metrics collection
     */
    private Level rootLevel = Level.OFF;

    private final Map<String, Level> levelMap = Collections.synchronizedMap(new HashMap<String, Level>());

    public MetricsLevelConfig() {
    }

    public Level getRootLevel() {
        return rootLevel;
    }

    public void setRootLevel(Level rootLevel) {
        this.rootLevel = rootLevel;
    }

    public Level getLevel(String metricName) {
        return levelMap.get(metricName);
    }

    public void setLevel(String metricName, Level level) {
        levelMap.put(metricName, level);
    }
}
