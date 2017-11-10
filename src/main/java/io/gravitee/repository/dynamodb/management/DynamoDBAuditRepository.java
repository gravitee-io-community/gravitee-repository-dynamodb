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
import com.amazonaws.services.dynamodbv2.model.*;
import io.gravitee.common.data.domain.Page;
import io.gravitee.repository.dynamodb.management.model.DynamoDBApiKey;
import io.gravitee.repository.dynamodb.management.model.DynamoDBAudit;
import io.gravitee.repository.dynamodb.management.model.DynamoDBUser;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.AuditRepository;
import io.gravitee.repository.management.api.UserRepository;
import io.gravitee.repository.management.api.search.AuditCriteria;
import io.gravitee.repository.management.api.search.Pageable;
import io.gravitee.repository.management.model.Audit;
import io.gravitee.repository.management.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
@Repository
public class DynamoDBAuditRepository implements AuditRepository {

    private final Logger LOGGER = LoggerFactory.getLogger(DynamoDBAuditRepository.class);

    @Autowired
    private DynamoDBMapper mapper;

//    @Override
//    public Page<Audit> search(AuditCriteria filter, Pageable pageable) {
//        int maxElement = pageable.pageNumber() * pageable.pageSize();
//        int count = mapper.count(DynamoDBAudit.class, new DynamoDBScanExpression());
//        int start = (pageable.pageNumber() - 1) * pageable.pageSize();
//        List<Audit> audits = mapper.
//                scan(DynamoDBAudit.class, new DynamoDBScanExpression().
//                        withLimit(maxElement)).
//                stream().
//                sorted(Comparator.comparingLong(DynamoDBAudit::getCreatedAt).reversed()).
//                collect(Collectors.toList()).
//                subList(start, Math.min(maxElement, count)).
//                stream().
//                map(this::convert).
//                collect(Collectors.toList());
//
//        return new Page<>(audits, pageable.pageNumber(), pageable.pageSize(), count);
//    }

    @Override
    public Page<Audit> search(AuditCriteria filter, Pageable pageable) {
        int maxElement = pageable.pageNumber() * pageable.pageSize();
        int start = (pageable.pageNumber() - 1) * pageable.pageSize();
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        Map<String, AttributeValue> eav = new HashMap<>();
        StringJoiner filterExpression = new StringJoiner(" and ");

        //references
        int i = 0;
        if(filter.getReferences() != null && !filter.getReferences().isEmpty()) {
            StringJoiner referenceIds = new StringJoiner(",");
            for (Map.Entry<Audit.AuditReferenceType, List<String>> auditReferenceTypeListEntry : filter.getReferences().entrySet()) {
                String refType = auditReferenceTypeListEntry.getKey().name();
                for (String refId : auditReferenceTypeListEntry.getValue()) {
                    String attr = ":ref" + (i++);
                    eav.put(attr, new AttributeValue().withS(refType + ":" + refId));
                    referenceIds.add(attr);
                }
            }
            filterExpression.add("#ref in (" + referenceIds.toString() + ")");
            scanExpression.withExpressionAttributeNames(Collections.singletonMap("#ref", "reference"));
        }

        //events
        if(filter.getEvents() != null && !filter.getEvents().isEmpty()) {
            i = 0;
            StringJoiner eventIds = new StringJoiner(",");
            for (String evt: filter.getEvents()) {
                String attr = ":e" + (i++);
                eav.put(attr, new AttributeValue().withS(evt));
                eventIds.add(attr);
            }
            filterExpression.add("event in (" + eventIds.toString() + ")");
        }

        //createAt / from-to
        if (filter.getFrom() > 0 && filter.getTo() > 0) {
            eav.put(":from", new AttributeValue().withN(Long.toString(filter.getFrom())));
            eav.put(":to", new AttributeValue().withN(Long.toString(filter.getTo())));
            filterExpression.add("createdAt between :from and :to");
        }

        //createAt / from
        if (filter.getFrom() > 0 && filter.getTo() == 0) {
            eav.put(":from", new AttributeValue().withN(Long.toString(filter.getFrom())));
            filterExpression.add("createdAt >= :from");
        }

        //createAt / to
        if (filter.getFrom() == 0 && filter.getTo() > 0) {
            eav.put(":to", new AttributeValue().withN(Long.toString(filter.getTo())));
            filterExpression.add("createdAt <= :to");
        }

        List<Audit> audits;
        int count;
        if (eav.isEmpty()) {
            count = mapper.count(DynamoDBAudit.class, new DynamoDBScanExpression());
            audits = mapper.
                    scan(DynamoDBAudit.class, new DynamoDBScanExpression().
                            withConsistentRead(false).
                            withLimit(maxElement)).
                    stream().
                    sorted(Comparator.comparingLong(DynamoDBAudit::getCreatedAt).reversed()).
                    collect(Collectors.toList()).
                    subList(start, Math.min(maxElement, count)).
                    stream().
                    map(this::convert).
                    collect(Collectors.toList());
        } else {
            scanExpression = scanExpression.
                    withFilterExpression(filterExpression.toString()).
                    withExpressionAttributeValues(eav).
                    withConsistentRead(false).
                    withLimit(maxElement);

            count = mapper.count(DynamoDBAudit.class, scanExpression);
            audits = mapper.
                    scan(DynamoDBAudit.class, scanExpression).
                    stream().
                    sorted(Comparator.comparingLong(DynamoDBAudit::getCreatedAt).reversed()).
                    collect(Collectors.toList()).
                    subList(start, Math.min(maxElement, count)).
                    stream().
                    map(this::convert).
                    collect(Collectors.toList());
        }

        return new Page<>(audits, pageable.pageNumber(), pageable.pageSize(), count);
    }

