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
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import io.gravitee.repository.dynamodb.management.model.DynamoDBSubscription;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.SubscriptionRepository;
import io.gravitee.repository.management.model.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@Repository
public class DynamoDBSubscriptionRepository implements SubscriptionRepository {

    private final Logger LOGGER = LoggerFactory.getLogger(DynamoDBSubscriptionRepository.class);

    @Autowired
    private DynamoDBMapper mapper;

    @Override
    public Set<Subscription> findByPlan(String planId) throws TechnicalException {
        return mapper.scan(
                DynamoDBSubscription.class,
                new DynamoDBScanExpression().
                        withFilterExpression("#p = :p").
                        withExpressionAttributeValues(Collections.singletonMap(":p", new AttributeValue().withS(planId))).
                        withExpressionAttributeNames(Collections.singletonMap("#p", "plan"))
        ).stream().
                map(this::convert).
                collect(Collectors.toSet());
    }

    @Override
    public Set<Subscription> findByApplication(String applicationId) throws TechnicalException {
        return mapper.scan(
                DynamoDBSubscription.class,
                new DynamoDBScanExpression().
                        withFilterExpression("application = :a").
                        withExpressionAttributeValues(Collections.singletonMap(":a", new AttributeValue().withS(applicationId)))
        ).stream().
                map(this::convert).
                collect(Collectors.toSet());
    }

    @Override
    public Optional<Subscription> findById(String id) throws TechnicalException {
        DynamoDBSubscription load = mapper.load(DynamoDBSubscription.class, id);
        return Optional.ofNullable(convert(load));
    }

    @Override
    public Subscription create(Subscription subscription) throws TechnicalException {
        if (subscription == null) {
            throw new IllegalArgumentException("Trying to create null");
        }
        mapper.save(
                convert(subscription),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().withExists(false)
                )
        );
        return subscription;
    }

    @Override
    public Subscription update(Subscription subscription) throws TechnicalException {
        if (subscription == null || subscription.getId() == null) {
            throw new IllegalStateException("Subscription to update must have an id");
        }

        if (!findById(subscription.getId()).isPresent()) {
            throw new IllegalStateException(String.format("No subscription found with id [%s]", subscription.getId()));
        }
        mapper.save(
                convert(subscription),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().
                                withValue(new AttributeValue().withS(subscription.getId())).
                                withExists(true)
                )
        );
        return subscription;
    }

    @Override
    public void delete(String id) throws TechnicalException {
        if (id == null) {
            throw new IllegalArgumentException("Trying to delete null");
        }
        DynamoDBSubscription subscription = new DynamoDBSubscription();
        subscription.setId(id);
        mapper.delete(subscription);
    }

    private Subscription convert(DynamoDBSubscription dynamoDBSubscription) {
        if (dynamoDBSubscription == null) {
            return null;
        }

        Subscription subscription = new Subscription();
        subscription.setId(dynamoDBSubscription.getId());
        subscription.setStatus(Subscription.Status.valueOf(dynamoDBSubscription.getStatus()));
        subscription.setApplication(dynamoDBSubscription.getApplication());
        subscription.setPlan(dynamoDBSubscription.getPlan());
        subscription.setProcessedBy(dynamoDBSubscription.getProcessedBy());
        subscription.setReason(dynamoDBSubscription.getReason());
        subscription.setSubscribedBy(dynamoDBSubscription.getSubscribedBy());

        if (dynamoDBSubscription.getProcessedAt() != 0) {
            subscription.setProcessedAt(new Date(dynamoDBSubscription.getProcessedAt()));
        }
        if (dynamoDBSubscription.getStartingAt() != 0) {
            subscription.setStartingAt(new Date(dynamoDBSubscription.getStartingAt()));
        }
        if (dynamoDBSubscription.getEndingAt() != 0) {
            subscription.setEndingAt(new Date(dynamoDBSubscription.getEndingAt()));
        }

        subscription.setCreatedAt(new Date(dynamoDBSubscription.getCreatedAt()));
        subscription.setUpdatedAt(new Date(dynamoDBSubscription.getUpdatedAt()));

        if (dynamoDBSubscription.getClosedAt() != 0) {
            subscription.setClosedAt(new Date(dynamoDBSubscription.getClosedAt()));
        }

        return subscription;
    }

    private DynamoDBSubscription convert(Subscription subscription) {
        DynamoDBSubscription dynamoDBSubscription = new DynamoDBSubscription();
        dynamoDBSubscription.setId(subscription.getId());
        dynamoDBSubscription.setStatus(subscription.getStatus().name());
        dynamoDBSubscription.setReason(subscription.getReason());
        dynamoDBSubscription.setProcessedBy(subscription.getProcessedBy());
        dynamoDBSubscription.setApplication(subscription.getApplication());
        dynamoDBSubscription.setPlan(subscription.getPlan());
        dynamoDBSubscription.setSubscribedBy(subscription.getSubscribedBy());

        if (subscription.getProcessedAt() != null) {
            dynamoDBSubscription.setProcessedAt(subscription.getProcessedAt().getTime());
        }
        if (subscription.getStartingAt() != null) {
            dynamoDBSubscription.setStartingAt(subscription.getStartingAt().getTime());
        }
        if (subscription.getEndingAt() != null) {
            dynamoDBSubscription.setEndingAt(subscription.getEndingAt().getTime());
        }
        dynamoDBSubscription.setCreatedAt(subscription.getCreatedAt().getTime());
        dynamoDBSubscription.setUpdatedAt(subscription.getUpdatedAt().getTime());

        if (subscription.getClosedAt() != null) {
            dynamoDBSubscription.setClosedAt(subscription.getClosedAt().getTime());
        }

        return dynamoDBSubscription;
    }
}
