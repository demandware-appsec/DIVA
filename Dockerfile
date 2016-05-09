FROM tomcat:8.0-jre8
MAINTAINER "Demandware Inc. AppSec"

#Install full jdk
RUN \
  apt-get update && \
  apt-get install -y openjdk-8-jdk && \
  rm -rf /var/lib/apt/lists/*
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64

#setup Maven info
ENV MAVEN_VERSION 3.3.9
ENV MAVEN_HOME /usr/share/maven
ENV PATH "$PATH:$MAVEN_HOME/bin"

#Install maven
RUN curl -fSL http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share && \
    mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven && \
    ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

#Fix for bad entropy in headless container
ENV JAVA_OPTS="-Djava.security.egd=file:///dev/./urandom"

#clean tomcat
RUN rm -rf /usr/local/tomcat/webapps && mkdir /usr/local/tomcat/webapps/

#pull DIVA into container
COPY DIVA/ /usr/local/DIVA/

#make the webapp
RUN \
  cd /usr/local/DIVA && \
  mvn install && \
  mv /usr/local/DIVA/target/DIVA.war /usr/local/tomcat/webapps/DIVA.war

#clean build location
RUN rm -rf /usr/local/DIVA/

#inject the server settings
COPY Dockerdata/server.xml /usr/local/tomcat/conf/server.xml


