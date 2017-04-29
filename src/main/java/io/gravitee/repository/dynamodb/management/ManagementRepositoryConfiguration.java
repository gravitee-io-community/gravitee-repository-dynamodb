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
package io.gravitee.repository.dynamodb.management;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import io.gravitee.repository.Scope;
import io.gravitee.repository.dynamodb.common.AbstractRepositoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com)
 * @author GraviteeSource Team
 */
@Configuration
@ComponentScan
@Profile("!test")
public class ManagementRepositoryConfiguration extends AbstractRepositoryConfiguration {
    private final Logger logger = LoggerFactory.getLogger(ManagementRepositoryConfiguration.class);

    @Autowired
    private Environment environment;

    private final String propertyPrefix = Scope.MANAGEMENT.getName() + ".dynamodb.";

    @Override
    protected Scope getScope() {
        return Scope.MANAGEMENT;
    }

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {

        String region = readPropertyValue(propertyPrefix + "awsRegion");
        String accessKeyId = readPropertyValue(propertyPrefix + "awsAccessKeyId");
        String secretKey = readPropertyValue(propertyPrefix + "awsSecretKey");
        String endpoint = readPropertyValue(propertyPrefix + "awsEndpoint");

        AmazonDynamoDBClientBuilder clientBuilder = AmazonDynamoDBClientBuilder.standard();

        if (region != null && accessKeyId != null && secretKey != null) {
            if(logger.isDebugEnabled()) {
                logger.debug("Load AWS Credentials from gravitee.yml");
                logger.debug("    aws.region: {}", region);
                logger.debug("    aws.access.key.id: {}", accessKeyId);
                logger.debug("    aws.secret.key: {}", secretKey.replaceAll(".*", "#"));
            }
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKeyId, secretKey);
            clientBuilder.
                    withRegion(region).
                    withCredentials(new AWSStaticCredentialsProvider(awsCredentials));

            if(endpoint != null) {
                logger.debug("    aws.endpoint: {}", endpoint);
                clientBuilder.setRegion(null);
                clientBuilder.
                        withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region));
            }
        } else {
            logger.debug("Load default AWS Credentials");
        }

        return clientBuilder.build();
    }

    @Bean
    public DynamoDBMapper dynamoDBMapper() {
        return new DynamoDBMapper(amazonDynamoDB());
    }

    @Bean
    public DynamoDB client() {
        return new DynamoDB(amazonDynamoDB());
    }

    private String readPropertyValue(String propertyName) {
        return readPropertyValue(propertyName, String.class, null);
    }

    private <T> T readPropertyValue(String propertyName, Class<T> propertyType, T defaultValue) {
        T value = environment.getProperty(propertyName, propertyType, defaultValue);
        logger.debug("Read property {}: {}", propertyName, value);
        return value;
    }
}
