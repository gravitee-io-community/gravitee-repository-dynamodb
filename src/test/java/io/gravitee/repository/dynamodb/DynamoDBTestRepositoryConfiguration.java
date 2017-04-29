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
package io.gravitee.repository.dynamodb;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import io.gravitee.repository.Scope;
import io.gravitee.repository.dynamodb.management.ManagementRepositoryConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@Configuration
@ComponentScan("io.gravitee.repository.dynamodb.management")
public class DynamoDBTestRepositoryConfiguration extends ManagementRepositoryConfiguration{

    private AmazonDynamoDB amazonDynamoDB;

    protected Scope getScope() {
        return Scope.MANAGEMENT;
    }

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        System.setProperty("sqlite4java.library.path", "native-libs");
        if(amazonDynamoDB == null){
            amazonDynamoDB =DynamoDBEmbedded.create().amazonDynamoDB();
        }
        return amazonDynamoDB;
    }

    @Bean
    public DynamoDBMapper dynamoDBMapper() {
        return new DynamoDBMapper(amazonDynamoDB());
    }

    @Bean
    public DynamoDB client() {
        return new DynamoDB(amazonDynamoDB());
    }

}
