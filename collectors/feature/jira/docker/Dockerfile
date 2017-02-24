
FROM docker.io/java:openjdk-8-jdk

MAINTAINER Hygieia@capitalone.com

RUN \

  mkdir  /hygieia

COPY *.jar /hygieia/
COPY jira-properties-builder.sh /hygieia/

WORKDIR /hygieia/

VOLUME ["/hygieia/logs"]

CMD ./jira-properties-builder.sh &&\
	 java -Djava.security.egd=file:/dev/./urandom -Djasypt.encryptor.password=$JASYPT_ENCRYPTOR_SECRET -jar jira-feature-collector*.jar --spring.config.location=hygieia-jira-feature-collector.properties
