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
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import io.gravitee.repository.dynamodb.management.model.DynamoDBRatingAnswer;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.RatingAnswerRepository;
import io.gravitee.repository.management.model.RatingAnswer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * @author Azize ELAMRANI (azize.elamrani at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public class DynamoDBRatingAnswerRepository implements RatingAnswerRepository {

    private final Logger LOGGER = LoggerFactory.getLogger(DynamoDBRatingAnswerRepository.class);

    @Autowired
    private DynamoDBMapper mapper;

    @Override
    public RatingAnswer create(RatingAnswer ratingAnswer) throws TechnicalException {
        if (ratingAnswer == null) {
            throw new IllegalArgumentException("Trying to create null");
        }
        mapper.save(
                convert(ratingAnswer),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().withExists(false)
                )
        );
        return ratingAnswer;
    }

    @Override
    public List<RatingAnswer> findByRating(String rating) throws TechnicalException {
        final DynamoDBRatingAnswer dynamoDBRatingAnswer = new DynamoDBRatingAnswer();
        dynamoDBRatingAnswer.setRating(rating);
        return mapper.query(DynamoDBRatingAnswer.class, new DynamoDBQueryExpression<DynamoDBRatingAnswer>().
                withConsistentRead(false).
                withHashKeyValues(dynamoDBRatingAnswer)).
                stream().
                map(this::convert).
                collect(toList());
    }

    @Override
    public Optional<RatingAnswer> findById(String id) throws TechnicalException {
        final DynamoDBRatingAnswer dynamoDBRatingAnswer = new DynamoDBRatingAnswer();
        dynamoDBRatingAnswer.setId(id);
        return mapper.query(DynamoDBRatingAnswer.class, new DynamoDBQueryExpression<DynamoDBRatingAnswer>().
                withConsistentRead(false).
                withHashKeyValues(dynamoDBRatingAnswer)).
                stream().
                map(this::convert).
                findFirst();
    }

    @Override
    public RatingAnswer update(RatingAnswer ratingAnswer) throws TechnicalException {
        if (ratingAnswer == null || ratingAnswer.getId() == null) {
            throw new IllegalStateException("Rating to update must have a id");
        }

        if (!findById(ratingAnswer.getId()).isPresent()) {
            throw new IllegalStateException(String.format("No rating found with id [%s]", ratingAnswer.getId()));
        }
        mapper.save(
                convert(ratingAnswer),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().
                                withValue(new AttributeValue().withS(ratingAnswer.getId())).
                                withExists(true)
                )
        );
        return ratingAnswer;
    }

    @Override
    public void delete(String id) throws TechnicalException {
        if (id == null) {
            throw new IllegalArgumentException("Trying to delete null");
        }
        DynamoDBRatingAnswer ratingAnswer = new DynamoDBRatingAnswer();
        ratingAnswer.setId(id);
        mapper.delete(ratingAnswer);
    }

    private DynamoDBRatingAnswer convert(final RatingAnswer ratingAnswer) {
        final DynamoDBRatingAnswer dynamoDBRatingAnswer = new DynamoDBRatingAnswer();
        dynamoDBRatingAnswer.setId(ratingAnswer.getId());
        dynamoDBRatingAnswer.setRating(ratingAnswer.getRating());
        dynamoDBRatingAnswer.setUser(ratingAnswer.getUser());
        dynamoDBRatingAnswer.setComment(ratingAnswer.getComment());
        if (ratingAnswer.getCreatedAt() != null) {
            dynamoDBRatingAnswer.setCreatedAt(ratingAnswer.getCreatedAt().getTime());
        }
        return dynamoDBRatingAnswer;
    }

    private RatingAnswer convert(final DynamoDBRatingAnswer dynamoDBRatingAnswer) {
        final RatingAnswer ratingAnswer = new RatingAnswer();
        ratingAnswer.setId(dynamoDBRatingAnswer.getId());
        ratingAnswer.setRating(dynamoDBRatingAnswer.getRating());
        ratingAnswer.setUser(dynamoDBRatingAnswer.getUser());
        ratingAnswer.setComment(dynamoDBRatingAnswer.getComment());
        if (dynamoDBRatingAnswer.getCreatedAt() > 0) {
            ratingAnswer.setCreatedAt(new Date(dynamoDBRatingAnswer.getCreatedAt()));
        }
        return ratingAnswer;
    }
}
