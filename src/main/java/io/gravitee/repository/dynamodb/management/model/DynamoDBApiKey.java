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
package io.gravitee.repository.dynamodb.management.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

import java.util.Objects;

import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType.BOOL;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@DynamoDBTable(tableName = "ApiKey")
public class DynamoDBApiKey {
    @DynamoDBHashKey
    private String key;
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "ApiKeySubscription")
    private String subscription;
    @DynamoDBAttribute
    private String application;
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "ApiKeyPlan")
    private String plan;
    @DynamoDBAttribute
    private long expireAt;
    @DynamoDBAttribute
    private long createdAt;
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "ApiKeyPlan")
    private long updatedAt;
    @DynamoDBTyped(BOOL)
    private boolean revoked;
    @DynamoDBAttribute
    private long revokeAt;

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    public String getSubscription() {
        return subscription;
    }
    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public String getApplication() {
        return application;
    }
    public void setApplication(String application) {
        this.application = application;
    }

    public String getPlan() {
        return plan;
    }
    public void setPlan(String plan) {
        this.plan = plan;
    }

    public long getExpireAt() {
        return expireAt;
    }
    public void setExpireAt(long expireAt) {
        this.expireAt = expireAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isRevoked() {
        return revoked;
    }
    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public long getRevokeAt() {
        return revokeAt;
    }
    public void setRevokeAt(long revokeAt) {
        this.revokeAt = revokeAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamoDBApiKey that = (DynamoDBApiKey) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
