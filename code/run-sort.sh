#!/bin/bash

BASEDIR=/root
DFSDIR=/user/wikititle

# Setup on First Run
if [ ! -f "${BASEDIR}/.presort" ] ; then
    ${BASEDIR}/code/run-count.sh
fi

# Cleanup old run
hadoop dfs -rmr ${DFSDIR}/output-sort

# run
hadoop jar ${BASEDIR}/wikititlesort.jar org.asayler.WikiTitleSort -D mapred.output.compress=false ${DFSDIR}/output-count ${DFSDIR}/output-sort
touch ${BASEDIR}/.predisplay
touch ${BASEDIR}/.pregrab
