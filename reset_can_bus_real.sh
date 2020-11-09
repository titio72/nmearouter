#!/bin/sh
date >> rest_can.log
sudo ifconfig can0 down
sudo ip link set can0 type can bitrate 250000
