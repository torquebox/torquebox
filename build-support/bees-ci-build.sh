#!/bin/sh

echo "*** Unsetting variables ***"
echo "Before:"
echo "     GEM_HOME: $GEM_HOME"
echo "     GEM_PATH: $GEM_PATH"
echo "  BUNDLE_PATH: $BUNDLE_PATH"

unset GEM_HOME
unset GEM_PATH
unset BUNDLE_PATH

echo "After:"
echo "     GEM_HOME: $GEM_HOME"
echo "     GEM_PATH: $GEM_PATH"
echo "  BUNDLE_PATH: $BUNDLE_PATH"

echo "*** Environment ***"
set

echo "*** PWD ***"
echo $PWD

echo "*** Start Preflight ***"
echo "Removing $M2_REPO/org/torquebox/"
rm -rf $M2_REPO/org/torquebox

echo "Removing $M2_REPO/rubygems"
rm -rf $M2_REPO/rubygems

echo "Peforming cleaning"
$MAVEN_HOME/bin/mvn clean -Pinteg -Pdist

echo "*** Start Build ***"

echo "Peforming core build skipping tests"
$MAVEN_HOME/bin/mvn -Dmaven.repo.local=$M2_REPO -U -s build-support/settings.xml install -Dmaven.test.skip=true

echo "Peforming integ build"
cd integration-tests && $MAVEN_HOME/bin/mvn -Dmaven.repo.local=$M2_REPO -U -s build-support/settings.xml test

