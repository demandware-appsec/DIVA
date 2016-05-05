#!/bin/bash

if [ "$TRAVIS_REPO_SLUG" == "demandware-appsec/DIVA" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then

  echo -e "Creating war file"
  mvn -f DIVA/pom.xml install

  echo -e "Publishing war for $TRAVIS_JDK_VERSION"
  jdkver=$(echo -n $TRAVIS_JDK_VERSION | tail -c 4)

  mkdir $HOME/war-latest/
  cp DIVA/target/*.war $HOME/war-latest/
  echo -e "Copied war"

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/demandware-appsec/DIVA gh-pages > /dev/null
  echo -e "Cloned gh-pages"

  cd gh-pages
  mkdir ./war/$jdkver/
  cp $HOME/war-latest/*.war ./war/$jdkver/
  git add -f .
  git commit -m "Updating wars on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"
  git push -fq origin gh-pages > /dev/null
  echo -e "Published war to gh-pages.\n"

fi