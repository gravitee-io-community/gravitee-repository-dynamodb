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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@DynamoDBTable(tableName = DynamoDBGraviteeSchema.USER_TABLENAME)
public class DynamoDBUser {
    @DynamoDBHashKey
    private String username;
    @DynamoDBAttribute
    private String email;
    @DynamoDBAttribute
    private String password;
    @DynamoDBAttribute
    private String firstname;
    @DynamoDBAttribute
    private String lastname;
    @DynamoDBAttribute
    private Set<String> roles = new HashSet<>();
    @DynamoDBAttribute
    private String picture;
    @DynamoDBAttribute
    private String source;
    @DynamoDBAttribute
    private String sourceId;
    @DynamoDBAttribute
    private long lastConnectionAt;
    @DynamoDBAttribute
    private long createdAt;
    @DynamoDBAttribute
    private long updatedAt;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String mail) {
        this.email = mail;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Set<String> getRoles() {
        return roles;
    }
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getPicture() {
        return picture;
    }
    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceId() {
        return sourceId;
    }
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public long getLastConnectionAt() {
        return lastConnectionAt;
    }
    public void setLastConnectionAt(long lastConnectionAt) {
        this.lastConnectionAt = lastConnectionAt;
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
        DynamoDBUser user = (DynamoDBUser) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append(" email='").append(email).append(",\'");
        sb.append(" username='").append(username).append("\'");
        sb.append('}');
        return sb.toString();
    }
}
