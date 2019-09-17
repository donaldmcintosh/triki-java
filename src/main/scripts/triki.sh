#!/bin/bash -x

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

if [ ! -z ${TRIKI_MODULES} ]
then
  for jar in `ls -1 $TRIKI_MODULES`; do
    TRIKI_MODULES=$jar:$TRIKI_MODULES
  done
  echo Adding triki modules $TRIKI_MODULES
fi

TRIKI_JAR=`ls -1 lib/triki*.jar`
export CLASSPATH=$CLASSPATH:$TRIKI_JAR:$TRIKI_MODULES

java $DEBUG -Dcontent_dir=$1 -Dport=$2 net.opentechnology.triki.core.boot.TrikiMain
