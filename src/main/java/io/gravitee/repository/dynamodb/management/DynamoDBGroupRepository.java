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
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import io.gravitee.repository.dynamodb.management.model.DynamoDBGroup;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.GroupRepository;
import io.gravitee.repository.management.model.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@Repository
public class DynamoDBGroupRepository implements GroupRepository {

    @Autowired
    private DynamoDBMapper mapper;

    @Override
    public Set<Group> findByType(Group.Type type) throws TechnicalException {
        DynamoDBGroup dynamoDBGroup = new DynamoDBGroup();
        dynamoDBGroup.setType(type.name());
        return mapper.query(DynamoDBGroup.class, new DynamoDBQueryExpression<DynamoDBGroup>().
                withConsistentRead(false).
                withHashKeyValues(dynamoDBGroup)).
                stream().
                map(this::convert).
                collect(Collectors.toSet());
    }

    @Override
    public Set<Group> findAll() throws TechnicalException {
        PaginatedScanList<DynamoDBGroup> dynamoDBGroups = mapper.scan(DynamoDBGroup.class, new DynamoDBScanExpression());
        return dynamoDBGroups.stream().map(this::convert).collect(Collectors.toSet());
    }

    @Override
    public Optional<Group> findById(String id) throws TechnicalException {
        DynamoDBGroup load = mapper.load(DynamoDBGroup.class, id);
        return Optional.ofNullable(convert(load));
    }

    @Override
    public Group create(Group group) throws TechnicalException {
        if (group == null) {
            throw new IllegalArgumentException("Trying to create null");
        }
        if (group.getAdministrators() == null) {
            group.setAdministrators(Collections.emptyList());
        }
        mapper.save(
                convert(group),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().withExists(false)
                )
        );
        return group;
    }

    @Override
    public Group update(Group group) throws TechnicalException {
        if (group == null) {
            throw new IllegalStateException("Group must not be null");
        }

        if (!findById(group.getId()).isPresent()) {
            throw new IllegalStateException(String.format("No group found with id [%s]", group.getId()));
        }
        mapper.save(
                convert(group),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().
                                withValue(new AttributeValue().withS(group.getId())).
                                withExists(true)
                )
        );
        return group;
    }

    @Override
    public void delete(String id) throws TechnicalException {
        if (id == null) {
            throw new IllegalArgumentException("Trying to delete null");
        }
        DynamoDBGroup group = new DynamoDBGroup();
        group.setId(id);
        mapper.delete(group);
    }

    private Group convert(DynamoDBGroup dynamoDBGroup) {
        if (dynamoDBGroup==null) {
            return null;
        }
        Group group = new Group();
        group.setId(dynamoDBGroup.getId());
        group.setName(dynamoDBGroup.getName());
        group.setType(Group.Type.valueOf(dynamoDBGroup.getType()));
        group.setCreatedAt(dynamoDBGroup.getCreatedAt());
        group.setUpdatedAt(dynamoDBGroup.getUpdatedAt());
        group.setAdministrators(dynamoDBGroup.getAdministrators()==null ? Collections.emptyList() : dynamoDBGroup.getAdministrators());
        return group;
    }

    private DynamoDBGroup convert(Group group) {
        if (group==null) {
            return null;
        }
        DynamoDBGroup dynamoDBGroup = new DynamoDBGroup();
        dynamoDBGroup.setId(group.getId());
        dynamoDBGroup.setName(group.getName());
        dynamoDBGroup.setType(group.getType().name());
        dynamoDBGroup.setCreatedAt(group.getCreatedAt());
        dynamoDBGroup.setUpdatedAt(group.getUpdatedAt());
        if (group.getAdministrators() != null && !group.getAdministrators().isEmpty()) {
            dynamoDBGroup.setAdministrators(group.getAdministrators());
        }
        return dynamoDBGroup;
    }
}
