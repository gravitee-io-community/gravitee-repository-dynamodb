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
import java.util.Set;

import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType.BOOL;
import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType.NS;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@DynamoDBTable(tableName = DynamoDBGraviteeSchema.ROLE_TABLENAME)
public class DynamoDBRole {

    @DynamoDBHashKey
    private String id;
    @DynamoDBAttribute
    private String name;
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "RoleScope")
    private int scope;
    @DynamoDBAttribute
    private String description;
    @DynamoDBAttribute(attributeName = "defaultRole")
    @DynamoDBTyped(BOOL)
    private boolean defaultRole;
    @DynamoDBAttribute(attributeName = "systemRole")
    @DynamoDBTyped(BOOL)
    private boolean system;
    @DynamoDBAttribute
    private Set<Integer> permissions;
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

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDefaultRole() {
        return defaultRole;
    }
    public void setDefaultRole(boolean defaultRole) {
        this.defaultRole = defaultRole;
    }

    public boolean isSystem() {
        return system;
    }
    public void setSystem(boolean system) {
        this.system = system;
    }

    public Set<Integer> getPermissions() {
        return permissions;
    }
    public void setPermissions(Set<Integer> permissions) {
        this.permissions = permissions;
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
        if (!(o instanceof DynamoDBRole)) return false;
        DynamoDBRole view = (DynamoDBRole) o;
        return Objects.equals(id, view.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DynamoDBView{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", scope='" + scope + '\'' +
                '}';
    }
}
