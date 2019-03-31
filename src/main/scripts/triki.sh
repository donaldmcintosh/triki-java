#!/bin/bash

contentDir=$1
port=$2
debug=$3

usage() {
    echo "triki.sh <content dir> <port>"
}

if [ -z ${contentDir} ]
then
    usage
    exit -1
fi

if [ -z ${port} ]
then
    usage
    exit -1
fi

if [ ! -z ${debug} ]
then
   DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000"
fi

CLASSPATH=$CLASSPATH:../lib/triki.jar

java $DEBUG -Dcontent_dir=$1 -Dport=$2 net.opentechnology.triki.core.boot.TrikiMain
