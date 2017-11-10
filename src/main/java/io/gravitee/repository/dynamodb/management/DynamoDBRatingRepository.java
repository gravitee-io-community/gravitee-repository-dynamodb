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
import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import io.gravitee.common.data.domain.Page;
import io.gravitee.repository.dynamodb.management.model.DynamoDBRating;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.RatingRepository;
import io.gravitee.repository.management.api.search.Pageable;
import io.gravitee.repository.management.model.Rating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * @author Azize ELAMRANI (azize.elamrani at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public class DynamoDBRatingRepository implements RatingRepository {

    @Autowired
    private DynamoDBMapper mapper;

    @Override
    public Page<Rating> findByApiPageable(String api, Pageable pageable) throws TechnicalException {
        final DynamoDBRating dynamoDBRating = new DynamoDBRating();
        dynamoDBRating.setApi(api);
        final QueryResultPage<DynamoDBRating> resultPage =
                mapper.queryPage(DynamoDBRating.class, new DynamoDBQueryExpression<DynamoDBRating>().
                withConsistentRead(false).
                withHashKeyValues(dynamoDBRating));

        final int limit = pageable.pageNumber() * pageable.pageSize();
        final List<Rating> ratings = resultPage.getResults().
                stream().
                sorted(comparing(DynamoDBRating::getCreatedAt).reversed()).
                skip(limit - pageable.pageSize()).
                limit(limit).
                map(this::convert).
                collect(toList());

        return new Page<>(ratings, pageable.pageNumber(), ratings.size(), resultPage.getResults().size());
    }

    @Override
    public List<Rating> findByApi(String api) throws TechnicalException {
        final DynamoDBRating dynamoDBRating = new DynamoDBRating();
        dynamoDBRating.setApi(api);
        return mapper.query(DynamoDBRating.class, new DynamoDBQueryExpression<DynamoDBRating>().
                withConsistentRead(false).
                withHashKeyValues(dynamoDBRating)).
                stream().
                map(this::convert).
                collect(toList());
    }

    @Override
    public Optional<Rating> findByApiAndUser(String api, String user) throws TechnicalException {
        final DynamoDBRating dynamoDBRating = new DynamoDBRating();
        dynamoDBRating.setApi(api);
        dynamoDBRating.setUser(user);
        return mapper.query(DynamoDBRating.class, new DynamoDBQueryExpression<DynamoDBRating>().
                withConsistentRead(false).
                withHashKeyValues(dynamoDBRating).
                withRangeKeyCondition("user", new Condition().
                        withComparisonOperator(ComparisonOperator.EQ).
                        withAttributeValueList(new AttributeValue().
                                withS(user)))).
                stream().
                map(this::convert).
                findFirst();
    }

    @Override
    public Optional<Rating> findById(String id) throws TechnicalException {
        DynamoDBRating load = mapper.load(DynamoDBRating.class, id);
        return Optional.ofNullable(convert(load));
    }

    @Override
    public Rating create(Rating rating) throws TechnicalException {
        if (rating == null) {
            throw new IllegalArgumentException("Trying to create null");
        }
        mapper.save(
                convert(rating),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().withExists(false)
                )
        );

        return rating;
    }

    @Override
    public Rating update(Rating rating) throws TechnicalException {
        if (rating == null || rating.getId() == null) {
            throw new IllegalStateException("Rating to update must have a id");
        }

        if (!findById(rating.getId()).isPresent()) {
            throw new IllegalStateException(String.format("No rating found with id [%s]", rating.getId()));
        }
        mapper.save(
                convert(rating),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().
                                withValue(new AttributeValue().withS(rating.getId())).
                                withExists(true)
                )
        );
        return rating;
    }

    @Override
    public void delete(String id) throws TechnicalException {
        if (id == null) {
            throw new IllegalArgumentException("Trying to delete null");
        }
        DynamoDBRating rating = new DynamoDBRating();
        rating.setId(id);
        mapper.delete(rating);
    }

    private Rating convert(final DynamoDBRating dynamoDBRating) {
        if (dynamoDBRating == null) {
            return null;
        }
        final Rating rating = new Rating();
        rating.setId(dynamoDBRating.getId());
        rating.setApi(dynamoDBRating.getApi());
        rating.setUser(dynamoDBRating.getUser());
        rating.setRate(dynamoDBRating.getRate());
        rating.setTitle(dynamoDBRating.getTitle());
        rating.setComment(dynamoDBRating.getComment());
        if (dynamoDBRating.getCreatedAt() > 0) {
            rating.setCreatedAt(new Date(dynamoDBRating.getCreatedAt()));
        }
        if (dynamoDBRating.getUpdatedAt() > 0) {
            rating.setUpdatedAt(new Date(dynamoDBRating.getUpdatedAt()));
        }
        return rating;
    }

    private DynamoDBRating convert(final Rating rating) {
        if (rating == null) {
            return null;
        }
        final DynamoDBRating dynamoDBRating = new DynamoDBRating();
        dynamoDBRating.setId(rating.getId());
        dynamoDBRating.setApi(rating.getApi());
        dynamoDBRating.setUser(rating.getUser());
        dynamoDBRating.setRate(rating.getRate());
        dynamoDBRating.setTitle(rating.getTitle());
        dynamoDBRating.setComment(rating.getComment());
        if (rating.getCreatedAt() != null) {
            dynamoDBRating.setCreatedAt(rating.getCreatedAt().getTime());
        }
        if (rating.getUpdatedAt() != null) {
            dynamoDBRating.setUpdatedAt(rating.getUpdatedAt().getTime());
        }
        return dynamoDBRating;
    }
}
