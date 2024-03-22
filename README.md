# officer-filing-api
This API is for appointing, changing and terminating director details, supporting the officer-filing-web Web UI.

## Requirements

In order to build this service locally you need:

- [Java 11](https://docs.oracle.com/en/java/javase/11)
- [corretto-11 JDK](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)
- [MongoDB](https://www.mongodb.com)
- [Spring Boot 2](https://spring.io/projects/spring-boot)
- [Spring Framework](https://spring.io/projects/spring-framework)
- [MapStruct](https://mapstruct.org/)
- [Docker](https://wwww.docker.com)
- [Tilt](https://tilt.dev)

## Installation

To download this repository, run the following from the command line and change into the directory:

```
git clone git@github.com:companieshouse/officer-filing-api.git

cd officer-filing-api

git submodule init

git submodule update

make dist
```

#### Configuration

The environment variables necessary to run the API can be found in the chs-configs repository and include:
- MONGODB_URL: points at the MongoDB instance for the specified environment
- LOGGING_LEVEL: the logging level for the CH structured logging


#### Docker Support

Pull image from private CH registry by running:
```
docker pull 169942020521.dkr.ecr.eu-west-1.amazonaws.com/local/officer-filing-api:latest

```
or run the following steps to build image locally:
```
mvn compile -s settings.xml jib:dockerBuild -Dimage=169942020521.dkr.ecr.eu-west-1.amazonaws.com
/local/officer-filing-api
```
#### docker-chs-development
To run the officer-filing service locally in docker

1. git clone the companieshouse/docker-chs-development repository and follow the steps in the readme file for docker-chs-development

1. To start up
    ```
    ./bin/chs-dev modules enable officer-filing

    tilt up
    ```
1. To enable development workflow for the service run the command: `./bin/chs-dev development enable officer-filing
   `

1. To disable development workflow for the service run the command: `./bin/chs-dev development disable officer
   -filing`

## Configuration
System properties for officer-filing-api are defined in application.properties. These are normally configured per environment.

| Variable                                     | Description                                                                           | Example                   | Mandatory |
|----------------------------------------------|---------------------------------------------------------------------------------------|---------------------------|-----------|
| MANAGEMENT_ENDPOINTS_ENABLED_BY_DEFAULT      |                                                                                       | false                     | always    |
| MANAGEMENT_ENDPOINT_HEALTH_ENABLED           |                                                                                       | true                      | always    |
| MANAGEMENT_ENDPOINTS_WEB_PATH_MAPPING_HEALTH |                                                                                       | healthcheck               | always    |
| MANAGEMENT_ENDPOINTS_WEB_BASE_PATH           |                                                                                       | /officer-filing-api       | always    |
| NATIONALITY_LIST                             | List of nationalities                                                                 |                           | always    |
| MONGODB_URL                                  | The URL of the MongoDB instance where documents and application data should be stored | mongodb://mongohost:27017 | always    |
| LOGGING_LEVEL                                | Log message granularity                                                               | INFO                      | always    | 
| WEB_LOGGING_LEVEL:INFO                       | Log web message granularity                                                           | INFO                      |           |
| REQUEST_LOGGING_LEVEL                        | Request log message granularity                                                       | WARN                      | always    |
## Usage
To create the officer filing an open transaction is required - see [Companies House Transaction API Service.](https://github.com/companieshouse/transactions.api.ch.gov.uk/blob/master/README.md)

### Current Endpoints
| Method | URI                                                                                         | Comments                                                             |
|--------|---------------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| GET    | /officer-filing/healthcheck                                                                       | System health check                                                  |
| POST   | /transactions/{transaction_id}/officers                                                     | Creates an officer filing resource, linking it to the transaction    |
| GET    | /private/transactions/{transaction_id}/officers/<br/>{filing_resource_id}/filings           | Wraps the filing resource data to produce standard message for CHIPS |
| GET    | /private/transactions/{transaction_id}/officers/<br/>{filing_resource_id}/validation_status | Final validation when the transaction is closed                      |
| GET    | /transactions/{transaction_id}/officers/<br/>{filing_resource_id}                           | Retrieves the officer filing data                                    |
| PATCH  | /transactions/{transaction_id}/officers/<br/>{filing_resource_id}                           | Updates (by insert/replace) the officer filing data                                    |

#### Other Environments

The API is deployed via Concourse or by the release team.

#### Using the REST API directly
The Draft API specification, documenting what has been implemented so far, can be found in `spec/swagger.json`.

##### Officer Filing API
Add link to the public API documentation when available

## Design
The following information will only be accessible from within the Companies House network.

* [Directors Service Architecture](https://companieshouse.atlassian.net/wiki/spaces/DACT/pages/3649699904/Directors+Service+Architecture)
* [Officer Filing API](https://companieshouse.atlassian.net/wiki/spaces/DACT/pages/3690889337/Officer+Filing+API)

### Data storage
The API Service, like most other Companies House services, stores its back-end
data in MongoDB, and that is not shown here. Each successful POST or PATCH causes
data to be stored in MongoDB. The model used by the DB can be found in the Java
package `uk.gov.ch.officerfiling.api.model.entity`.

### Validation
The API performs full validation of TM01, AP01 and CH01 submissions as well as field validation when patching a filing.

### Outputs
The API delivers a filing document (JSON) that is inputted to the CHIPS filing consumer.
