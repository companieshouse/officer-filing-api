# officer-filing-api
This API is for 

## Requirements

In order to build this service locally you need:

- [Java 11]()
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)
- [MongoDB](https://www.mongodb.com)
- [Spring Boot 2](https://spring.io/projects/spring-boot)
- [Spring Framework](https://spring.io/projects/spring-framework)
- [MapStruct](https://mapstruct.org/)

## Installation

To download this repository, run the following from the command line and change into the directory:

```
git clone git@github.com:companieshouse/officer-filing-api.git

cd officer-filing-api

make dist
```

#### Configuration

The environment variables necessary to run the API can be found in: 

chs-configs/(environment)/officer-filing-api/env

#### Docker Support

Pull image from private CH registry by running 
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

git clone the companieshouse/docker-chs-development repository

To start up
```
./bin/chs-dev enable module officer-filing

tilt up
```
To enable development workflow for the service run `./bin/chs-dev development enable officer-filing
` command.

To disable development workflow for the service run `./bin/chs-dev development disable officer
-filing
` command.
Postman requests
 <links to the doco here> 

#### Other Environments

The API is deployed via Concourse or by the release team.

## Usage

TBA

#### Using the REST API directly
The API specification can be found - TBA

##### Officer Filing API
Add link to the public api documentation when available


## Design
The following information will only be accessible from within the Companies House network.

* https://companieshouse.atlassian.net/wiki/spaces/DACT/pages/3649699904/Directors+Service
+Architecture
* https://companieshouse.atlassian.net/wiki/spaces/DACT/pages/3690889337/Officer+Filing+API

### Data storage
The API Service, like most other Companies House services, stores its back-end
 data in MongoDB, and that is not shown here. Each successful POST or PATCH causes
 data to be stored in MongoDB. The model used by the DB can be found in the Java
 package `uk.gov.ch.officerfiling.api.model.entity`.
 
### Validation



### API project code structure




