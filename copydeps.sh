#!/bin/sh

cd web
echo "Clean up"
rm -Rf css/*
rm -Rf fonts/*

echo "Backup nmearouter.js"
cp -f js/nmearouter.js .
rm -Rf js/*
mv -f ./nmearouter.js js/

echo "Copy bootstrap"
cp -Rf ../node_modules/bootstrap/dist/js/* ./js
cp -Rf ../node_modules/bootstrap/dist/css/* ./css
cp -Rf ../node_modules/bootstrap/dist/fonts/* ./fonts

echo "Copy dark theme"
cp -f ../node_modules/bootswatch/darkly/* ./css

echo "Copy angular"
cp -f ../node_modules/angular/angular.min.js js/
echo "Copy angular-sanitize"
cp -f ../node_modules/angular-sanitize/angular-sanitize.min.js js/
echo "Copy bootbox"
cp -f ../node_modules/bootbox/bootbox.min.js js/
echo "Copy jquery"
cp -f ../node_modules/jquery/dist/jquery.min.js js/
echo "Copy Chart.js"
cp -f ../node_modules/chart.js/dist/Chart.min.js js/
echo "Copy moment-with-locales"
cp -f ../node_modules/moment/min/moment-with-locales.min.js js/
cp -f ../node_modules/moment/moment.js js/

echo "Copy bootstrap-datepicker"
cp -f ../node_modules/bootstrap-datepicker/dist/css/bootstrap-datepicker.min.css css/
cp -f ../node_modules/bootstrap-datepicker/dist/js/bootstrap-datepicker.min.js js/
