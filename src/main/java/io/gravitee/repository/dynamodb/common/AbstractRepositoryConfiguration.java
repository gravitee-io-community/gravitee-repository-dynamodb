/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.repository.dynamodb.common;

import io.gravitee.repository.Scope;
import io.gravitee.repository.dynamodb.common.transaction.NoTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

/**
 * Common configuration for creating Cassandra Driver cluster and session with the provided options.
 *
 * @author Azize ELAMRANI (azize.elamrani at graviteesource.com)
 * @author Adel Abdelhak (adel.abdelhak@leansys.fr)
 */
public abstract class AbstractRepositoryConfiguration {
    private final Logger LOGGER = LoggerFactory.getLogger(AbstractRepositoryConfiguration.class);

    @Autowired
    private Environment environment;

    private String scope;

    protected abstract Scope getScope();

    public AbstractRepositoryConfiguration() {
        this.scope = getScope().getName();
    }


    /**
     * The repository does not use transactional workflow
     * @return transaction manager that does nothing
     */
    @Bean
    public AbstractPlatformTransactionManager graviteeTransactionManager() {
        return new NoTransactionManager();
    }
}