    @Override
    public Optional<Audit> findById(String id) throws TechnicalException {
        DynamoDBAudit load = mapper.load(DynamoDBAudit.class, id);
        return Optional.ofNullable(convert(load));
    }

    @Override
    public Audit create(Audit audit) throws TechnicalException {
        if (audit == null) {
            throw new IllegalArgumentException("Trying to create null");
        }
        mapper.save(
                convert(audit),
                new DynamoDBSaveExpression().withExpectedEntry(
                        "id",
                        new ExpectedAttributeValue().withExists(false)
                )
        );
        return audit;
    }

    private Audit convert(DynamoDBAudit dynamoDBAudit) {
        if (dynamoDBAudit == null) {
            return null;
        }

        Audit audit = new Audit();
        audit.setId(dynamoDBAudit.getId());
        audit.setReferenceId(dynamoDBAudit.getReferenceId());
        audit.setReferenceType(Audit.AuditReferenceType.valueOf(dynamoDBAudit.getReferenceType()));
        audit.setUsername(dynamoDBAudit.getUsername());
        audit.setEvent(dynamoDBAudit.getEvent());
        audit.setProperties(dynamoDBAudit.getProperties());
        audit.setPatch(dynamoDBAudit.getPatch());
        audit.setCreatedAt(new Date(dynamoDBAudit.getCreatedAt()));

        return audit;
    }

    private DynamoDBAudit convert(Audit audit) {
        DynamoDBAudit dynamoDBAudit = new DynamoDBAudit();
        dynamoDBAudit.setId(audit.getId());
        dynamoDBAudit.setReferenceId(audit.getReferenceId());
        dynamoDBAudit.setReferenceType(audit.getReferenceType().name());
        dynamoDBAudit.setReference(dynamoDBAudit.getReferenceType()+":"+dynamoDBAudit.getReferenceId());
        dynamoDBAudit.setUsername(audit.getUsername());
        dynamoDBAudit.setEvent(audit.getEvent());
        dynamoDBAudit.setProperties(audit.getProperties());
        dynamoDBAudit.setPatch(audit.getPatch());
        dynamoDBAudit.setCreatedAt(audit.getCreatedAt().getTime());

        return dynamoDBAudit;
    }
}
