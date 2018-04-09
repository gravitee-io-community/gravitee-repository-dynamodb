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
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;

import java.util.List;
import java.util.Objects;

import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType.BOOL;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */

@DynamoDBTable(tableName = "Page")
public class DynamoDBPage {

    @DynamoDBHashKey
    private String id;
    @DynamoDBAttribute
    private String name;
    @DynamoDBAttribute
    private String type;
    @DynamoDBAttribute
    private String content;
    @DynamoDBAttribute
    private String lastContributor;
    @DynamoDBAttribute
    private int order;
    @DynamoDBAttribute(attributeName = "published")
    @DynamoDBTyped(BOOL)
    private boolean published;
    @DynamoDBAttribute
    private String api;
    @DynamoDBAttribute
    private long createdAt;
    @DynamoDBAttribute
    private long updatedAt;
    @DynamoDBAttribute
    private String sourceType;
    @DynamoDBAttribute
    private String sourceConfiguration;
    @DynamoDBAttribute(attributeName = "configurationTryIt")
    @DynamoDBTyped(BOOL)
    private boolean configurationTryIt;
    @DynamoDBAttribute
    private String configurationTryItURL;
    @DynamoDBAttribute(attributeName = "homepage")
    @DynamoDBTyped(BOOL)
    private boolean homepage;

    @DynamoDBAttribute
    private List<String> excludedGroups;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getApi() {
        return api;
    }
    public void setApi(String api) {
        this.api = api;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastContributor() {
        return lastContributor;
    }
    public void setLastContributor(String lastContributor) {
        this.lastContributor = lastContributor;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }
    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isPublished() {
        return published;
    }
    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSourceType() {
        return sourceType;
    }
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceConfiguration() {
        return sourceConfiguration;
    }
    public void setSourceConfiguration(String sourceConfiguration) {
        this.sourceConfiguration = sourceConfiguration;
    }

    public boolean isConfigurationTryIt() {
        return configurationTryIt;
    }
    public void setConfigurationTryIt(boolean configurationTryIt) {
        this.configurationTryIt = configurationTryIt;
    }

    public String getConfigurationTryItURL() {
        return configurationTryItURL;
    }
    public void setConfigurationTryItURL(String configurationTryItURL) {
        this.configurationTryItURL = configurationTryItURL;
    }

    public boolean isHomepage() {
        return homepage;
    }
    public void setHomepage(boolean homepage) {
        this.homepage = homepage;
    }

    public List<String> getExcludedGroups() {
        return excludedGroups;
    }

    public void setExcludedGroups(List<String> excludedGroups) {
        this.excludedGroups = excludedGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamoDBPage dynamoDBPage = (DynamoDBPage) o;
        return Objects.equals(id, dynamoDBPage.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DynamoDBPage{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", api='" + api + '\'' +
                ", homepage='" + homepage + '\'' +
                '}';
    }
}
