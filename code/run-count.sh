#!/bin/bash

BASEDIR=/root
DFSDIR=/user/wikititle

# Setup on First Run
if [ ! -f "${BASEDIR}/.built" ] ; then
    ${BASEDIR}/code/build.sh
fi
if [ ! -f "${BASEDIR}/.precount" ] ; then
    touch ${BASEDIR}/.precount
    hadoop dfs -mkdir ${DFSDIR}
    hadoop dfs -mkdir ${DFSDIR}/input
    hadoop dfs -put ${BASEDIR}/data/enwiki-20090810-all-titles-in-ns0 ${DFSDIR}/input/
fi


# Cleanup old run
hadoop dfs -rmr ${DFSDIR}/output-count

# run
hadoop jar ${BASEDIR}/wikititlecount.jar org.asayler.WikiTitleCount -D mapred.output.compress=false ${DFSDIR}/input ${DFSDIR}/output-count
touch ${BASEDIR}/.presort
