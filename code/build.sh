#!/bin/bash

BASEDIR=/root
DFSDIR=/user/wikititle

# Setup on First Run
if [ ! -d "${BASEDIR}/wiki_count_classes" ] ; then
    mkdir ${BASEDIR}/wiki_count_classes
fi
if [ ! -d "${BASEDIR}/wiki_sort_classes" ] ; then
    mkdir ${BASEDIR}/wiki_sort_classes
fi

# Compile Code
javac -classpath /usr/local/hadoop-0.19.0/hadoop-0.19.0-core.jar -d ${BASEDIR}/wiki_count_classes/ ${BASEDIR}/code/WikiTitleCount.java
jar -cvf ${BASEDIR}/wikititlecount.jar -C ${BASEDIR}/wiki_count_classes/ .
javac -classpath /usr/local/hadoop-0.19.0/hadoop-0.19.0-core.jar -d ${BASEDIR}/wiki_sort_classes/ ${BASEDIR}/code/WikiTitleSort.java
jar -cvf ${BASEDIR}/wikititlesort.jar -C ${BASEDIR}/wiki_sort_classes/ .
touch ${BASEDIR}/.built
