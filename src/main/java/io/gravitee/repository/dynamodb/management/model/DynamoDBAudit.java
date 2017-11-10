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

import java.util.Map;
import java.util.Objects;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@DynamoDBTable(tableName = DynamoDBGraviteeSchema.AUDIT_TABLENAME)
public class DynamoDBAudit {
    @DynamoDBHashKey
    private String id;
    @DynamoDBAttribute
    private String referenceId;
    @DynamoDBAttribute
    private String referenceType;
    @DynamoDBAttribute
    private String reference;
    @DynamoDBAttribute
    private String username;
    @DynamoDBAttribute
    private String event;
    @DynamoDBAttribute
    private Map<String,String> properties;
    @DynamoDBAttribute
    private String patch;
    @DynamoDBAttribute
    private long createdAt;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getReferenceId() {
        return referenceId;
    }
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }
    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEvent() {
        return event;
    }
    public void setEvent(String event) {
        this.event = event;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getPatch() {
        return patch;
    }
    public void setPatch(String patch) {
        this.patch = patch;
    }

    public long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getReference() {
        return reference;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamoDBAudit user = (DynamoDBAudit) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Audit{");
        sb.append(" id='").append(id).append(",\'");
        sb.append(" referenceId='").append(referenceId).append("\'");
        sb.append(" referenceType='").append(referenceType).append("\'");
        sb.append(" username='").append(username).append("\'");
        sb.append(" event='").append(event).append("\'");
        sb.append(" properties='").append(properties).append("\'");
        sb.append(" patch='").append(patch).append("\'");
        sb.append(" createdAt='").append(createdAt).append("\'");
        sb.append('}');
        return sb.toString();
    }
}
