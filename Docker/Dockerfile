FROM tomcat:8.0-jre8
MAINTAINER "Demandware Inc. AppSec"

ENV JAVA_OPTS="-Djava.security.egd=file:///dev/./urandom"

RUN rm -rf /usr/local/tomcat/webapps

COPY DIVA.war /usr/local/tomcat/webapps/DIVA.war
COPY server.xml /usr/local/tomcat/conf/server.xml

##ADD https://github.com/demandware-appsec/DIVA/tree/master/war_latest/DIVA.war /usr/local/tomcat/webapps/DIVA.war