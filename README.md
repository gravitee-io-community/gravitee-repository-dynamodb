[![Build Status](https://ci.gravitee.io/buildStatus/icon?job=gravitee-io/gravitee-repository-dynamodb/master)](https://ci.gravitee.io/job/gravitee-io/job/gravitee-repository-dynamodb/job/master/)

# Gravitee AWS DynamoDB Repository

AWS DynamoDB repository

## Installing
Move the *gravitee-repository-dynamodb-1.0.0-SNAPSHOT.zip* in the **Gravitee** */plugins* directory.

## Create tables
You can create tables using the [Amazon aws CLI](http://docs.aws.amazon.com/cli/latest/).
All scripts are located in the `scripts` folder.
If you want to create tables on your local dynamoDb, you have to provide the endpoint url:
```
$ cd scripts
$ aws dynamodb create-table --endpoint-url http://localhost:8000 --cli-input-json file://01-createtable-api.json
$ aws dynamodb create-table --endpoint-url http://localhost:8000 --cli-input-json file://02-createtable-apikey.json
$ aws dynamodb create-table --endpoint-url http://localhost:8000 --cli-input-json file://03-createtable-application.json
$ aws dynamodb create-table --endpoint-url http://localhost:8000 --cli-input-json file://04-createtable-event.json
$ aws dynamodb create-table --endpoint-url http://localhost:8000 --cli-input-json file://05-createtable-eventsearchindex.json
$ aws dynamodb create-table --endpoint-url http://localhost:8000 --cli-input-json file://06-createtable-group.json
$ aws dynamodb create-table --endpoint-url http://localhost:8000 --cli-input-json file://07-createtable-membership.json
$ aws dynamodb create-table --endpoint-url http://localhost:8000 --cli-input-json file://08-createtable-metadata.json
$ aws dynamodb create-table --endpoint-url http://localhost:8000 --cli-input-json file://09-createtable-page.json
$ aws dynamodb create-table --endpoint-url http://localhost:8000 --cli-input-json file://10-createtable-plan.json
$ aws dynamodb create-table --endpoint-url http://localhost:8000 --cli-input-json file://11-createtable-subscription.json
$ aws dynamodb create-table --endpoint-url http://localhost:8000 --cli-input-json file://12-createtable-tag.json
$ aws dynamodb create-table --endpoint-url http://localhost:8000 --cli-input-json file://13-createtable-tenant.json
$ aws dynamodb create-table --endpoint-url http://localhost:8000 --cli-input-json file://14-createtable-user.json
$ aws dynamodb create-table --endpoint-url http://localhost:8000 --cli-input-json file://15-createtable-view.json
```

## Configure

### Credentials
You can configure credentials using `gravitee.yml`

```
management:
  type: dynamodb
  dynamodb:
    #http://docs.aws.amazon.com/general/latest/gr/rande.html#ddb_region
    awsRegion: eu-west-2
    awsAccessKeyId: YOUR-ACCESS-KEY-ID
    awsSecretKey: YOUR-SECRET-ACCESS-KEY
```

Or you can use the default AWS credential mechanism to set options:
http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html

This means you can set credentials via :
1. Environment variables
2. Java system properties
3. The default credentials profile file
4. Amazon ECS container credentials
5. EC2 instance profile credentials


### How to run a local DynamoDB
You can setup a local DynamoDB following this guide: http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.html

But to get started quickly, we provide a Docker image
```
docker run -p 8000:8000 graviteeio/dynamodb
```

To configure Gravitee.io APIM to connect to your local instance you have to configure your local endpoint:

```
management:
  type: dynamodb
  dynamodb:
    #http://docs.aws.amazon.com/general/latest/gr/rande.html#ddb_region
    awsRegion: eu-west-2
    awsAccessKeyId: localdynamodb
    awsSecretKey: xxx
    awsEndpoint: http://localhost:8000
```

And configure your AWS CLI to use your local dynamodb :
```
$ export AWS_ACCESS_KEY_ID=localdynamodb
$ export AWS_SECRET_ACCESS_KEY=xxx
$ aws dynamodb create-table --endpoint-url http://localhost:8000 --cli-input-json ...
```
