#!/bin/sh

echo "*** Start Preflight ***"
echo "Removing $M2_REPO/org/torquebox/"
rm -rf $M2_REPO/org/torquebox

echo "Removing $M2_REPO/rubygems"
rm -rf $M2_REPO/rubygems

echo "Peforming cleaning"
mvn clean -Pinteg -Pdist

echo "*** Start Build ***"

echo "Peforming build"
mvn -U -s build-support/settings.xml install -Pinteg -Pdist -Pci -e

