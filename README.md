# Functional Movie Library

This repository contains a movie library service written in a functional way. 

## Overview

Things you can find here:
- http4s
- circe
- cats and cats-effect
- pureconfig
- refined
- practical usage of EitherT
- tagless final
- scanamo with custom interpreter for Async from cats-effect (purely for fun \& learning)
- ...

This experiment is a side-effect of playing around with Typelevel libraries. It was inspired by:
- https://github.com/pauljamescleary/scala-pet-store
- https://github.com/cb372/web-app-functional-style
- https://github.com/calvinlfer/tagless-final-example

## Running

1. Download local DynamoDB [here](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.html).
2. Run local DynamoDB: 
```bash
java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb
```
3. Go to DynamoDB local shell http://localhost:8000/shell and create a table:
```javascript
const params = {
    TableName: 'movies-test',
    KeySchema: [
        {
            AttributeName: 'id',
            KeyType: 'HASH'
        }
    ],
    AttributeDefinitions: [
        {
            AttributeName: 'id',
            AttributeType: 'N'
        }
    ],
    ProvisionedThroughput: {
        ReadCapacityUnits: 5,
        WriteCapacityUnits: 5
    }
};

dynamodb.createTable(params, function(err, data) {
    if (err) ppJson(err);
    else ppJson(data);
});
```

4. Run the service `sbt run`
5. Make some calls:

```bash
curl http://localhost:8080/movies -d '{"title": "Star Wars IV", "year": 1977, "id": 1}'
curl http://localhost:8080/movies -d '{"title": "pulp fiction", "year": 1994, "id": 2}'
curl http://localhost:8080/movies
curl http://localhost:8080/movies/1
curl -X DELETE http://localhost:8080/movies/1
curl http://localhost:8080/movies
```

## Testing

```bash
sbt test
```
