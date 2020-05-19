# Terms and conditions
Provides FDK terms and conditions text, with versioning data, and a service for users to accept the terms and conditions on behalf of organizations.

## Requirements
- maven
- java 8
- docker
- docker-compose

## Run tests
```
% mvn verify
```

## Run locally
```
docker-compose up -d
mvn clean compile
mvn exec:java -Dspring.profiles.active=develop
```

Then in another terminal e.g.
```
% curl http://localhost:8080/terms/org/123456789
```

## Datastore
To inspect the MongoDB datastore, run in terminal
```
docker-compose exec mongodb mongo
use admin
db.auth("admin","admin")
use termsDB
db.orgterms.find()
```