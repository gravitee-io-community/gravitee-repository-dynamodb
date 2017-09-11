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
import io.gravitee.repository.dynamodb.management.model.DynamoDBApplication;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.ApplicationRepository;
import io.gravitee.repository.management.model.Application;
import io.gravitee.repository.management.model.ApplicationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@Repository
public class DynamoDBApplicationRepository implements ApplicationRepository {

    @Autowired
    private DynamoDBMapper mapper;

    @Override
    public Set<Application> findAll(ApplicationStatus... applicationStatuses) throws TechnicalException {
        if (applicationStatuses !=null && applicationStatuses.length > 0) {
            Set<Application> result = new HashSet<>();
            for (ApplicationStatus applicationStatus : applicationStatuses) {
                DynamoDBApplication dynamoDBApplication = new DynamoDBApplication();
                dynamoDBApplication.setStatus(applicationStatus.name());
                result.addAll(mapper.query(DynamoDBApplication.class, new DynamoDBQueryExpression<DynamoDBApplication>().
                        withConsistentRead(false).
                        withHashKeyValues(dynamoDBApplication)).
                        stream().
                        map(this::convert).
                        collect(Collectors.toSet()));
            }
            return result;
        } else {
            PaginatedScanList<DynamoDBApplication> dynamoDBApplications = mapper.scan(DynamoDBApplication.class, new DynamoDBScanExpression());
            return dynamoDBApplications.stream().map(this::convert).collect(Collectors.toSet());
        }
    }

    @Override
    public Set<Application> findByIds(List<String> ids) throws TechnicalException {
        Map<String, List<Object>> result = mapper.batchLoad(ids.stream().map(id -> {
            DynamoDBApplication app = new DynamoDBApplication();
            app.setId(id);
            return app;
        }).collect(Collectors.toSet()));

        if (result != null && !result.isEmpty()) {
            List<Object> apps = result.entrySet().iterator().next().getValue();
            return apps.stream().map(o -> convert((DynamoDBApplication)o)).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    @Override
    public Set<Application> findByGroups(List<String> groupIds, ApplicationStatus... applicationStatuses) throws TechnicalException {
        if (groupIds !=null && !groupIds.isEmpty()) {
            final List<String> status =
                    applicationStatuses == null ?
                    new ArrayList<>() :
                    Arrays.stream(applicationStatuses).map(Enum::name).collect(Collectors.toList());
            Set<Application> result = new HashSet<>();
            for (String groupId : groupIds) {
                DynamoDBApplication dynamoDBApplication = new DynamoDBApplication();
                dynamoDBApplication.setGroup(groupId);
                result.addAll(mapper.query(DynamoDBApplication.class, new DynamoDBQueryExpression<DynamoDBApplication>().
                        withConsistentRead(false).
                        withHashKeyValues(dynamoDBApplication)).
                        stream().
                        filter(app -> status.isEmpty() || status.contains(app.getStatus())).
                        map(this::convert).
                        collect(Collectors.toSet()));
            }
            return result;
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public Set<Application> findByName(String partialName) throws TechnicalException {
        return mapper.scan(DynamoDBApplication.class, new DynamoDBScanExpression().
                withProjectionExpression("id, #n").withExpressionAttributeNames(Collections.singletonMap("#n", "name"))).stream().
                filter(dynamoDBApplication -> dynamoDBApplication.getName().toUpperCase().contains(partialName.toUpperCase())).
                map(dynamoDBApplication -> mapper.load(DynamoDBApplication.class, dynamoDBApplication.getId()))
                .map(this::convert)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<Application> findById(String id) throws TechnicalException {
        DynamoDBApplication load = mapper.load(DynamoDBApplication.class, id);
        return Optional.ofNullable(convert(load));
    }

    @Override
    public Application create(Application application) throws TechnicalException {
        if (application == null) {
            throw new IllegalArgumentException("Trying to create null");
        }
        mapper.save(
                convert(application),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().withExists(false)
                )
        );
        return application;
    }

    @Override
    public Application update(Application application) throws TechnicalException {
        if (application == null) {
            throw new IllegalArgumentException("Trying to update null");
        }

        DynamoDBApplication oldApplication = mapper.load(DynamoDBApplication.class, application.getId());
        if(oldApplication == null) {
            throw new TechnicalException("Unknown application " + application.getId());
        }

        oldApplication.setName(application.getName());
        oldApplication.setDescription(application.getDescription());
        oldApplication.setCreatedAt(application.getCreatedAt().getTime());
        oldApplication.setUpdatedAt(application.getUpdatedAt().getTime());
        oldApplication.setType(application.getType());
        oldApplication.setGroup(application.getGroup());
        oldApplication.setStatus(application.getStatus().name());

        mapper.save(
                oldApplication,
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().
                                withValue(new AttributeValue().withS(oldApplication.getId())).
                                withExists(true)
                )
        );
        return application;
    }

    @Override
    public void delete(String id) throws TechnicalException {
        if (id == null) {
            throw new IllegalArgumentException("Trying to delete null");
        }
        DynamoDBApplication dynamoDBApplication = new DynamoDBApplication();
        dynamoDBApplication.setId(id);
        mapper.delete(dynamoDBApplication);
    }

    private Application convert(DynamoDBApplication dynamoDBApplication) {
        if (dynamoDBApplication == null) {
            return null;
        }

        Application application = new Application();

        application.setId(dynamoDBApplication.getId());
        application.setName(dynamoDBApplication.getName());
        application.setCreatedAt(new Date(dynamoDBApplication.getCreatedAt()));
        application.setUpdatedAt(new Date(dynamoDBApplication.getUpdatedAt()));
        application.setDescription(dynamoDBApplication.getDescription());
        application.setType(dynamoDBApplication.getType());
        application.setGroup(dynamoDBApplication.getGroup());
        application.setStatus(ApplicationStatus.valueOf(dynamoDBApplication.getStatus()));
        return application;
    }

    private DynamoDBApplication convert(Application application) {
        DynamoDBApplication dynamoDBApplication = new DynamoDBApplication();

        dynamoDBApplication.setId(application.getId());
        dynamoDBApplication.setName(application.getName());
        dynamoDBApplication.setCreatedAt(application.getCreatedAt().getTime());
        dynamoDBApplication.setUpdatedAt(application.getUpdatedAt().getTime());
        dynamoDBApplication.setDescription(application.getDescription());
        dynamoDBApplication.setType(application.getType());
        dynamoDBApplication.setGroup(application.getGroup());
        dynamoDBApplication.setStatus(application.getStatus().name());

        return dynamoDBApplication;
    }
}
