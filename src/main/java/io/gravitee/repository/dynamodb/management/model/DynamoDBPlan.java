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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.util.List;
import java.util.Set;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@DynamoDBTable(tableName = "Plan")
public class DynamoDBPlan {
    @DynamoDBHashKey
    private String id;
    @DynamoDBAttribute
    private String name;
    @DynamoDBAttribute
    private String description;
    @DynamoDBAttribute
    private String validation;
    @DynamoDBAttribute
    private String type;
    @DynamoDBAttribute
    private int order;
    @DynamoDBAttribute
    private Set<String> apis;
    @DynamoDBAttribute
    private String definition;
    @DynamoDBAttribute
    private List<String> characteristics;
    @DynamoDBAttribute
    private long publishedAt;
    @DynamoDBAttribute
    private long closedAt;
    @DynamoDBAttribute
    private String status;
    @DynamoDBAttribute
    private String security;
    @DynamoDBAttribute
    private List<String> excludedGroups;
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

    public String getValidation() {
        return validation;
    }
    public void setValidation(String validation) {
        this.validation = validation;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public int getOrder() {
        return order;
    }
    public void setOrder(int order) {
        this.order = order;
    }

    public Set<String> getApis() {
        return apis;
    }
    public void setApis(Set<String> apis) {
        this.apis = apis;
    }

    public String getDefinition() {
        return definition;
    }
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public List<String> getCharacteristics() {
        return characteristics;
    }
    public void setCharacteristics(List<String> characteristics) {
        this.characteristics = characteristics;
    }

    public long getPublishedAt() {
        return publishedAt;
    }
    public void setPublishedAt(long publishedAt) {
        this.publishedAt = publishedAt;
    }

    public long getClosedAt() {
        return closedAt;
    }
    public void setClosedAt(long closedAt) {
        this.closedAt = closedAt;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getSecurity() {
        return security;
    }
    public void setSecurity(String security) {
        this.security = security;
    }

    public List<String> getExcludedGroups() {
        return excludedGroups;
    }
    public void setExcludedGroups(List<String> excludedGroups) {
        this.excludedGroups = excludedGroups;
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

        DynamoDBPlan dynamoDBPlan = (DynamoDBPlan) o;

        return id.equals(dynamoDBPlan.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "DynamoDBPlan{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
