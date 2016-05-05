# DIVA
### Demandware Inc, 2016

##Overview
The Demandware Intentionally Vulnerable WebApp is a teaching tool to help those interested in security test their skills on increasingly tough challenges. There are a total of 20 challenges ranging from very simple HTML editing to "decrypting" and AES string. The applications has further instructions and suggestions for playing the game once started.
The application is mostly self-contained and doesn't require any setup besides an application server. It contains a database implementation, but is in-memory only and transient.

##Important Disclaimers
The code contained in this application is intentionally terrible. Almost no part of this code base should be used in any other applications including side projects. The entire codebase is riddled with vulnerabilities even outside of the actual challenges. 
The web application should not be deployed on any server containing any important information (docker is used for separation between docker image and host). Futhermore, if this application is used in any kind of multi-user challenge, networking and access to and from the hosting server should be thoroughly restricted.

##Installation
###Docker image from hub
Execute this command to pull from docker hub
```
docker pull dwreappsec/diva
```
Then access your docker image ip on port 9000.

###Dockerfile manual
Download the Dockerfile from this repository and run
```
docker build -r dwreappsec/diva Dockerfile
```

###Running docker image
```
docker run -it -p {port}:8080 {custom/name}  (You can also add --rm if you want the image destroyed on stop)
```
Then access your docker image ip on your custom port to begin.
NOTE: You may want to run 
```
docker-machine ls
```
to find your docker IP (if it's not your localhost).

###Maven
Clone this repository and run 
```
mvn -f DIVA/pom.xml install
```
and look in DIVA/target/ for DIVA.war. You may deploy this in any appserver that supports Servlets 3.1

##Hints
See the [Hints Page]() for help with specific challenges. Hints for each challenge are on their own pages in rollover text.

##Usage Suggestions
Demandware AppSec uses this CTF as a means to help teach different departments about Application Security and provide a testbed for interested individuals to try "real-world" attacks. We also use this CTF to help identify Security Champions and will be hosted in the future during conferences and hackathons both internally and with customers.

## License
###Code
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)

###Images
All images used in SQL database are [Creative Commons "No Rights Reserved"](https://creativecommons.org/about/cc0/)

