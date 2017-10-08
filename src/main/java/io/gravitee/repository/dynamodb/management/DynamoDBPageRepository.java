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
import com.amazonaws.util.ImmutableMapParameter;
import io.gravitee.repository.dynamodb.management.model.DynamoDBPage;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.PageRepository;
import io.gravitee.repository.management.model.Page;
import io.gravitee.repository.management.model.PageConfiguration;
import io.gravitee.repository.management.model.PageSource;
import io.gravitee.repository.management.model.PageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@Repository
public class DynamoDBPageRepository implements PageRepository {

    private final Logger LOGGER = LoggerFactory.getLogger(DynamoDBPageRepository.class);

    @Autowired
    private DynamoDBMapper mapper;

    @Override
    public Collection<Page> findApiPageByApiIdAndHomepage(String apiId, boolean homepage) throws TechnicalException {
        return mapper.scan(
                DynamoDBPage.class,
                new DynamoDBScanExpression().
                        withFilterExpression("api = :a and homepage = :h").
                        withExpressionAttributeValues(ImmutableMapParameter.of(
                                ":a", new AttributeValue().withS(apiId),
                                ":h", new AttributeValue().withBOOL(homepage)))
        ).stream().
                map(this::convert).
                collect(Collectors.toSet());
    }

    @Override
    public Collection<Page> findApiPageByApiId(String apiId) throws TechnicalException {
        return mapper.scan(
                DynamoDBPage.class,
                new DynamoDBScanExpression().
                        withFilterExpression("api = :a").
                        withExpressionAttributeValues(Collections.singletonMap(
                                ":a", new AttributeValue().withS(apiId)))
        ).stream().
                map(this::convert).
                collect(Collectors.toSet());
    }

    @Override
    public Integer findMaxApiPageOrderByApiId(String apiId) throws TechnicalException {
        Optional<Integer> first = findApiPageByApiId(apiId).stream().map(Page::getOrder).max(Integer::compare);
        return first.orElse(0);
    }

    @Override
    public Collection<Page> findPortalPageByHomepage(boolean homepage) throws TechnicalException {
        return mapper.scan(
                DynamoDBPage.class,
                new DynamoDBScanExpression().
                        withFilterExpression("attribute_not_exists(api) and homepage = :h").
                        withExpressionAttributeValues(Collections.singletonMap(
                                ":h", new AttributeValue().withBOOL(homepage)))
        ).stream().
                map(this::convert).
                collect(Collectors.toSet());
    }

    @Override
    public Collection<Page> findPortalPages() throws TechnicalException {
        return mapper.scan(
                DynamoDBPage.class,
                new DynamoDBScanExpression().
                        withFilterExpression("attribute_not_exists(api)")
        ).stream().
                map(this::convert).
                collect(Collectors.toSet());
    }

    @Override
    public Integer findMaxPortalPageOrder() throws TechnicalException {
        Optional<Integer> first = findPortalPages().stream().map(Page::getOrder).max(Integer::compare);
        return first.orElse(0);
    }

    @Override
    public Optional<Page> findById(String id) throws TechnicalException {
        DynamoDBPage load = mapper.load(DynamoDBPage.class, id);
        return Optional.ofNullable(convert(load));
    }

    @Override
    public Page create(Page page) throws TechnicalException {
        if (page == null) {
            throw new IllegalArgumentException("Trying to create null");
        }
        mapper.save(
                convert(page),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().withExists(false)
                )
        );
        return page;
    }

    @Override
    public Page update(Page page) throws TechnicalException {
        if (page == null) {
            throw new IllegalStateException("Page must not be null");
        }

        if (!findById(page.getId()).isPresent()) {
            throw new IllegalStateException(String.format("No page found with id [%s]", page.getId()));
        }
        mapper.save(
                convert(page),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().
                                withValue(new AttributeValue().withS(page.getId())).
                                withExists(true)
                )
        );
        return page;
    }

    @Override
    public void delete(String id) throws TechnicalException {
        if (id == null) {
            throw new IllegalArgumentException("Trying to delete null");
        }
        DynamoDBPage page = new DynamoDBPage();
        page.setId(id);
        mapper.delete(page);
    }

    private Page convert(DynamoDBPage dynamoDBPage) {
        if (dynamoDBPage == null) {
            return null;
        }
        Page page = new Page();
        page.setId(dynamoDBPage.getId());
        page.setApi(dynamoDBPage.getApi());
        page.setContent(dynamoDBPage.getContent());
        page.setCreatedAt(new Date(dynamoDBPage.getCreatedAt()));
        page.setUpdatedAt(new Date(dynamoDBPage.getUpdatedAt()));
        page.setLastContributor(dynamoDBPage.getLastContributor());
        page.setName(dynamoDBPage.getName());
        page.setOrder(dynamoDBPage.getOrder());
        page.setPublished(dynamoDBPage.isPublished());
        page.setType(PageType.valueOf(dynamoDBPage.getType()));
        page.setHomepage(dynamoDBPage.isHomepage());
        page.setExcludedGroups(dynamoDBPage.getExcludedGroups());

        if (dynamoDBPage.getSourceType() != null) {
            PageSource pageSource = new PageSource();
            pageSource.setType(dynamoDBPage.getSourceType());
            pageSource.setConfiguration(dynamoDBPage.getSourceConfiguration());
            page.setSource(pageSource);
        }

        PageConfiguration configuration = new PageConfiguration();
        configuration.setTryIt(dynamoDBPage.isConfigurationTryIt());
        configuration.setTryItURL(dynamoDBPage.getConfigurationTryItURL());
        page.setConfiguration(configuration);

        return page;
    }

    private DynamoDBPage convert(Page page) {
        DynamoDBPage dynamoDBPage = new DynamoDBPage();
        dynamoDBPage.setId(page.getId());
        dynamoDBPage.setApi(page.getApi());
        dynamoDBPage.setContent(page.getContent());
        dynamoDBPage.setCreatedAt(page.getCreatedAt().getTime());
        dynamoDBPage.setUpdatedAt(page.getUpdatedAt().getTime());
        dynamoDBPage.setLastContributor(page.getLastContributor());
        dynamoDBPage.setName(page.getName());
        dynamoDBPage.setOrder(page.getOrder());
        dynamoDBPage.setPublished(page.isPublished());
        dynamoDBPage.setType(page.getType().name());
        dynamoDBPage.setHomepage(page.isHomepage());
        dynamoDBPage.setExcludedGroups(page.getExcludedGroups());

        if (page.getSource() != null) {
            dynamoDBPage.setSourceType(page.getSource().getType());
            dynamoDBPage.setSourceConfiguration(page.getSource().getConfiguration());
        }

        if (page.getConfiguration() != null) {
            dynamoDBPage.setConfigurationTryIt(page.getConfiguration().isTryIt());
            dynamoDBPage.setConfigurationTryItURL(page.getConfiguration().getTryItURL());
        }
        return dynamoDBPage;
    }
}
