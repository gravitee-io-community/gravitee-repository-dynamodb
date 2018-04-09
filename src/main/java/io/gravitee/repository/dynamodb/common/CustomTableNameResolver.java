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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
public class CustomTableNameResolver extends DynamoDBMapperConfig.DefaultTableNameResolver {

    private static final String DEFAULT_PREFIX = "GraviteeioApim";
    private String customPrefix;

    public CustomTableNameResolver(String customPrefix) {
        super();
        this.customPrefix = customPrefix;
    }

    @Override
    public String getTableName(Class<?> clazz, DynamoDBMapperConfig config) {
        return getPrefix() + super.getTableName(clazz, config);
    }

    private String getPrefix() {
        if(customPrefix == null || customPrefix.trim().isEmpty()) {
            return DEFAULT_PREFIX;
        }
        return customPrefix;
    }
}
