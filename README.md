# officer-filing-api 
This API is for appointing, changing and terminating director details, supporting the officer-filing-web Web UI.

## Requirements

In order to build this service locally you need:

- [Java 21](https://docs.oracle.com/en/java/javase/11)
- [Java 21.0.2-amzn JDK](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)
- [MongoDB](https://www.mongodb.com)
- [Spring Boot 3](https://spring.io/projects/spring-boot)
- [Spring Framework](https://spring.io/projects/spring-framework)
- [MapStruct](https://mapstruct.org/)
- [Docker](https://wwww.docker.com)

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

    chs-dev up
    ```
1. To enable development workflow for the service run the command: `./bin/chs-dev development enable officer-filing
` 

1. To disable development workflow for the service run the command: `./bin/chs-dev development disable officer
-filing`

## Configuration
System properties for officer-filing-api are defined in application.properties. These are normally configured per environment.

| Variable                                     | Description                                                                           | Example                         | Mandatory |
|----------------------------------------------|---------------------------------------------------------------------------------------|---------------------------------|-----------|
| MANAGEMENT_ENDPOINTS_ENABLED_BY_DEFAULT      |                                                                                       | false                           | always    |
| MANAGEMENT_ENDPOINT_HEALTH_ENABLED           |                                                                                       | true                            | always    |
| MANAGEMENT_ENDPOINTS_WEB_PATH_MAPPING_HEALTH |                                                                                       | healthcheck                     | always    |
| MANAGEMENT_ENDPOINTS_WEB_BASE_PATH           |                                                                                       | /officer-filing-api             | always    |
| NATIONALITY_LIST                             | List of nationalities                                                                 | "American;British;French;Irish" | always    |
| MONGODB_URL                                  | The URL of the MongoDB instance where documents and application data should be stored | mongodb://mongohost:27017       | always    |
| LOGGING_LEVEL                                | Log message granularity                                                               | INFO                            | always    | 
| WEB_LOGGING_LEVEL:INFO                       | Log web message granularity                                                           | INFO                            |           |
| REQUEST_LOGGING_LEVEL                        | Request log message granularity                                                       | WARN                            | always    |

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

## Terraform ECS

### What does this code do?

The code present in this repository is used to define and deploy a dockerised container in AWS ECS.
This is done by calling a [module](https://github.com/companieshouse/terraform-modules/tree/main/aws/ecs) from terraform-modules. Application specific attributes are injected and the service is then deployed using Terraform via the CICD platform 'Concourse'.


Application specific attributes | Value                                | Description
:---------|:-----------------------------------------------------------------------------|:-----------
**ECS Cluster**        |filing-maintain                                      | ECS cluster (stack) the service belongs to
**Load balancer**      |{env}-chs-apichgovuk & {env}-chs-apichgovuk-private                          | The load balancer that sits in front of the service
**Concourse pipeline**     |[Pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/officer-filing-api ) <br> [Pipeline code](https://github.com/companieshouse/ci-pipelines/blob/master/pipelines/ssplatform/team-development/officer-filing-api)                                  | Concourse pipeline link in shared services


### Contributing
- Please refer to the [ECS Development and Infrastructure Documentation](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/4390649858/Copy+of+ECS+Development+and+Infrastructure+Documentation+Updated) for detailed information on the infrastructure being deployed.

### Testing
- Ensure the terraform runner local plan executes without issues. For information on terraform runners please see the [Terraform Runner Quickstart guide](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/1694236886/Terraform+Runner+Quickstart).
- If you encounter any issues or have questions, reach out to the team on the **#platform** slack channel.

### Vault Configuration Updates
- Any secrets required for this service will be stored in Vault. For any updates to the Vault configuration, please consult with the **#platform** team and submit a workflow request.

### Useful Links
- [ECS service config dev repository](https://github.com/companieshouse/ecs-service-configs-dev)
- [ECS service config production repository](https://github.com/companieshouse/ecs-service-configs-production)
