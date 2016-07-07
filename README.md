# DIVA
[![analytics](http://www.google-analytics.com/collect?v=1&t=pageview&tid=UA-79686240-1&cid=5bc8d9b9-99b3-4646-b434-81d5d4479ff3&dl=https%3A%2F%2Fgithub.com%2Fdemandware-appsec%2FDIVA)]()

## Overview
The Demandware Intentionally Vulnerable WebApp is a teaching tool to help those interested in security test their skills on increasingly tough challenges. The game is run as a CTF, or Capture the Flag, where users must find randomly generated flags for each challenge in order to progress. There are a total of 20 challenges ranging from very simple HTML editing to "decrypting" an AES string. The applications has further instructions and suggestions for playing the game once started.
The application is  self-contained and doesn't require any setup besides an application server. It contains a database implementation, but is in-memory only and transient.

## Usage Suggestions
Demandware AppSec uses this CTF as a means to help teach different departments about Application Security and provide a testbed for interested individuals to try "real-world" attacks. We also use this CTF to help identify Security Champions and will be hosting it in the future during conferences and hackathons both internally and with customers. We have had success using this to explain why intermediate and advanced exploits work and why certain patterns that seem like great ideas can still be worked around or have unintended consequences -e.g. I've filtered certain control statements from SQL, or my one-time use code is using secure random (but I'm only changing three characters).

## Why make another CTF?
The impetus for making this CTF is that there is a strong lack of interesting CTF games that focus solely on Web security and delve deeply into the subject that are not pedantic. Some are excellent teaching tools; these hold users' hands through the process to make sure they can execute attacks to see what it takes, but these challenges are fairly basic and the attacks are of middling difficulty, at best. Other CTFs are excellent challenges; these typically cast a wide net -incorporating Network security, Binary exploitation, and Web security- but never delving too deeply or providing unrealistic exploitation patterns. This CTF is aimed squarely at web application security and does not deviate from it. Users will have to have a strong technical background or a very lateral-thinking mind to win. They will have to know basics such as SQL Injection and Remote File Inclusion, but they will also have to handle hash cracking, insecure storage of information, reversing the application's war file, even breaking java's Random class. We hope that this provides a very difficult set of challenges and helps drive excellent conversations about web security with challenges that can't be broken with metasploit.

## Important Disclaimers
The code contained in this application is intentionally terrible. Almost no part of this code base should be used in any other applications including side projects. The entire codebase is riddled with vulnerabilities even outside of the actual challenges (hint hint). It is lightly commented to provide only bare minimum sense of classes and some methods (you'll find out why).
The web application should not be deployed on any server containing any important information (docker is used for separation between docker image and host). Futhermore, if this application is used in any kind of multi-user challenge, networking and access to and from the hosting server should be thoroughly restricted. It should be assumed that players will gain full access to the host and will be able to do anything they wish on it.

## Installation
### Docker image from Hub 

Execute this command to pull from the [Docker Hub Site](https://hub.docker.com/r/dwreappsec/diva_ctf/)
```
docker pull dwreappsec/diva_ctf
```

### Dockerfile manual
Clone this repository and execute the Dockerfile from this repository
```
docker build -r dwreappsec/diva_ctf Dockerfile
```

### Running docker image
```
docker run -it -p {port}:8080 dwreappsec/diva_ctf  (You can also add --rm if you want the image destroyed on stop)
```
Then access your docker image ip on your custom port to begin.
NOTE: You may want to run 
```
docker-machine ls
```
to find your docker IP (if it's not your localhost).

### Maven
Clone this repository and run 
```
mvn -f DIVA/pom.xml install
```
and look in DIVA/target/ for DIVA.war. You may deploy this in any appserver that supports Servlets 3.1

## Hints
See the [Hints Page](http://demandware-appsec.github.io/DIVA/hints/) for help with specific challenges. Hints for each challenge are on their own pages in rollover text.

## License
### Code
Copyright 2016  Demandware Inc, All Rights Reserved.
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)

### Images
All images used in SQL database are [Creative Commons "No Rights Reserved"](https://creativecommons.org/about/cc0/)

