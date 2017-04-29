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

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@DynamoDBTable(tableName = DynamoDBGraviteeSchema.MEMBERSHIP_TABLENAME)
public class DynamoDBMembership {

    @DynamoDBHashKey
    private String id;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "UserAndReferenceType")
    private String userId;

    @DynamoDBIndexRangeKey(globalSecondaryIndexNames = {"UserAndReferenceType", "ReferenceTypeAndId"})
    private String referenceType;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "ReferenceTypeAndId")
    private String referenceId;

    @DynamoDBAttribute
    private String type;

    @DynamoDBAttribute
    private long createdAt;

    @DynamoDBAttribute
    private long updatedAt;


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReferenceType() {
        return referenceType;
    }
    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getReferenceId() {
        return referenceId;
    }
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamoDBMembership that = (DynamoDBMembership) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(referenceId, that.referenceId) &&
                Objects.equals(referenceType, that.referenceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, referenceId, referenceType);
    }

    @Override
    public String toString() {
        return "DynamoDBMembership{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", referenceType='" + referenceType + '\'' +
                ", referenceId='" + referenceId + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
