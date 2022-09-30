FROM centos:7

RUN yum update -y && \
  yum install -y \
  epel-release-7 \
  zip \
  unzip \
  java-11-openjdk \
  maven \
  make && \
  yum clean all

COPY officer-filing-api.jar /opt/officer-filing-api/
COPY start-ecs /usr/local/bin/

RUN chmod 555 /usr/local/bin/start-ecs

CMD ["start-ecs"]
