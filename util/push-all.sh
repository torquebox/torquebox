#!/bin/sh

top="$(dirname $0 )/.."
 
modules=$top/torquebox-*

for module in $modules ; do
  pushd $module
  git push origin master
  popd
done

pushd $top
git push origin master
popd
