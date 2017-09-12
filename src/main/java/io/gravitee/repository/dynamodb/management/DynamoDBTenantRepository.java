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
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import io.gravitee.repository.dynamodb.management.model.DynamoDBTenant;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.TenantRepository;
import io.gravitee.repository.management.model.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@Repository
public class DynamoDBTenantRepository implements TenantRepository {

    private final Logger LOGGER = LoggerFactory.getLogger(DynamoDBTenantRepository.class);

    @Autowired
    private DynamoDBMapper mapper;

    @Override
    public Set<Tenant> findAll() throws TechnicalException {
        PaginatedScanList<DynamoDBTenant> dynamoDBTenants = mapper.scan(DynamoDBTenant.class, new DynamoDBScanExpression());
        return dynamoDBTenants.stream().map(this::convert).collect(Collectors.toSet());
    }

    @Override
    public Optional<Tenant> findById(String id) throws TechnicalException {
        DynamoDBTenant load = mapper.load(DynamoDBTenant.class, id);
        return Optional.ofNullable(convert(load));
    }

    @Override
    public Tenant create(Tenant tenant) throws TechnicalException {
        if (tenant == null) {
            throw new IllegalArgumentException("Trying to create null");
        }
        mapper.save(
                convert(tenant),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().withExists(false)
                )
        );
        return tenant;
    }

    @Override
    public Tenant update(Tenant tenant) throws TechnicalException {
        if (tenant == null || tenant.getName() == null) {
            throw new IllegalStateException("Tenant to update must have a name");
        }

        if (!findById(tenant.getId()).isPresent()) {
            throw new IllegalStateException(String.format("No tenant found with name [%s]", tenant.getId()));
        }
        mapper.save(
                convert(tenant),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().
                                withValue(new AttributeValue().withS(tenant.getId())).
                                withExists(true)
                )
        );
        return tenant;
    }

    @Override
    public void delete(String id) throws TechnicalException {
        if (id == null) {
            throw new IllegalArgumentException("Trying to delete null");
        }
        DynamoDBTenant tenant = new DynamoDBTenant();
        tenant.setId(id);
        mapper.delete(tenant);
    }

    private Tenant convert(final DynamoDBTenant dynamoDBTenant) {
        if (dynamoDBTenant == null) {
            return null;
        }
        final Tenant tenant = new Tenant();
        tenant.setId(dynamoDBTenant.getId());
        tenant.setName(dynamoDBTenant.getName());
        tenant.setDescription(dynamoDBTenant.getDescription());
        return tenant;
    }

    private DynamoDBTenant convert(final Tenant tenant) {
        if (tenant == null) {
            return null;
        }
        final DynamoDBTenant dynamoDBTenant = new DynamoDBTenant();
        dynamoDBTenant.setId(tenant.getId());
        dynamoDBTenant.setName(tenant.getName());
        dynamoDBTenant.setDescription(tenant.getDescription());
        return dynamoDBTenant;
    }
}
