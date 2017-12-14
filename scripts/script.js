let AWS = require('aws-sdk');
AWS.config.loadFromPath('./config.json');
let dynamodb = new AWS.DynamoDB({apiVersion: '2012-08-10'});
const args = process.argv.slice(2);

log("===================================");
log();
log("  Create Gravitee.io APIM Tables");
log();
log("               -----");
log();
log("  TableName prefix: " + getPrefix());
log();
log("===================================");

dynamodb.listTables({}, function(err, data) {
  if (err) {
    log(err, err.stack);
  } else {
    log("Existing Tables:");
    const existingTables = data.TableNames;
    if (existingTables.length > 0) {
      for (let tableName of existingTables) {
        log("  - " + tableName);
      }
    } else {
      log("  - no existing tables.")
    }

    log();
    log("Create missing tables:");
    for (let table of getTables(getPrefix())) {
      if (existingTables.indexOf(table.TableName) < 0) {
        dynamodb.createTable(table, function(err, data) {
          if (err) {
            log("  - ERROR: " + table.TableName);
            log(err, err.stack);
          } else {
            log("  - SUCCESS: " + table.TableName);
          }
        });
      }
    }
  }
});

function log(msg) {
  console.log("[" + new Date().toISOString() + "] " + (msg?msg:''));
}

function getTables(prefix) {
  return [
    getApiTable(prefix + "Api"),
    getApiKeyTable(prefix + "ApiKey"),
    getApplicationTable(prefix + "Application"),
    getEventTable(prefix + "Event"),
    getEventSearchIndexTable(prefix + "EventSearchIndex"),
    getGroupTable(prefix + "Group"),
    getMembershipTable(prefix + "Membership"),
    getMetadataTable(prefix + "Metadata"),
    getPageTable(prefix + "Page"),
    getPlanTable(prefix + "Plan"),
    getSubscriptionTable(prefix + "Subscription"),
    getTagTable(prefix + "Tag"),
    getTenantTable(prefix + "Tenant"),
    getUserTable(prefix + "User"),
    getViewTable(prefix + "View"),
    getRoleTable(prefix + "Role"),
    getRatingTable(prefix + "Rating"),
    getRatingAnswerTable(prefix + "RatingAnswer"),
    getAuditTable(prefix + "Audit")
  ];
}

function getPrefix() {
  if (args && args.length > 0) {
    return args[0];
  }
  return "GraviteeioApim";
}

function getApiTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      {"AttributeName": "id", "AttributeType": "S"},
      {"AttributeName": "visibility", "AttributeType": "S"}
    ],
    "KeySchema": [
      {"AttributeName": "id", "KeyType": "HASH"}
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    },
    "GlobalSecondaryIndexes": [
      {
        "IndexName": "ApiVisibility",
        "KeySchema": [
          {"AttributeName": "visibility", "KeyType": "HASH"}
        ],
        "Projection": {"ProjectionType": "ALL"},
        "ProvisionedThroughput": {
          "ReadCapacityUnits": 5,
          "WriteCapacityUnits": 5
        }
      }
    ]
  };
}

function getApiKeyTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      {"AttributeName": "key", "AttributeType": "S"},
      {"AttributeName": "subscription", "AttributeType": "S"},
      {"AttributeName": "plan", "AttributeType": "S"},
      {"AttributeName": "updatedAt", "AttributeType": "N"}

    ],
    "KeySchema": [
      {"AttributeName": "key", "KeyType": "HASH"}
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    },
    "GlobalSecondaryIndexes": [
      {
        "IndexName": "ApiKeySubscription",
        "KeySchema": [
          {"AttributeName": "subscription", "KeyType": "HASH"}
        ],
        "Projection": {"ProjectionType": "ALL"},
        "ProvisionedThroughput": {
          "ReadCapacityUnits": 5,
          "WriteCapacityUnits": 5
        }
      },
      {
        "IndexName": "ApiKeyPlan",
        "KeySchema": [
          {"AttributeName": "plan", "KeyType": "HASH"},
          {"AttributeName": "updatedAt", "KeyType": "RANGE"}
        ],
        "Projection": {"ProjectionType": "ALL"},
        "ProvisionedThroughput": {
          "ReadCapacityUnits": 5,
          "WriteCapacityUnits": 5
        }
      }
    ]
  };
}

function getApplicationTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      {"AttributeName": "id", "AttributeType": "S"},
      {"AttributeName": "status", "AttributeType": "S"}
    ],
    "KeySchema": [
      {"AttributeName": "id", "KeyType": "HASH"}
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    },
    "GlobalSecondaryIndexes": [
      {
        "IndexName": "ApplicationStatus",
        "KeySchema": [
          {"AttributeName": "status", "KeyType": "HASH"}
        ],
        "Projection": {"ProjectionType": "ALL"},
        "ProvisionedThroughput": {
          "ReadCapacityUnits": 5,
          "WriteCapacityUnits": 5
        }
      }
    ]
  };
}

function getEventTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      {"AttributeName": "id", "AttributeType": "S"},
      {"AttributeName": "updatedAt", "AttributeType": "N"}
    ],
    "KeySchema": [
      {"AttributeName": "id", "KeyType": "HASH"}
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    },
    "GlobalSecondaryIndexes": [
      {
        "IndexName": "EventKeyAndUpdateDate",
        "KeySchema": [
          {"AttributeName": "id", "KeyType": "HASH"},
          {"AttributeName": "updatedAt", "KeyType": "RANGE"}
        ],
        "Projection": {"ProjectionType": "ALL"},
        "ProvisionedThroughput": {
          "ReadCapacityUnits": 5,
          "WriteCapacityUnits": 5
        }
      }
    ]
  };
}

function getEventSearchIndexTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      { "AttributeName": "id", "AttributeType": "S" }
    ],
    "KeySchema": [
      { "AttributeName": "id", "KeyType": "HASH" }
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    }
  };
}

function getGroupTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      { "AttributeName": "id", "AttributeType": "S" }
    ],
    "KeySchema": [
      { "AttributeName": "id", "KeyType": "HASH" }
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    }
  };
}

function getMembershipTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      { "AttributeName": "id", "AttributeType": "S" },
      { "AttributeName": "userId", "AttributeType": "S" },
      { "AttributeName": "referenceType", "AttributeType": "S" },
      { "AttributeName": "referenceId", "AttributeType": "S" }
    ],
    "KeySchema": [
      { "AttributeName": "id", "KeyType": "HASH" }
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    },
    "GlobalSecondaryIndexes": [
      {
        "IndexName": "UserAndReferenceType",
        "KeySchema": [
          { "AttributeName": "userId", "KeyType": "HASH" },
          { "AttributeName": "referenceType", "KeyType": "RANGE" }
        ],
        "Projection": { "ProjectionType": "ALL" },
        "ProvisionedThroughput": {
          "ReadCapacityUnits": 5,
          "WriteCapacityUnits": 5
        }
      },
      {
        "IndexName": "ReferenceTypeAndId",
        "KeySchema": [
          { "AttributeName": "referenceId", "KeyType": "HASH" },
          { "AttributeName": "referenceType", "KeyType": "RANGE" }
        ],
        "Projection": { "ProjectionType": "ALL" },
        "ProvisionedThroughput": {
          "ReadCapacityUnits": 5,
          "WriteCapacityUnits": 5
        }
      }
    ]
  };
}

function getMetadataTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      { "AttributeName": "id", "AttributeType": "S" },
      { "AttributeName": "referenceType", "AttributeType": "S" },
      { "AttributeName": "referenceId", "AttributeType": "S" }
    ],
    "KeySchema": [
      { "AttributeName": "id", "KeyType": "HASH" }
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    },
    "GlobalSecondaryIndexes": [
      {
        "IndexName": "Reference",
        "KeySchema": [
          { "AttributeName": "referenceType", "KeyType": "HASH" },
          { "AttributeName": "referenceId", "KeyType": "RANGE" }
        ],
        "Projection": { "ProjectionType": "ALL" },
        "ProvisionedThroughput": {
          "ReadCapacityUnits": 5,
          "WriteCapacityUnits": 5
        }
      }
    ]
  };
}

function getPageTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      { "AttributeName": "id", "AttributeType": "S" }
    ],
    "KeySchema": [
      { "AttributeName": "id", "KeyType": "HASH" }
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    }
  };
}

function getPlanTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      { "AttributeName": "id", "AttributeType": "S" }
    ],
    "KeySchema": [
      { "AttributeName": "id", "KeyType": "HASH" }
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    }
  };
}

function getSubscriptionTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      { "AttributeName": "id", "AttributeType": "S" }
    ],
    "KeySchema": [
      { "AttributeName": "id", "KeyType": "HASH" }
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    }
  };
}

function getTagTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      { "AttributeName": "id", "AttributeType": "S" }
    ],
    "KeySchema": [
      { "AttributeName": "id", "KeyType": "HASH" }
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    }
  };
}

function getTenantTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      { "AttributeName": "id", "AttributeType": "S" }
    ],
    "KeySchema": [
      { "AttributeName": "id", "KeyType": "HASH" }
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    }
  };
}

function getUserTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      { "AttributeName": "username", "AttributeType": "S" }
    ],
    "KeySchema": [
      { "AttributeName": "username", "KeyType": "HASH" }
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    }
  };
}

function getViewTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      { "AttributeName": "id", "AttributeType": "S" }
    ],
    "KeySchema": [
      { "AttributeName": "id", "KeyType": "HASH" }
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    }
  };
}

function getRoleTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      { "AttributeName": "id", "AttributeType": "S" },
      { "AttributeName": "scope", "AttributeType": "N" }
    ],
    "KeySchema": [
      { "AttributeName": "id", "KeyType": "HASH" }
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    },
    "GlobalSecondaryIndexes": [
      {
        "IndexName": "RoleScope",
        "KeySchema": [
          { "AttributeName": "scope", "KeyType": "HASH" }
        ],
        "Projection": {
          "ProjectionType": "ALL"
        },
        "ProvisionedThroughput": {
          "ReadCapacityUnits": 5,
          "WriteCapacityUnits": 5
        }
      }
    ]
  };
}

function getRatingTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      { "AttributeName": "id", "AttributeType": "S" },
      { "AttributeName": "api", "AttributeType": "S" },
      { "AttributeName": "user", "AttributeType": "S" }
    ],
    "KeySchema": [
      { "AttributeName": "id", "KeyType": "HASH" }
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    },
    "GlobalSecondaryIndexes": [
      {
        "IndexName": "RatingApiAndUser",
        "KeySchema": [
          { "AttributeName": "api", "KeyType": "HASH" },
          { "AttributeName": "user", "KeyType": "RANGE" }
        ],
        "Projection": {
          "ProjectionType": "ALL"
        },
        "ProvisionedThroughput": {
          "ReadCapacityUnits": 5,
          "WriteCapacityUnits": 5
        }
      }
    ]
  };
}

function getRatingAnswerTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      { "AttributeName": "id", "AttributeType": "S" },
      { "AttributeName": "rating", "AttributeType": "S" }
    ],
    "KeySchema": [
      { "AttributeName": "id", "KeyType": "HASH" }
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    },
    "GlobalSecondaryIndexes": [
      {
        "IndexName": "RatingAnswer",
        "KeySchema": [
          { "AttributeName": "rating", "KeyType": "HASH" }
        ],
        "Projection": {
          "ProjectionType": "ALL"
        },
        "ProvisionedThroughput": {
          "ReadCapacityUnits": 5,
          "WriteCapacityUnits": 5
        }
      }
    ]
  };
}

function getAuditTable(tableName) {
  return {
    "TableName": tableName,
    "AttributeDefinitions": [
      { "AttributeName": "id", "AttributeType": "S" }
    ],
    "KeySchema": [
      { "AttributeName": "id", "KeyType": "HASH" }
    ],
    "ProvisionedThroughput": {
      "ReadCapacityUnits": 5,
      "WriteCapacityUnits": 5
    }
  };
}