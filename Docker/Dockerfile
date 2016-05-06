FROM tomcat:8.0-jre8
MAINTAINER "Demandware Inc. AppSec"

#Install full jdk
RUN \
  apt-get update && \
  apt-get install -y openjdk-8-jdk && \
  rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64

ENV JAVA_OPTS="-Djava.security.egd=file:///dev/./urandom"

RUN rm -rf /usr/local/tomcat/webapps

COPY DIVA.war /usr/local/tomcat/webapps/DIVA.war
COPY server.xml /usr/local/tomcat/conf/server.xml

##ADD https://github.com/demandware-appsec/DIVA/tree/master/war_latest/DIVA.war /usr/local/tomcat/webapps/DIVA.war