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
package io.gravitee.repository.dynamodb.management;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import io.gravitee.repository.dynamodb.management.model.DynamoDBUser;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.UserRepository;
import io.gravitee.repository.management.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@Repository
public class DynamoDBUserRepository implements UserRepository {

    private final Logger LOGGER = LoggerFactory.getLogger(DynamoDBUserRepository.class);

    @Autowired
    private DynamoDBMapper mapper;

    @Override
    public User create(User user) throws TechnicalException {
        if (user == null) {
            throw new IllegalArgumentException("Trying to create null");
        }
        mapper.save(
                convert(user),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "username",
                        new ExpectedAttributeValue().withExists(false)
                )
        );
        return user;
    }

    @Override
    public User update(User user) throws TechnicalException {
        if (user == null) {
            throw new IllegalArgumentException("Trying to update null");
        }
        try {
            mapper.save(
                    convert(user),
                    new DynamoDBSaveExpression().withExpectedEntry(
                            "username",
                            new ExpectedAttributeValue().
                                    withValue(new AttributeValue().withS(user.getUsername())).
                                    withExists(true)
                    )
            );
            return user;
        } catch (ConditionalCheckFailedException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Optional<User> findByUsername(String username) throws TechnicalException {
        DynamoDBUser load = mapper.load(DynamoDBUser.class, username);
        return Optional.ofNullable(convert(load));
    }

    @Override
    public Set<User> findByUsernames(List<String> usernames) throws TechnicalException {
        if (usernames == null || usernames.isEmpty()) {
            return Collections.emptySet();
        }

        Map<String, List<Object>> result = mapper.batchLoad(usernames.stream().map(u -> {
            DynamoDBUser du = new DynamoDBUser();
            du.setUsername(u);
            return du;
        }).collect(Collectors.toSet()));

        if (result != null && !result.isEmpty()) {
            List<Object> users = result.entrySet().iterator().next().getValue();
            return users.stream().map(o -> convert((DynamoDBUser)o)).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    @Override
    public Set<User> findAll() throws TechnicalException {
        PaginatedScanList<DynamoDBUser> dynamoDBUsers = mapper.scan(DynamoDBUser.class, new DynamoDBScanExpression());
        return dynamoDBUsers.stream().
                map(this::convert).
                collect(Collectors.toSet());
    }

    private User convert(DynamoDBUser dynamoDBUser) {
        if (dynamoDBUser == null) {
            return null;
        }

        User user = new User();
        user.setUsername(dynamoDBUser.getUsername());
        user.setEmail(dynamoDBUser.getEmail());
        user.setFirstname(dynamoDBUser.getFirstname());
        user.setLastname(dynamoDBUser.getLastname());
        user.setPassword(dynamoDBUser.getPassword());
        if (dynamoDBUser.getRoles() != null && !dynamoDBUser.getRoles().isEmpty()) {
            user.setRoles(dynamoDBUser.getRoles());
        }
        user.setCreatedAt(new Date(dynamoDBUser.getCreatedAt()));
        user.setUpdatedAt(new Date(dynamoDBUser.getUpdatedAt()));
        user.setPicture(dynamoDBUser.getPicture());
        user.setSource(dynamoDBUser.getSource());
        user.setSourceId(dynamoDBUser.getSourceId());

        if (dynamoDBUser.getLastConnectionAt() != 0) {
            user.setLastConnectionAt(new Date(dynamoDBUser.getLastConnectionAt()));
        }

        return user;
    }

    private DynamoDBUser convert(User user) {
        DynamoDBUser dynamoDBUser = new DynamoDBUser();
        dynamoDBUser.setUsername(user.getUsername());
        dynamoDBUser.setEmail(user.getEmail());
        dynamoDBUser.setFirstname(user.getFirstname());
        dynamoDBUser.setLastname(user.getLastname());
        dynamoDBUser.setPassword(user.getPassword());
        dynamoDBUser.setRoles(user.getRoles());
        dynamoDBUser.setCreatedAt(user.getCreatedAt().getTime());
        dynamoDBUser.setUpdatedAt(user.getUpdatedAt().getTime());
        dynamoDBUser.setPicture(user.getPicture());
        dynamoDBUser.setSource(user.getSource());
        dynamoDBUser.setSourceId(user.getSourceId());

        if (user.getLastConnectionAt() != null) {
            dynamoDBUser.setLastConnectionAt(user.getLastConnectionAt().getTime());
        }
        return dynamoDBUser;
    }
}
