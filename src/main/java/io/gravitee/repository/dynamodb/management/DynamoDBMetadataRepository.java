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
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import io.gravitee.repository.dynamodb.management.model.DynamoDBMetadata;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.MetadataRepository;
import io.gravitee.repository.management.model.Metadata;
import io.gravitee.repository.management.model.MetadataFormat;
import io.gravitee.repository.management.model.MetadataReferenceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@Repository
public class DynamoDBMetadataRepository implements MetadataRepository {

    private final Logger LOGGER = LoggerFactory.getLogger(DynamoDBMetadataRepository.class);

    @Autowired
    private DynamoDBMapper mapper;

    @Override
    public Metadata create(Metadata item) throws TechnicalException {

        if (item == null) {
            throw new IllegalArgumentException("Trying to create null");
        }
        mapper.save(
                convert(item),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().withExists(false)
                )
        );
        return item;
    }

    @Override
    public Metadata update(Metadata metadata) throws TechnicalException {
        if (metadata == null || metadata.getName() == null) {
            throw new IllegalStateException("Metadata to update must have a name");
        }

        if (!findById(metadata.getKey(), metadata.getReferenceId(), metadata.getReferenceType()).isPresent()) {
            throw new IllegalStateException(String.format("No metadata found with key [%s], reference id [%s] and type [%s]",
                    metadata.getKey(), metadata.getReferenceId(), metadata.getReferenceType()));
        }
        try {
            mapper.save(
                    convert(metadata),
                    new DynamoDBSaveExpression().withExpectedEntry(
                            "id",
                            new ExpectedAttributeValue().
                                    withValue(new AttributeValue().withS(generateId(metadata))).
                                    withExists(true)
                    )
            );
            return metadata;
        } catch (ConditionalCheckFailedException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void delete(String key, String referenceId, MetadataReferenceType referenceType) throws TechnicalException {
        if (key == null || referenceId == null || referenceType == null) {
            throw new IllegalArgumentException("Trying to delete null");
        }
        DynamoDBMetadata metadata = new DynamoDBMetadata();
        metadata.setId(generateId(referenceType, referenceId, key));
        mapper.delete(metadata);
    }

    @Override
    public Optional<Metadata> findById(String key, String referenceId, MetadataReferenceType referenceType) throws TechnicalException {
        DynamoDBMetadata load = mapper.load(DynamoDBMetadata.class, generateId(referenceType, referenceId, key));
        return Optional.ofNullable(convert(load));
    }

    @Override
    public List<Metadata> findByReferenceType(MetadataReferenceType referenceType) throws TechnicalException {
        return findByReferenceTypeAndReferenceId(referenceType, null);
    }

    @Override
    public List<Metadata> findByReferenceTypeAndReferenceId(MetadataReferenceType referenceType, String referenceId) throws TechnicalException {
        DynamoDBMetadata dynamoDBMetadata = new DynamoDBMetadata();
        dynamoDBMetadata.setReferenceType(referenceType.name());
        DynamoDBQueryExpression<DynamoDBMetadata> queryExpression = new DynamoDBQueryExpression<DynamoDBMetadata>().
                withConsistentRead(false).
                withHashKeyValues(dynamoDBMetadata);
        if (referenceId != null) {
            queryExpression.
                    withRangeKeyCondition("referenceId", new Condition().
                            withComparisonOperator(ComparisonOperator.EQ).
                            withAttributeValueList(new AttributeValue().withS(referenceId)));
        }
        return mapper.
                query(DynamoDBMetadata.class, queryExpression).
                stream().
                map(this::convert).
                collect(Collectors.toList());
    }

    @Override
    public List<Metadata> findByKeyAndReferenceType(String key, MetadataReferenceType referenceType) throws TechnicalException {
        DynamoDBMetadata dynamoDBMetadata = new DynamoDBMetadata();
        dynamoDBMetadata.setReferenceType(referenceType.name());
        DynamoDBQueryExpression<DynamoDBMetadata> queryExpression = new DynamoDBQueryExpression<DynamoDBMetadata>().
                withConsistentRead(false).
                withHashKeyValues(dynamoDBMetadata).
                withFilterExpression("#k = :k").
                withExpressionAttributeValues(Collections.singletonMap(":k", new AttributeValue().withS(key))).
                withExpressionAttributeNames(Collections.singletonMap("#k", "key"));
        return mapper.
                query(DynamoDBMetadata.class, queryExpression).
                stream().
                map(this::convert).
                collect(Collectors.toList());
    }

    private String generateId(Metadata metadata) {
        return generateId(metadata.getReferenceType(), metadata.getReferenceId(), metadata.getKey());
    }

    private String generateId(MetadataReferenceType refType, String refId, String key) {
        return refType.name() + ":" + refId + ":" + key;
    }

    private Metadata convert(final DynamoDBMetadata dynamoDBMetadata) {
        if (dynamoDBMetadata == null) {
            return null;
        }
        final Metadata metadata = new Metadata();
        metadata.setKey(dynamoDBMetadata.getKey());
        metadata.setReferenceType(MetadataReferenceType.valueOf(dynamoDBMetadata.getReferenceType()));
        metadata.setReferenceId(dynamoDBMetadata.getReferenceId());
        metadata.setName(dynamoDBMetadata.getName());
        metadata.setFormat(MetadataFormat.valueOf(dynamoDBMetadata.getFormat()));
        metadata.setValue(dynamoDBMetadata.getValue());
        metadata.setCreatedAt(dynamoDBMetadata.getCreatedAt());
        metadata.setUpdatedAt(dynamoDBMetadata.getUpdatedAt());
        return metadata;
    }

    private DynamoDBMetadata convert(final Metadata metadata) {
        if (metadata == null) {
            return null;
        }
        final DynamoDBMetadata dynamoDBMetadata = new DynamoDBMetadata();
        dynamoDBMetadata.setId(generateId(metadata));
        dynamoDBMetadata.setKey(metadata.getKey());
        dynamoDBMetadata.setReferenceType(metadata.getReferenceType().name());
        dynamoDBMetadata.setReferenceId(metadata.getReferenceId());
        dynamoDBMetadata.setName(metadata.getName());
        dynamoDBMetadata.setFormat(metadata.getFormat().name());
        dynamoDBMetadata.setValue(metadata.getValue());
        dynamoDBMetadata.setCreatedAt(metadata.getCreatedAt());
        dynamoDBMetadata.setUpdatedAt(metadata.getUpdatedAt());
        return dynamoDBMetadata;
    }
}
