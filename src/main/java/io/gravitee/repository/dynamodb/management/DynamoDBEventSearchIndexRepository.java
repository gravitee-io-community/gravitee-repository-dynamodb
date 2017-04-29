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
import io.gravitee.repository.dynamodb.management.model.DynamoDBEventSearchIndex;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.search.EventCriteria;
import io.gravitee.repository.management.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@Repository
public class DynamoDBEventSearchIndexRepository {

    @Autowired
    private DynamoDBMapper mapper;


    void create(Event event) throws TechnicalException {
        Set<String> indices = generateIndices(event);

        //generate potentially new indices
        Set<DynamoDBEventSearchIndex> newIndices = indices.stream().map(i -> {
            DynamoDBEventSearchIndex index = new DynamoDBEventSearchIndex();
            index.setId(i);
            index.setEvents(Collections.singleton(event.getId()));
            return index;
        }).collect(Collectors.toSet());

        //get old indices and update them
        Set<DynamoDBEventSearchIndex> oldIndex = new HashSet<>();
        Map<String, List<Object>> batchResult = mapper.batchLoad(indices.
                stream().
                map(i -> {
                    DynamoDBEventSearchIndex index = new DynamoDBEventSearchIndex();
                    index.setId(i);
                    return index;
                }).
                collect(Collectors.toList()));
        if (nonNull(batchResult) && !batchResult.isEmpty()) {
            oldIndex.addAll(batchResult.
                    entrySet().
                    iterator().
                    next().
                    getValue().
                    stream().
                    map(o -> {
                        DynamoDBEventSearchIndex index = (DynamoDBEventSearchIndex) o;
                        index.getEvents().add(event.getId());
                        return index;
                    }).
                    collect(Collectors.toSet()));
        }

        //save indices 
        oldIndex.addAll(newIndices);
        mapper.batchSave(oldIndex);
    }

    void delete(Event event) throws TechnicalException {
        Set<String> indices = generateIndices(event);
        Map<String, List<Object>> batchResult = mapper.batchLoad(indices.
                stream().
                map(i -> {
                    DynamoDBEventSearchIndex index = new DynamoDBEventSearchIndex();
                    index.setId(i);
                    return index;
                }).
                collect(Collectors.toList()));
        if (nonNull(batchResult) && !batchResult.isEmpty()) {
            List<DynamoDBEventSearchIndex> indicesToDelete = new ArrayList<>();
            List<DynamoDBEventSearchIndex> indicesToUpdate = batchResult.
                    entrySet().
                    iterator().
                    next().
                    getValue().
                    stream().
                    map(o -> {
                        DynamoDBEventSearchIndex index = (DynamoDBEventSearchIndex) o;
                        index.getEvents().remove(event.getId());
                        if (index.getEvents().isEmpty()) {
                            indicesToDelete.add(index);
                            return null;
                        }
                        return index;
                    }).
                    filter(Objects::nonNull).
                    collect(Collectors.toList());

            if (!indicesToDelete.isEmpty()) {
                mapper.batchDelete(indicesToDelete);
            }
            if (!indicesToUpdate.isEmpty()) {
                mapper.batchSave(indicesToUpdate);
            }
        }
    }

    void update(Event previousEvent, Event newEvent) throws TechnicalException {
        if (isNull(previousEvent.getProperties()) || previousEvent.getProperties().isEmpty()) {
            create(newEvent);
        } else if (isNull(newEvent.getProperties()) || newEvent.getProperties().isEmpty()) {
            delete(previousEvent);
        } else {
            List<String> previousIndices = previousEvent.getProperties().
                    entrySet().
                    stream().
                    map(kv -> generateIndexKey(kv.getKey(), kv.getValue())).
                    collect(Collectors.toList());
            List<String> newIndices = newEvent.getProperties().
                    entrySet().
                    stream().
                    map(kv -> generateIndexKey(kv.getKey(), kv.getValue())).
                    collect(Collectors.toList());

            List<String> toDelete = previousIndices.stream().filter(i -> !newIndices.contains(i)).collect(Collectors.toList());
            List<String> toAdd = newIndices.stream().filter(i -> !previousIndices.contains(i)).collect(Collectors.toList());
            if (!toDelete.isEmpty()) {

                mapper.batchDelete(toDelete);
            }
            if (!toAdd.isEmpty()) {
                mapper.batchSave(toAdd);
            }
        }
    }

    Set<String> search(EventCriteria eventCriteria) {
        final Set<String> eventIds = new HashSet<>();
        if (nonNull(eventCriteria.getTypes()) && !eventCriteria.getTypes().isEmpty()) {
            eventIds.addAll(getEventIds(eventCriteria.getTypes().
                    stream().
                    map(t -> generateEventTypeIndexKey(t.name())).
                    collect(Collectors.toSet())));
            if(eventIds.isEmpty()) {
                return Collections.emptySet();
            }
        }
        if (nonNull(eventCriteria.getProperties()) && !eventCriteria.getProperties().isEmpty()) {
            for (Map.Entry<String, Object> entry : eventCriteria.getProperties().entrySet()) {
                String propKey = entry.getKey();
                Object propValue = entry.getValue();
                Set<String> eventIdsByProp;
                if (propValue instanceof Collection) {
                    Set<String> indices = ((Collection<String>) propValue).stream().map(v -> generateIndexKey(propKey, v)).collect(Collectors.toSet());
                    eventIdsByProp = getEventIds(indices);
                } else {
                    eventIdsByProp = getEventIds(Collections.singleton(generateIndexKey(propKey, (String) propValue)));
                }

                if (eventIdsByProp.isEmpty()) {
                    return Collections.emptySet();
                }

                if (eventIds.isEmpty()) {
                    eventIds.addAll(eventIdsByProp);
                } else {
                    eventIds.retainAll(eventIdsByProp);
                }
            }
        }

        return eventIds;
    }

    private Set<String> getEventIds(Set<String> indices) {
        Set<String> eventIds = new HashSet<>();
        Map<String, List<Object>> batchResult = mapper.batchLoad(indices.
                stream().
                map(id -> {
                    DynamoDBEventSearchIndex dynamoDBEventSearchIndex = new DynamoDBEventSearchIndex();
                    dynamoDBEventSearchIndex.setId(id);
                    return dynamoDBEventSearchIndex;
                }).
                collect(Collectors.toList()));
        if (nonNull(batchResult) && !batchResult.isEmpty()) {
            batchResult.
                    entrySet().
                    iterator().
                    next().
                    getValue().
                    forEach(o -> eventIds.addAll(((DynamoDBEventSearchIndex) o).getEvents()));
        }
        return eventIds;
    }

    private Set<String> generateIndices(Event event) {
        Set<String> indices = new HashSet<>();

        if (nonNull(event.getType())) {
            indices.add(generateEventTypeIndexKey(event));
        }
        if (nonNull(event.getProperties()) && !event.getProperties().isEmpty()) {
            indices.addAll(event.getProperties().
                    entrySet().
                    stream().
                    map(kv -> generateIndexKey(kv.getKey(), kv.getValue())).
                    collect(Collectors.toSet()));
        }
        return indices;
    }

    private String generateEventTypeIndexKey(Event ev) {
        return generateEventTypeIndexKey(ev.getType().name());
    }

    private String generateEventTypeIndexKey(String type) {
        return generateIndexKey("type", type);
    }

    private String generateIndexKey(String propName, String propValue) {
        return propName + ":" + propValue;
    }

}
