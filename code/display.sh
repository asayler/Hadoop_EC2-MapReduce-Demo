#!/bin/bash

BASEDIR=/root
DFSDIR=/user/wikititle

# Setup on First Run
if [ ! -f "${BASEDIR}/.predisplay" ] ; then
    ${BASEDIR}/code/run-sort.sh
fi

# Display Top Words
hadoop dfs -text ${DFSDIR}/output-sort/part-00000 | tail -n 25
