#!/bin/bash

contentDir=$1
port=$2

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

java -Dcontent_dir=$1 -Dport=$2 -jar ../lib/triki.jar
