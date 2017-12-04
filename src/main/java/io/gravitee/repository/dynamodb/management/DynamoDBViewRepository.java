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
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import io.gravitee.repository.dynamodb.management.model.DynamoDBView;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.ViewRepository;
import io.gravitee.repository.management.model.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@Repository
public class DynamoDBViewRepository implements ViewRepository {

    private final Logger LOGGER = LoggerFactory.getLogger(DynamoDBViewRepository.class);

    @Autowired
    private DynamoDBMapper mapper;

    @Override
    public Set<View> findAll() throws TechnicalException {
        PaginatedScanList<DynamoDBView> dynamoDBViews = mapper.scan(DynamoDBView.class, new DynamoDBScanExpression());
        return dynamoDBViews.stream().map(this::convert).collect(Collectors.toSet());
    }

    @Override
    public Optional<View> findById(String id) throws TechnicalException {
        DynamoDBView load = mapper.load(DynamoDBView.class, id);
        return Optional.ofNullable(convert(load));
    }

    @Override
    public View create(View view) throws TechnicalException {
        if (view == null) {
            throw new IllegalArgumentException("Trying to create null");
        }
        mapper.save(
                convert(view),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().withExists(false)
                )
        );
        return view;
    }

    @Override
    public View update(View view) throws TechnicalException {
        if (view == null || view.getName() == null) {
            throw new IllegalStateException("View to update must have a name");
        }

        if (!findById(view.getId()).isPresent()) {
            throw new IllegalStateException(String.format("No view found with name [%s]", view.getId()));
        }
        try {
            mapper.save(
                    convert(view),
                    new DynamoDBSaveExpression().withExpectedEntry(
                            "id",
                            new ExpectedAttributeValue().
                                    withValue(new AttributeValue().withS(view.getId())).
                                    withExists(true)
                    )
            );
            return view;
        } catch (ConditionalCheckFailedException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void delete(String id) throws TechnicalException {
        if (id == null) {
            throw new IllegalArgumentException("Trying to delete null");
        }
        DynamoDBView view = new DynamoDBView();
        view.setId(id);
        mapper.delete(view);
    }

    private View convert(final DynamoDBView dynamoDBView) {
        if (dynamoDBView == null) {
            return null;
        }
        final View view = new View();
        view.setId(dynamoDBView.getId());
        view.setName(dynamoDBView.getName());
        view.setDescription(dynamoDBView.getDescription());
        view.setDefaultView(dynamoDBView.isDefaultView());
        view.setHidden(dynamoDBView.isHidden());
        view.setOrder(dynamoDBView.getOrder());
        view.setUpdatedAt(new Date(dynamoDBView.getUpdatedAt()));
        view.setCreatedAt(new Date(dynamoDBView.getCreatedAt()));
        return view;
    }

    private DynamoDBView convert(final View view) {
        if (view == null) {
            return null;
        }
        final DynamoDBView dynamoDBView = new DynamoDBView();
        dynamoDBView.setId(view.getId());
        dynamoDBView.setName(view.getName());
        dynamoDBView.setDescription(view.getDescription());
        dynamoDBView.setDefaultView(view.isDefaultView());
        dynamoDBView.setHidden(view.isHidden());
        dynamoDBView.setOrder(view.getOrder());
        dynamoDBView.setUpdatedAt(view.getUpdatedAt().getTime());
        dynamoDBView.setCreatedAt(view.getCreatedAt().getTime());
        return dynamoDBView;
    }
}
