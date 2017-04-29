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
package io.gravitee.repository.dynamodb.management.model;

import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;

/**
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com) 
 * @author GraviteeSource Team
 */
public interface DynamoDBGraviteeSchema {
    String prefix = "GraviteeioApim";
    //Tenants
    String TENANT_TABLENAME = prefix + "Tenant";
    ProvisionedThroughput TENANT_PRO_THROU = new ProvisionedThroughput(5L, 5L);
    //Views
    String VIEW_TABLENAME = prefix + "View";
    ProvisionedThroughput VIEW_PRO_THROU = new ProvisionedThroughput(5L, 5L);
    //Tags
    String TAG_TABLENAME = prefix + "Tag";
    ProvisionedThroughput TAG_PRO_THROU = new ProvisionedThroughput(5L, 5L);
    //Users
    String USER_TABLENAME = prefix + "User";
    ProvisionedThroughput USER_PRO_THROU = new ProvisionedThroughput(5L, 5L);
    //Subscriptions
    String SUBSCRIPTION_TABLENAME = prefix + "Subscription";
    ProvisionedThroughput SUBSCRIPTION_PRO_THROU = new ProvisionedThroughput(5L, 5L);
    //Plans
    String PLAN_TABLENAME = prefix + "Plan";
    ProvisionedThroughput PLAN_PRO_THROU = new ProvisionedThroughput(5L, 5L);
    //Pages
    String PAGE_TABLENAME = prefix + "Page";
    ProvisionedThroughput PAGE_PRO_THROU = new ProvisionedThroughput(5L, 5L);
    //Memberships
    String MEMBERSHIP_TABLENAME = prefix + "Membership";
    ProvisionedThroughput MEMBERSHIP_PRO_THROU = new ProvisionedThroughput(5L, 5L);
    //Groups
    String GROUP_TABLENAME = prefix + "Group";
    ProvisionedThroughput GROUP_PRO_THROU = new ProvisionedThroughput(5L, 5L);
    //Applications
    String APPLICATION_TABLENAME = prefix + "Application";
    ProvisionedThroughput APPLICATION_PRO_THROU = new ProvisionedThroughput(5L, 5L);
    //Apis
    String API_TABLENAME = prefix + "Api";
    ProvisionedThroughput API_PRO_THROU = new ProvisionedThroughput(5L, 5L);
    //ApiKeys
    String APIKEY_TABLENAME = prefix + "ApiKey";
    ProvisionedThroughput APIKEY_PRO_THROU = new ProvisionedThroughput(5L, 5L);
    //Events
    String EVENT_TABLENAME = prefix + "Event";
    ProvisionedThroughput EVENT_PRO_THROU = new ProvisionedThroughput(5L, 5L);
    //EventSearchIndexes
    String EVENT_SEARCH_INDEX_TABLENAME = prefix + "EventSearchIndex";
    ProvisionedThroughput EVENT_SEARCH_INDEX_PRO_THROU = new ProvisionedThroughput(5L, 5L);
    //Metadatas
    String METADATA_TABLENAME = prefix + "Metadata";
    ProvisionedThroughput METADATA_PRO_THROU = new ProvisionedThroughput(5L, 5L);
}
