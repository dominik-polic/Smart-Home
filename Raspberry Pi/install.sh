#!/bin/sh

#This is my custom MagicMirror install script. 
#It's probably not complete or working at all, but I will try to add all the changes I made to the pi to it. 
#Wish me luck :)


if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 
   exit 1
fi

