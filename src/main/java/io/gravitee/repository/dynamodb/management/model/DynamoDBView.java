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

import java.util.Objects;

import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType.BOOL;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@DynamoDBTable(tableName = DynamoDBGraviteeSchema.VIEW_TABLENAME)
public class DynamoDBView {

    @DynamoDBHashKey
    private String id;
    @DynamoDBAttribute
    private String name;
    @DynamoDBAttribute
    private String description;
    @DynamoDBTyped(BOOL)
    private boolean defaultView;
    @DynamoDBTyped(BOOL)
    private boolean hidden;
    @DynamoDBAttribute
    private int order;
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

    public boolean isDefaultView() {
        return defaultView;
    }
    public void setDefaultView(boolean defaultView) {
        this.defaultView = defaultView;
    }

    public boolean isHidden() {
        return hidden;
    }
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public int getOrder() {
        return order;
    }
    public void setOrder(int order) {
        this.order = order;
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
        if (!(o instanceof DynamoDBView)) return false;
        DynamoDBView view = (DynamoDBView) o;
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
                '}';
    }
}
