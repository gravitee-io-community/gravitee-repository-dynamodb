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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import io.gravitee.repository.config.TestRepositoryInitializer;
import io.gravitee.repository.dynamodb.management.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static java.util.Collections.singletonList;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com)
 * @author GraviteeSource Team
 */
public class DynamoDBTestRepositoryInitializer implements TestRepositoryInitializer {

    @Autowired
    private DynamoDBMapper mapper;
    @Autowired
    private AmazonDynamoDB dynamo;

    public void setUp() {
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBTenant.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.TENANT_PRO_THROU));
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBView.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.VIEW_PRO_THROU));
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBTag.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.TAG_PRO_THROU));
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBUser.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.USER_PRO_THROU));
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBSubscription.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.SUBSCRIPTION_PRO_THROU));
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBPlan.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.PLAN_PRO_THROU));
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBPage.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.PAGE_PRO_THROU));
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBMembership.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.MEMBERSHIP_PRO_THROU).
                withGlobalSecondaryIndexes(Arrays.asList(
                        new GlobalSecondaryIndex().
                                withIndexName("UserAndReferenceType").
                                withKeySchema(
                                        new KeySchemaElement().withAttributeName("userId").withKeyType(KeyType.HASH),
                                        new KeySchemaElement().withAttributeName("referenceType").withKeyType(KeyType.RANGE)
                                ).
                                withProjection(new Projection().withProjectionType(ProjectionType.ALL)).
                                withProvisionedThroughput(DynamoDBGraviteeSchema.MEMBERSHIP_PRO_THROU),
                        new GlobalSecondaryIndex().
                                withIndexName("ReferenceTypeAndId").
                                withKeySchema(
                                        new KeySchemaElement().withAttributeName("referenceId").withKeyType(KeyType.HASH),
                                        new KeySchemaElement().withAttributeName("referenceType").withKeyType(KeyType.RANGE)
                                ).
                                withProjection(new Projection().withProjectionType(ProjectionType.ALL)).
                                withProvisionedThroughput(DynamoDBGraviteeSchema.MEMBERSHIP_PRO_THROU)
                        )));
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBGroup.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.GROUP_PRO_THROU));
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBApplication.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.APPLICATION_PRO_THROU).
                withGlobalSecondaryIndexes(singletonList(
                        new GlobalSecondaryIndex().
                                withIndexName("ApplicationStatus").
                                withKeySchema(
                                        new KeySchemaElement().withAttributeName("status").withKeyType(KeyType.HASH)
                                ).
                                withProjection(new Projection().withProjectionType(ProjectionType.ALL)).
                                withProvisionedThroughput(DynamoDBGraviteeSchema.APPLICATION_PRO_THROU)

                )));
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBApi.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.API_PRO_THROU).
                withGlobalSecondaryIndexes(singletonList(
                        new GlobalSecondaryIndex().
                                withIndexName("ApiVisibility").
                                withKeySchema(
                                        new KeySchemaElement().withAttributeName("visibility").withKeyType(KeyType.HASH)
                                ).
                                withProjection(new Projection().withProjectionType(ProjectionType.ALL)).
                                withProvisionedThroughput(DynamoDBGraviteeSchema.API_PRO_THROU)
                )));
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBApiKey.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.APIKEY_PRO_THROU).
                withGlobalSecondaryIndexes(Arrays.asList(
                        new GlobalSecondaryIndex().
                                withIndexName("ApiKeySubscription").
                                withKeySchema(
                                        new KeySchemaElement().withAttributeName("subscription").withKeyType(KeyType.HASH)
                                ).
                                withProjection(new Projection().withProjectionType(ProjectionType.ALL)).
                                withProvisionedThroughput(DynamoDBGraviteeSchema.APIKEY_PRO_THROU),
                        new GlobalSecondaryIndex().
                                withIndexName("ApiKeyPlan").
                                withKeySchema(
                                        new KeySchemaElement().withAttributeName("plan").withKeyType(KeyType.HASH),
                                        new KeySchemaElement().withAttributeName("updatedAt").withKeyType(KeyType.RANGE)
                                ).
                                withProjection(new Projection().withProjectionType(ProjectionType.ALL)).
                                withProvisionedThroughput(DynamoDBGraviteeSchema.APIKEY_PRO_THROU)
                )));
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBEvent.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.EVENT_PRO_THROU).
                withGlobalSecondaryIndexes(singletonList(
                        new GlobalSecondaryIndex().
                                withIndexName("EventKeyAndUpdateDate").
                                withKeySchema(
                                        new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH),
                                        new KeySchemaElement().withAttributeName("updatedAt").withKeyType(KeyType.RANGE)
                                ).
                                withProjection(new Projection().withProjectionType(ProjectionType.ALL)).
                                withProvisionedThroughput(DynamoDBGraviteeSchema.APIKEY_PRO_THROU)
                )));
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBEventSearchIndex.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.EVENT_SEARCH_INDEX_PRO_THROU));
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBMetadata.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.METADATA_PRO_THROU).
                withGlobalSecondaryIndexes(singletonList(
                        new GlobalSecondaryIndex().
                                withIndexName("Reference").
                                withKeySchema(
                                        new KeySchemaElement().withAttributeName("referenceType").withKeyType(KeyType.HASH),
                                        new KeySchemaElement().withAttributeName("referenceId").withKeyType(KeyType.RANGE)
                                ).
                                withProjection(new Projection().withProjectionType(ProjectionType.ALL)).
                                withProvisionedThroughput(DynamoDBGraviteeSchema.METADATA_PRO_THROU)
                )));
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBRole.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.ROLE_PRO_THROU).
                withGlobalSecondaryIndexes(singletonList(
                        new GlobalSecondaryIndex().
                                withIndexName("RoleScope").
                                withKeySchema(
                                        new KeySchemaElement().withAttributeName("scope").withKeyType(KeyType.HASH)
                                ).
                                withProjection(new Projection().withProjectionType(ProjectionType.ALL)).
                                withProvisionedThroughput(DynamoDBGraviteeSchema.ROLE_PRO_THROU)
                )));
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBRating.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.RATING_PRO_THROU).
                withGlobalSecondaryIndexes(singletonList(
                        new GlobalSecondaryIndex().
                                withIndexName("RatingApiAndUser").
                                withKeySchema(
                                        new KeySchemaElement().withAttributeName("api").withKeyType(KeyType.HASH),
                                        new KeySchemaElement().withAttributeName("user").withKeyType(KeyType.RANGE)
                                ).
                                withProjection(new Projection().withProjectionType(ProjectionType.ALL)).
                                withProvisionedThroughput(DynamoDBGraviteeSchema.RATING_PRO_THROU)
                )));
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBRatingAnswer.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.RATING_ANSWER_PRO_THROU).
                withGlobalSecondaryIndexes(singletonList(
                        new GlobalSecondaryIndex().
                                withIndexName("RatingAnswer").
                                withKeySchema(
                                        new KeySchemaElement().withAttributeName("rating").withKeyType(KeyType.HASH)
                                ).
                                withProjection(new Projection().withProjectionType(ProjectionType.ALL)).
                                withProvisionedThroughput(DynamoDBGraviteeSchema.RATING_ANSWER_PRO_THROU)
                )));
        TableUtils.createTableIfNotExists(dynamo, mapper.
                generateCreateTableRequest(DynamoDBAudit.class).
                withProvisionedThroughput(DynamoDBGraviteeSchema.AUDIT_PRO_THROU)
        );
    }

    public void tearDown() {
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBTenant.class));
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBView.class));
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBTag.class));
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBUser.class));
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBSubscription.class));
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBPlan.class));
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBPage.class));
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBMembership.class));
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBGroup.class));
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBApplication.class));
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBApi.class));
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBApiKey.class));
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBEvent.class));
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBEventSearchIndex.class));
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBMetadata.class));
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBRole.class));
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBRating.class));
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBRatingAnswer.class));
        TableUtils.deleteTableIfExists(dynamo, mapper.generateDeleteTableRequest(DynamoDBAudit.class));
    }
}
