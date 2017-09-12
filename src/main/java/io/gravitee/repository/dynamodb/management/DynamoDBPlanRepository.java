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
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import io.gravitee.repository.dynamodb.management.model.DynamoDBPlan;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.PlanRepository;
import io.gravitee.repository.management.model.Plan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.amazonaws.services.dynamodbv2.model.ComparisonOperator.CONTAINS;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@Repository
public class DynamoDBPlanRepository implements PlanRepository {

    private final Logger LOGGER = LoggerFactory.getLogger(DynamoDBPlanRepository.class);

    @Autowired
    private DynamoDBMapper mapper;

    @Override
    public Set<Plan> findByApi(String apiId) throws TechnicalException {
        return mapper.scan(
                DynamoDBPlan.class,
                new DynamoDBScanExpression().
                        withScanFilter(Collections.singletonMap(
                                "apis", new Condition().
                                        withComparisonOperator(CONTAINS).
                                        withAttributeValueList(new AttributeValue().withS(apiId))))
        ).stream().
                map(this::convert).
                collect(Collectors.toSet());
    }

    @Override
    public Optional<Plan> findById(String id) throws TechnicalException {
        DynamoDBPlan load = mapper.load(DynamoDBPlan.class, id);
        return Optional.ofNullable(convert(load));
    }

    @Override
    public Plan create(Plan plan) throws TechnicalException {
        if (plan == null) {
            throw new IllegalArgumentException("Trying to create null");
        }
        mapper.save(
                convert(plan),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().withExists(false)
                )
        );
        return plan;
    }

    @Override
    public Plan update(Plan plan) throws TechnicalException {
        if (plan == null || plan.getId() == null) {
            throw new IllegalStateException("Plan to update must have an id");
        }

        if (!findById(plan.getId()).isPresent()) {
            throw new IllegalStateException(String.format("No plan found with id [%s]", plan.getId()));
        }
        mapper.save(
                convert(plan),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().
                                withValue(new AttributeValue().withS(plan.getId())).
                                withExists(true)
                )
        );
        return plan;
    }

    @Override
    public void delete(String id) throws TechnicalException {
        if (id == null) {
            throw new IllegalArgumentException("Trying to delete null");
        }
        DynamoDBPlan plan = new DynamoDBPlan();
        plan.setId(id);
        mapper.delete(plan);
    }

    private Plan convert(DynamoDBPlan dynamoDBPlan) {
        if (dynamoDBPlan == null) {
            return null;
        }

        Plan plan = new Plan();
        plan.setId(dynamoDBPlan.getId());
        plan.setName(dynamoDBPlan.getName());
        plan.setOrder(dynamoDBPlan.getOrder());

        if (dynamoDBPlan.getType() != null) {
            plan.setType(Plan.PlanType.valueOf(dynamoDBPlan.getType()));
        }

        plan.setCharacteristics(dynamoDBPlan.getCharacteristics());
        plan.setApis(dynamoDBPlan.getApis());
        plan.setDescription(dynamoDBPlan.getDescription());
        plan.setDefinition(dynamoDBPlan.getDefinition());

        if (dynamoDBPlan.getValidation() != null) {
            plan.setValidation(Plan.PlanValidationType.valueOf(dynamoDBPlan.getValidation()));
        }

        if (dynamoDBPlan.getCreatedAt() != 0) {
            plan.setCreatedAt(new Date(dynamoDBPlan.getCreatedAt()));
        }

        if (dynamoDBPlan.getUpdatedAt() != 0) {
            plan.setUpdatedAt(new Date(dynamoDBPlan.getUpdatedAt()));
        }

        if (dynamoDBPlan.getStatus() != null) {
            plan.setStatus(Plan.Status.valueOf(dynamoDBPlan.getStatus()));
        }

        if (dynamoDBPlan.getSecurity() != null) {
            plan.setSecurity(Plan.PlanSecurityType.valueOf(dynamoDBPlan.getSecurity()));
        }

        if (dynamoDBPlan.getPublishedAt() != 0) {
            plan.setPublishedAt(new Date(dynamoDBPlan.getPublishedAt()));
        }

        if (dynamoDBPlan.getClosedAt() != 0) {
            plan.setClosedAt(new Date(dynamoDBPlan.getClosedAt()));
        }
        return plan;
    }

    private DynamoDBPlan convert(Plan plan) {
        DynamoDBPlan dynamoDBPlan = new DynamoDBPlan();
        dynamoDBPlan.setId(plan.getId());
        dynamoDBPlan.setName(plan.getName());
        dynamoDBPlan.setOrder(dynamoDBPlan.getOrder());

        if (plan.getType() != null) {
            dynamoDBPlan.setType(plan.getType().name());
        }

        if (plan.getCharacteristics() != null && !plan.getCharacteristics().isEmpty()) {
            dynamoDBPlan.setCharacteristics(plan.getCharacteristics());
        }

        if (plan.getApis() != null && !plan.getApis().isEmpty()) {
            dynamoDBPlan.setApis(plan.getApis());
        }
        dynamoDBPlan.setDescription(plan.getDescription());
        dynamoDBPlan.setDefinition(plan.getDefinition());

        if (plan.getValidation() != null) {
            dynamoDBPlan.setValidation(plan.getValidation().name());
        }

        if (plan.getCreatedAt() != null) {
            dynamoDBPlan.setCreatedAt(plan.getCreatedAt().getTime());
        }

        if (plan.getUpdatedAt() != null) {
            dynamoDBPlan.setUpdatedAt(plan.getUpdatedAt().getTime());
        }

        if (plan.getStatus() != null) {
            dynamoDBPlan.setStatus(plan.getStatus().name());
        }

        if (plan.getSecurity() != null) {
            dynamoDBPlan.setSecurity(plan.getSecurity().name());
        }

        if (plan.getClosedAt() != null) {
            dynamoDBPlan.setClosedAt(plan.getClosedAt().getTime());
        }

        if (plan.getPublishedAt() != null) {
            dynamoDBPlan.setPublishedAt(plan.getPublishedAt().getTime());
        }

        return dynamoDBPlan;
    }
}
