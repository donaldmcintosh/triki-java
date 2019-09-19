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

# Colon separated paths
if [ ! -z ${TRIKI_MODULES} ]
then
  for jar in `echo $TRIKI_MODULES | sed "s/:/ /g"`; do
    MODULES_CLASSPATH=$MODULES_CLASSPATH:`ls -1 $LOCAL_DEPLOY/modules/*$jar*`
  done
  echo Adding triki modules $MODULES_CLASSPATH
fi

TRIKI_JAR=`ls -1 lib/triki*.jar`
export CLASSPATH=$CLASSPATH:$TRIKI_JAR:$MODULES_CLASSPATH

java $DEBUG -Dcontent_dir=$1 -Dport=$2 net.opentechnology.triki.core.boot.TrikiMain
