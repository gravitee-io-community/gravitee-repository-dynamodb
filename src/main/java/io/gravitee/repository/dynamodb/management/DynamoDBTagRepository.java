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
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import io.gravitee.repository.dynamodb.management.model.DynamoDBTag;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.TagRepository;
import io.gravitee.repository.management.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@Repository
public class DynamoDBTagRepository implements TagRepository {

    private final Logger LOGGER = LoggerFactory.getLogger(DynamoDBTagRepository.class);

    @Autowired
    private DynamoDBMapper mapper;

    @Override
    public Set<Tag> findAll() throws TechnicalException {
        PaginatedScanList<DynamoDBTag> dynamoDBTags = mapper.scan(DynamoDBTag.class, new DynamoDBScanExpression());
        return dynamoDBTags.stream().map(this::convert).collect(Collectors.toSet());
    }

    @Override
    public Optional<Tag> findById(String id) throws TechnicalException {
        DynamoDBTag load = mapper.load(DynamoDBTag.class, id);
        return Optional.ofNullable(convert(load));
    }

    @Override
    public Tag create(Tag tag) throws TechnicalException {
        if (tag == null) {
            throw new IllegalArgumentException("Trying to create null");
        }
        mapper.save(
                convert(tag),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().withExists(false)
                )
        );
        return tag;
    }

    @Override
    public Tag update(Tag tag) throws TechnicalException {
        if (tag == null) {
            throw new IllegalArgumentException("Trying to update null");
        }
        mapper.save(
                convert(tag),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().
                                withValue(new AttributeValue().withS(tag.getId())).
                                withExists(true)
                )
        );
        return tag;
    }

    @Override
    public void delete(String id) throws TechnicalException {
        if (id == null) {
            throw new IllegalArgumentException("Trying to delete null");
        }
        DynamoDBTag tag = new DynamoDBTag();
        tag.setId(id);
        mapper.delete(tag);
    }

    private Tag convert(final DynamoDBTag dynamoDBTag) {
        if (dynamoDBTag == null) {
            return null;
        }
        final Tag tag = new Tag();
        tag.setId(dynamoDBTag.getId());
        tag.setName(dynamoDBTag.getName());
        tag.setDescription(dynamoDBTag.getDescription());
        return tag;
    }

    private DynamoDBTag convert(final Tag tag) {
        if (tag == null) {
            return null;
        }
        final DynamoDBTag dynamoDBTag = new DynamoDBTag();
        dynamoDBTag.setId(tag.getId());
        dynamoDBTag.setName(tag.getName());
        dynamoDBTag.setDescription(tag.getDescription());
        return dynamoDBTag;
    }
}
