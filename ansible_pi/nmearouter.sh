#!/bin/bash
JAR=NMEARouter.jar
PATH_TO=/home/aboni/router
PATH_TO_JAR=$PATH_TO/$JAR
cd $PATH_TO || exit
/usr/bin/java -Xmx1536m -jar $PATH_TO_JAR -web > $PATH_TO/out.txt 2> $PATH_TO/err.txt < /dev/null &
