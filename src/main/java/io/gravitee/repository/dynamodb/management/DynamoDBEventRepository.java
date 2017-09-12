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
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import io.gravitee.common.data.domain.Page;
import io.gravitee.repository.dynamodb.management.model.DynamoDBEvent;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.EventRepository;
import io.gravitee.repository.management.api.search.EventCriteria;
import io.gravitee.repository.management.api.search.Pageable;
import io.gravitee.repository.management.model.Event;
import io.gravitee.repository.management.model.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@Repository
public class DynamoDBEventRepository implements EventRepository {

    @Autowired
    private DynamoDBMapper mapper;

    @Autowired
    private DynamoDBEventSearchIndexRepository indicesRepository;

    @Override
    public List<Event> search(EventCriteria eventCriteria) {
        return search(eventCriteria, null).getContent();
    }
    @Override
    public Page<Event> search(EventCriteria eventCriteria, Pageable pageable) {
        final List<Event> result = new ArrayList<>();

        Set<String> searchEventIds = indicesRepository.search(eventCriteria);
        if (searchEventIds.isEmpty()) {
            return new Page<>(Collections.emptyList(), 0, 0, 0);
        }
        DynamoDBQueryExpression<DynamoDBEvent> queryExpression = new DynamoDBQueryExpression<>();
        if (eventCriteria.getFrom() != 0 && eventCriteria.getTo() != 0) {
            queryExpression.withRangeKeyCondition("updatedAt", new Condition().
                    withComparisonOperator(ComparisonOperator.BETWEEN).
                    withAttributeValueList(
                            new AttributeValue().withN(String.valueOf(eventCriteria.getFrom())),
                            new AttributeValue().withN(String.valueOf(eventCriteria.getTo()))
                    ));
        }

        searchEventIds.
                forEach(id -> {
                    DynamoDBEvent dynamoDBEvent = new DynamoDBEvent();
                    dynamoDBEvent.setId(id);
                    queryExpression.
                            withConsistentRead(false).
                            withHashKeyValues(dynamoDBEvent);
                    result.addAll(mapper.query(DynamoDBEvent.class, queryExpression).
                            stream().
                            map(this::convert).
                            collect(Collectors.toSet()));
                });

        // sort by updatedAt descending
        List<Event> sortedResult = result.
                stream().
                sorted(Comparator.comparing(Event::getUpdatedAt).reversed()).
                collect(Collectors.toList());

        long total = sortedResult.size();
        int page = isNull(pageable)?0:pageable.pageNumber();

        return new Page<>(sortedResult, page, sortedResult.size(), total);
    }

    @Override
    public Optional<Event> findById(String id) throws TechnicalException {
        DynamoDBEvent load = mapper.load(DynamoDBEvent.class, id);
        return Optional.ofNullable(convert(load));
    }

    @Override
    public Event create(Event event) throws TechnicalException {
        if (isNull(event)) {
            throw new IllegalArgumentException("Trying to create null");
        }
        indicesRepository.create(event);
        mapper.save(
                convert(event),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().withExists(false)
                )
        );
        return event;
    }

    @Override
    public Event update(Event event) throws TechnicalException {
        if (event == null || event.getId() == null) {
            throw new IllegalStateException("Event to update must have an id");
        }

        if (!findById(event.getId()).isPresent()) {
            throw new IllegalStateException(String.format("No event found with id [%s]", event.getId()));
        }

        mapper.save(
                convert(event),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().
                                withValue(new AttributeValue().withS(event.getId())).
                                withExists(true)
                )
        );
        return event;
    }

    @Override
    public void delete(String id) throws TechnicalException {
        if (id == null) {
            throw new IllegalArgumentException("Trying to delete null");
        }
        Optional<Event> event = findById(id);
        if(event.isPresent()) {
            indicesRepository.delete(event.get());
            DynamoDBEvent dynamoDBEvent = new DynamoDBEvent();
            dynamoDBEvent.setId(event.get().getId());
            mapper.delete(dynamoDBEvent);
        } else {
            throw new TechnicalException("Event "+ id + " is unknown");
        }
    }

    private Event convert(DynamoDBEvent dynamoDBEvent) {
        if (dynamoDBEvent == null) {
            return null;
        }

        Event event = new Event();
        event.setId(dynamoDBEvent.getId());
        event.setParentId(dynamoDBEvent.getParentId());
        event.setCreatedAt(new Date(dynamoDBEvent.getCreatedAt()));
        event.setUpdatedAt(new Date(dynamoDBEvent.getUpdatedAt()));
        event.setPayload(dynamoDBEvent.getPayload());
        event.setProperties(dynamoDBEvent.getProperties());
        event.setType(EventType.valueOf(dynamoDBEvent.getType()));
        return event;
    }

    private DynamoDBEvent convert(Event event) {
        DynamoDBEvent dynamoDBEvent = new DynamoDBEvent();
        dynamoDBEvent.setId(event.getId());
        dynamoDBEvent.setParentId(event.getParentId());
        dynamoDBEvent.setCreatedAt(event.getCreatedAt().getTime());
        dynamoDBEvent.setUpdatedAt(event.getUpdatedAt().getTime());
        dynamoDBEvent.setPayload(event.getPayload());
        dynamoDBEvent.setProperties(event.getProperties());
        dynamoDBEvent.setType(event.getType().name());
        return dynamoDBEvent;
    }
}
