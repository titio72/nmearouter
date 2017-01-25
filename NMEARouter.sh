#!/bin/sh
SERVICE_NAME=NMEARouter
JAR=NMEARouter-0.1.2-SNAPSHOT.jar
PATH_TO=/home/aboni/router
PID_PATH_NAME=/var/run/NMEARouter.pid
PATH_TO_JAR=$PATH_TO/$JAR
case $1 in
    start)
        echo "Starting $SERVICE_NAME ..."
        if [ ! -f $PID_PATH_NAME ]; then
            echo "Changing work dir to $PATH_TO"
            cd $PATH_TO
            echo "Starting java "
            sudo -u aboni nohup /usr/bin/java -jar $PATH_TO_JAR -web /tmp 2>> /dev/null >> /dev/null &
                        echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is already running ..."
        fi
    ;;
    stop)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stopping ..."
            kill $PID;
            echo "$SERVICE_NAME stopped ..."
            rm $PID_PATH_NAME
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
    restart)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stopping ...";
            kill $PID;
            echo "$SERVICE_NAME stopped ...";
            rm $PID_PATH_NAME
            echo "$SERVICE_NAME starting ..."
            cd $PATH_TO
            sudo -u aboni nohup /usr/bin/java -jar $PATH_TO_JAR -web /tmp 2>> /dev/null >> /dev/null &
                        echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
esac 
