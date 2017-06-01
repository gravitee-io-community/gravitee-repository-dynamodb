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
import io.gravitee.repository.dynamodb.management.model.DynamoDBApi;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.ApiRepository;
import io.gravitee.repository.management.model.Api;
import io.gravitee.repository.management.model.LifecycleState;
import io.gravitee.repository.management.model.Visibility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@Repository
public class DynamoDBApiRepository implements ApiRepository {

    @Autowired
    private DynamoDBMapper mapper;

    @Override
    public Set<Api> findAll() throws TechnicalException {
        PaginatedScanList<DynamoDBApi> dynamoDBApis = mapper.scan(DynamoDBApi.class, new DynamoDBScanExpression());
        return dynamoDBApis.stream().map(this::convert).collect(Collectors.toSet());
    }

    @Override
    public Set<Api> findByVisibility(Visibility visibility) throws TechnicalException {
        DynamoDBApi dynamoDBApi = new DynamoDBApi();
        dynamoDBApi.setVisibility(visibility.name());
        return mapper.query(DynamoDBApi.class, new DynamoDBQueryExpression<DynamoDBApi>().
                withConsistentRead(false).
                withHashKeyValues(dynamoDBApi)).
                stream().
                map(this::convert).
                collect(Collectors.toSet());
    }

    @Override
    public Set<Api> findByIds(List<String> ids) throws TechnicalException {
        Map<String, List<Object>> result = mapper.batchLoad(ids.stream().map(id -> {
            DynamoDBApi api = new DynamoDBApi();
            api.setId(id);
            return api;
        }).collect(Collectors.toSet()));

        if (result != null && !result.isEmpty()) {
            List<Object> apis = result.entrySet().iterator().next().getValue();
            return apis.stream().map(o -> convert((DynamoDBApi)o)).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    @Override
    public Set<Api> findByGroups(List<String> groupIds) throws TechnicalException {
        if (groupIds !=null && !groupIds.isEmpty()) {
            Set<Api> result = new HashSet<>();
            for (String groupId : groupIds) {
                DynamoDBApi dynamoDBApi = new DynamoDBApi();
                dynamoDBApi.setGroup(groupId);
                result.addAll(mapper.query(DynamoDBApi.class, new DynamoDBQueryExpression<DynamoDBApi>().
                        withConsistentRead(false).
                        withHashKeyValues(dynamoDBApi)).
                        stream().
                        map(this::convert).
                        collect(Collectors.toSet()));
            }
            return result;
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public Optional<Api> findById(String id) throws TechnicalException {
        DynamoDBApi load = mapper.load(DynamoDBApi.class, id);
        return Optional.ofNullable(convert(load));
    }

    @Override
    public Api create(Api api) throws TechnicalException {
        if (api == null) {
            throw new IllegalArgumentException("Trying to create null");
        }

        mapper.save(
                convert(api),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().withExists(false)
                )
        );
        return api;
    }

    @Override
    public Api update(Api api) throws TechnicalException {
        if (api == null) {
            throw new IllegalArgumentException("Trying to update null");
        }
        mapper.save(
                convert(api),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().
                                withValue(new AttributeValue().withS(api.getId())).
                                withExists(true)
                )
        );
        return api;
    }

    @Override
    public void delete(String id) throws TechnicalException {
        if (id == null) {
            throw new IllegalArgumentException("Trying to delete null");
        }
        DynamoDBApi dynamoDBApi = new DynamoDBApi();
        dynamoDBApi.setId(id);
        mapper.delete(dynamoDBApi);
    }


    private Api convert(DynamoDBApi dynamoDBApi) {
        if (dynamoDBApi == null) {
            return null;
        }

        Api api = new Api();

        api.setId(dynamoDBApi.getId());
        api.setName(dynamoDBApi.getName());
        api.setCreatedAt(new Date(dynamoDBApi.getCreatedAt()));
        api.setUpdatedAt(new Date(dynamoDBApi.getUpdatedAt()));
        if (dynamoDBApi.getDeployedAt() != 0) {
            api.setDeployedAt(new Date(dynamoDBApi.getDeployedAt()));
        }
        api.setDefinition(dynamoDBApi.getDefinition());
        api.setDescription(dynamoDBApi.getDescription());
        api.setVersion(dynamoDBApi.getVersion());
        api.setVisibility(Visibility.valueOf(dynamoDBApi.getVisibility()));
        api.setLifecycleState(LifecycleState.valueOf(dynamoDBApi.getLifecycleState()));
        api.setPicture(dynamoDBApi.getPicture());
        api.setGroup(dynamoDBApi.getGroup());
        api.setViews(dynamoDBApi.getViews());
        api.setLabels(dynamoDBApi.getLabels());

        return api;
    }

    private DynamoDBApi convert(Api api) {
        DynamoDBApi dynamoDBApi = new DynamoDBApi();

        dynamoDBApi.setId(api.getId());
        dynamoDBApi.setName(api.getName());
        dynamoDBApi.setCreatedAt(api.getCreatedAt().getTime());
        dynamoDBApi.setUpdatedAt(api.getUpdatedAt().getTime());

        if (api.getDeployedAt() != null) {
            dynamoDBApi.setDeployedAt(api.getDeployedAt().getTime());
        }

        dynamoDBApi.setDefinition(api.getDefinition());
        dynamoDBApi.setDescription(api.getDescription());
        dynamoDBApi.setVersion(api.getVersion());
        dynamoDBApi.setVisibility(api.getVisibility().name());
        dynamoDBApi.setLifecycleState(api.getLifecycleState().name());
        dynamoDBApi.setPicture(api.getPicture());
        dynamoDBApi.setGroup(api.getGroup());
        if (api.getViews() != null && !api.getViews().isEmpty()) {
            dynamoDBApi.setViews(api.getViews());
        }
        if (api.getLabels() != null && !api.getLabels().isEmpty()) {
            dynamoDBApi.setLabels(api.getLabels());
        }

        return dynamoDBApi;
    }
}
