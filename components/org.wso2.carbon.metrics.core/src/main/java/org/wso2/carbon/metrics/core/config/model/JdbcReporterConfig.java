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

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.internal.Utils;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;
import org.wso2.carbon.metrics.core.reporter.ReporterBuilder;
import org.wso2.carbon.metrics.core.reporter.impl.JdbcReporter;

import java.io.File;
import java.util.Optional;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Configuration for JDBC Reporter. Implements {@link ReporterBuilder} to construct a {@link JdbcReporter}
 */
public class JdbcReporterConfig extends ScheduledReporterConfig implements ReporterBuilder<JdbcReporter> {

    private static final Logger logger = LoggerFactory.getLogger(JdbcReporterConfig.class);

    private String source = Utils.getDefaultSource();

    private boolean lookupDataSource;

    private String dataSourceName;

    private JdbcScheduledCleanupConfig scheduledCleanup = new JdbcScheduledCleanupConfig();

    public JdbcReporterConfig() {
        name = "JDBC";
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isLookupDataSource() {
        return lookupDataSource;
    }

    public void setLookupDataSource(boolean lookupDataSource) {
        this.lookupDataSource = lookupDataSource;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public JdbcScheduledCleanupConfig getScheduledCleanup() {
        return scheduledCleanup;
    }

    public void setScheduledCleanup(JdbcScheduledCleanupConfig scheduledCleanup) {
        this.scheduledCleanup = scheduledCleanup;
    }

    /**
     * Build the JDBC Reporter
     *
     * @param metricRegistry The {@link MetricRegistry} for the reporter
     * @param metricFilter   The {@link MetricFilter} for the reporter
     * @return an {@link Optional} with {@link JdbcReporter}, if the reporter is built successfully, otherwise an empty
     * {@code Optional}
     * @throws ReporterBuildException when there was a failure in constructing the reporter
     */
    @Override
    public Optional<JdbcReporter> build(MetricRegistry metricRegistry, MetricFilter metricFilter)
            throws ReporterBuildException {
        if (!enabled) {
            return Optional.empty();
        }

        final DataSource dataSource;

        if (lookupDataSource) {
            if (dataSourceName == null || dataSourceName.trim().isEmpty()) {
                throw new ReporterBuildException("Data Source Name is not specified for JDBC Reporting.");
            }
            try {
                dataSource = InitialContext.doLookup(dataSourceName);
            } catch (NamingException e) {
                throw new ReporterBuildException(
                        String.format("Error when looking up the Data Source: '%s'.", dataSourceName), e);
            }
        } else {
            Optional<File> metricsLevelConfigFile = Utils.getConfigFile("metrics.datasource.conf",
                    "metrics-datasource.properties");
            if (!metricsLevelConfigFile.isPresent()) {
                throw new ReporterBuildException("Metrics Datasource configuration file not found!");
            }

            File file = metricsLevelConfigFile.get();
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Creating Metrics Datasource. Config file: %s", file.getAbsolutePath()));
            }
            HikariConfig hikariConfig = new HikariConfig(file.getPath());
            dataSource = new HikariDataSource(hikariConfig);
        }

        if (logger.isInfoEnabled()) {
            logger.info(String.format(
                    "Creating JDBC reporter for Metrics with source '%s', data source '%s'" +
                            " and %d seconds polling period",
                    source, dataSourceName, pollingPeriod));
        }

        return Optional.of(new JdbcReporter(name, metricRegistry, metricFilter, source, dataSource, pollingPeriod,
                scheduledCleanup.isEnabled(), scheduledCleanup.getDaysToKeep(),
                scheduledCleanup.getScheduledCleanupPeriod()));
    }

}
