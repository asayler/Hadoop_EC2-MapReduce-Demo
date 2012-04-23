#!/bin/bash

BASEDIR=/root
DFSDIR=/user/wikititle

# Setup on First Run
if [ ! -f "${BASEDIR}/.pregrab" ] ; then
    ${BASEDIR}/code/run-count.sh
fi

# Cleanup old data
rm -f ${BASEDIR}/data/part-00000
hadoop dfs -get ${DFSDIR}/output-sort/part-00000 ${BASEDIR}/data/
