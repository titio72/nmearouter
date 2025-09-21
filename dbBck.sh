#!/bin/sh
mysqldump --password=$2 --user=$1 nmearouter track meteo trip > $3
tar -zcf ./web/$3.tgz $3
rm $3
