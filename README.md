# officer-filing-api
This API is for appointing, changing and terminating director details.

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

## Usage
To create the officer filing an open transaction is required - see [Companies House Transaction API Service.](https://github.com/companieshouse/transactions.api.ch.gov.uk/blob/master/README.md)

### Current Endpoints
| Method | URI                                                                                         | Comments                                                             |
|--------|---------------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| GET    | /officers/healthcheck                                                                       | System health check                                                  |
| POST   | /transactions/{transaction_id}/officers                                                     | Creates an officer filing resource, linking it to the transaction    |
| GET    | /private/transactions/{transaction_id}/officers/<br/>{filing_resource_id}/filings           | Wraps the filing resource data to produce standard message for CHIPS |
| GET    | /private/transactions/{transaction_id}/officers/<br/>{filing_resource_id}/validation_status | Final validation when the transaction is closed                      |
| GET    | /transactions/{transaction_id}/officers/<br/>{filing_resource_id}                           | Retrieves the officer filing data                                    |

#### Other Environments

The API is deployed via Concourse or by the release team.

#### Using the REST API directly
The API specification can be found - TBA

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
Simple field validation for TM01 mandatory fields e.g. date, where resigned on date is not in the future, and IDs. 
Further validation will be required for other forms.

### API project code structure
TBA



