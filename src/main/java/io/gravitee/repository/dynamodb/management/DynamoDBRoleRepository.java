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

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import io.gravitee.repository.dynamodb.management.model.DynamoDBRole;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.RoleRepository;
import io.gravitee.repository.management.model.Role;
import io.gravitee.repository.management.model.RoleScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@Repository
public class DynamoDBRoleRepository implements RoleRepository {

    private final Logger LOGGER = LoggerFactory.getLogger(DynamoDBRoleRepository.class);

    @Autowired
    private DynamoDBMapper mapper;

    @Override
    public Set<Role> findAll() throws TechnicalException {
        PaginatedScanList<DynamoDBRole> dynamoDBRoles = mapper.scan(DynamoDBRole.class, new DynamoDBScanExpression());
        return dynamoDBRoles.stream().map(this::convert).collect(Collectors.toSet());
    }

    @Override
    public Optional<Role> findById(RoleScope roleScope, String name) throws TechnicalException {
        String id = convertId(roleScope, name);
        DynamoDBRole load = mapper.load(DynamoDBRole.class, id);
        return Optional.ofNullable(convert(load));
    }

    @Override
    public Role create(Role role) throws TechnicalException {
        if (role == null) {
            throw new IllegalArgumentException("Trying to create null");
        }
        mapper.save(
                convert(role),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().withExists(false)
                )
        );
        return role;
    }

    @Override
    public Role update(Role role) throws TechnicalException {
        if (role == null) {
            throw new IllegalArgumentException("Trying to update null");
        }
        String id = convertId(role.getScope(), role.getName());
        try {
            mapper.save(
                    convert(role),
                    new DynamoDBSaveExpression().withExpectedEntry(
                            "id",
                            new ExpectedAttributeValue().
                                    withValue(new AttributeValue().withS(id)).
                                    withExists(true)
                    )
            );
            return role;
        } catch (ConditionalCheckFailedException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void delete(RoleScope roleScope, String name) throws TechnicalException {
        if (roleScope == null || name == null) {
            throw new IllegalArgumentException("Trying to delete null");
        }
        String id = convertId(roleScope, name);
        DynamoDBRole role = new DynamoDBRole();
        role.setId(id);
        mapper.delete(role);
    }

    @Override
    public Set<Role> findByScope(RoleScope scope) throws TechnicalException {
        DynamoDBRole dynamoDBRole = new DynamoDBRole();
        dynamoDBRole.setScope(scope.getId());
        return mapper.query(DynamoDBRole.class, new DynamoDBQueryExpression<DynamoDBRole>().
                withConsistentRead(false).
                withHashKeyValues(dynamoDBRole)).
                stream().
                map(this::convert).
                collect(Collectors.toSet());
    }

    private String convertId(RoleScope scope, String name) {
        return scope.getId() + ":" + name;
    }

    private Role convert(final DynamoDBRole dynamoDBRole) {
        if (dynamoDBRole == null) {
            return null;
        }

        final Role role = new Role();
        role.setName(dynamoDBRole.getName());
        role.setScope(RoleScope.valueOf(dynamoDBRole.getScope()));
        role.setDescription(dynamoDBRole.getDescription());
        role.setDefaultRole(dynamoDBRole.isDefaultRole());
        role.setSystem(dynamoDBRole.isSystem());
        int[] perms = dynamoDBRole.getPermissions().stream().mapToInt(x->x).toArray();
        role.setPermissions(perms);
        if (dynamoDBRole.getUpdatedAt() > 0) {
            role.setUpdatedAt(new Date(dynamoDBRole.getUpdatedAt()));
        }
        if (dynamoDBRole.getCreatedAt() > 0) {
            role.setCreatedAt(new Date(dynamoDBRole.getCreatedAt()));
        }

        return role;
    }

    private DynamoDBRole convert(final Role role) {
        if (role == null) {
            return null;
        }

        final DynamoDBRole dynamoDBRole = new DynamoDBRole();
        dynamoDBRole.setId(convertId(role.getScope(), role.getName()));
        dynamoDBRole.setName(role.getName());
        dynamoDBRole.setScope(role.getScope().getId());
        dynamoDBRole.setDescription(role.getDescription());
        dynamoDBRole.setDefaultRole(role.isDefaultRole());
        dynamoDBRole.setSystem(role.isSystem());
        Set<Integer> perms = new HashSet<>(role.getPermissions().length);
        for (int perm : role.getPermissions()) {
            perms.add(perm);
        }
        dynamoDBRole.setPermissions(perms);
        if (role.getUpdatedAt() != null) {
            dynamoDBRole.setUpdatedAt(role.getUpdatedAt().getTime());
        }
        if (role.getCreatedAt() != null) {
            dynamoDBRole.setCreatedAt(role.getCreatedAt().getTime());
        }

        return dynamoDBRole;
    }
}
