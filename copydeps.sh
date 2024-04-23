#!/bin/sh

cd web

echo "Backup nmearouter"
cp -f js/nmearouter.js .
cp -f css/nmearouter.css .

echo "Clean up"
rm -Rf css/*
rm -Rf fonts/*
rm -Rf js/*

echo "Restore nmearouter.js"
mv -f ./nmearouter.js js/
mv -f ./nmearouter.css css/

echo "Copy bootstrap"
cp -Rf ../node_modules/bootstrap/dist/js/* ./js
cp -Rf ../node_modules/bootstrap/dist/css/* ./css

#echo "Copy dark theme"
#cp -f ../node_modules/bootswatch/dist/slate/* ./css

echo "Copy angular"
cp -f ../node_modules/angular/angular.min.js js/
echo "Copy angular-sanitize"
cp -f ../node_modules/angular-sanitize/angular-sanitize.min.js js/
#echo "Copy bootbox"
#cp -f ../node_modules/bootbox/dist/bootbox.min.js js/
echo "Copy jquery"
cp -f ../node_modules/jquery/dist/jquery.min.js js/
echo "Copy Chart.js"
cp -f ../node_modules/chart.js/dist/Chart.min.js js/
echo "Copy moment-with-locales"
cp -f ../node_modules/moment/min/moment-with-locales.min.js js/
echo "Copy Google wrapper"
cp -f ../node_modules/google-maps/lib/Google.min.js js/
echo "Copy bootstrap-datepicker"
cp -f ../node_modules/bootstrap-datepicker/dist/css/bootstrap-datepicker.min.css css/
cp -f ../node_modules/bootstrap-datepicker/dist/js/bootstrap-datepicker.min.js js/
echo "Copy chartjs-plugin-zoom"
cp -f "../node_modules/chartjs-plugin-zoom/dist/chartjs-plugin-zoom.min.js" js/
echo "Copy hammer"
cp -f "../node_modules/hammerjs/hammer.min.js" js/

echo "Copy glypicons"
cp -Rf ../web_cache/fonts .
cp ../web_cache/css/glyphicons.css ./css

echo "Copy maplabel"
cp ../web_cache/js/maplabel.js ./js



