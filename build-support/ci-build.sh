#!/bin/sh

echo "*** Start Preflight ***"
echo "Removing $HOME/.m2/repository/org/torquebox/"
rm -rf $HOME/.m2/repository/org/torquebox

echo "Removing $HOME/.m2/repository/rubygems"
rm -rf $HOME/.m2/repository/rubygems

echo "Peforming cleaning"
mvn clean -Pinteg -Pdist

echo "*** Start Build ***"

echo "Peforming build"
$MAVEN_HOME/bin/mvn -U -s settings.xml install -Pinteg -Pdist

echo "*** Start Postflight ***"

echo "Removing $HOME/.m2/repository/org/torquebox/"
rm -rf $HOME/.m2/repository/org/torquebox
