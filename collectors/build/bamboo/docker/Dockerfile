
FROM docker.io/java:openjdk-8-jdk

MAINTAINER Hygieia@capitalone.com

RUN \
  mkdir /hygieia

COPY *.jar /hygieia/
COPY bamboo-build-properties-builder.sh /hygieia/

WORKDIR /hygieia

VOLUME ["/hygieia/logs"]

CMD ./jenkins-build-properties-builder.sh && \
  java -Djava.security.egd=file:/dev/./urandom -Djasypt.encryptor.password=$JASYPT_ENCRYPTOR_SECRET -jar bamboo-build-collector*.jar --spring.config.location=/hygieia/hygieia-bamboo-build-collector.properties

