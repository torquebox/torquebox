#!/usr/bin/env bash

i=0

control_c() 
{
  echo "Exiting"
  exit
}

trap control_c SIGINT

echo "Looping test: $*"

for (( ; ; ))
do
  i=$((i+1))
  echo "Loop $i"
  $*
  if [ $? != 0 ] 
  then
    echo "Failure!"   
    exit
  fi
done
