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

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@DynamoDBTable(tableName = DynamoDBGraviteeSchema.SUBSCRIPTION_TABLENAME)
public class DynamoDBSubscription {

    @DynamoDBHashKey
    private String id;
    @DynamoDBAttribute
    private String plan;
    @DynamoDBAttribute
    private String application;
    @DynamoDBAttribute
    private long processedAt;
    @DynamoDBAttribute
    private String reason;
    @DynamoDBAttribute
    private String status;
    @DynamoDBAttribute
    private String processedBy;
    @DynamoDBAttribute
    private String subscribedBy;
    @DynamoDBAttribute
    private long startingAt;
    @DynamoDBAttribute
    private long endingAt;
    @DynamoDBAttribute
    private long createdAt;
    @DynamoDBAttribute
    private long updatedAt;
    @DynamoDBAttribute
    private long closedAt;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getPlan() {
        return plan;
    }
    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getApplication() {
        return application;
    }
    public void setApplication(String application) {
        this.application = application;
    }

    public long getProcessedAt() {
        return processedAt;
    }
    public void setProcessedAt(long processedAt) {
        this.processedAt = processedAt;
    }

    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcessedBy() {
        return processedBy;
    }
    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    public String getSubscribedBy() {
        return subscribedBy;
    }
    public void setSubscribedBy(String subscribedBy) {
        this.subscribedBy = subscribedBy;
    }

    public long getStartingAt() {
        return startingAt;
    }
    public void setStartingAt(long startingAt) {
        this.startingAt = startingAt;
    }

    public long getEndingAt() {
        return endingAt;
    }
    public void setEndingAt(long endingAt) {
        this.endingAt = endingAt;
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

    public long getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(long closedAt) {
        this.closedAt = closedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DynamoDBSubscription that = (DynamoDBSubscription) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "DynamoDBSubscription{" +
                "id='" + id + '\'' +
                ", plan='" + plan + '\'' +
                ", application='" + application + '\'' +
                '}';
    }
}
