#!/bin/sh

top="$(dirname $0 )/.."
 
modules=$top/torquebox-*

#echo $modules

for module in $modules ; do
  pushd $module
  git commit -a
  popd
done

pushd $top
git commit -a
popd
