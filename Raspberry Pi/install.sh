#!/bin/sh

#This is my custom MagicMirror install script. 
#It's probably not complete or working at all, but I will try to add all the changes I made to the pi to it. 
#Wish me luck :)


if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 
   exit 1
fi

#Update the Raspbian to the latest version
apt-get update
apt-get upgrade

#Install MagicMirror software from http://magicmirror.builders
bash -c "$(curl -sL https://raw.githubusercontent.com/MichMich/MagicMirror/master/installers/raspberry.sh)"

#Enable OpenGL to decrease electron CPU usage
echo "dtoverlay=vc4-kms-v3d" >> /boot/config.txt

#Rotate display with OpenGL enabled
echo "@xrandr --output HDMI-1 --rotate right" >> ~/.config/lxsession/LXDE-pi/autostart

#Disable low power wargings
echo "avoid_warnings=1 " >> /boot/config.txt

#Install unclutter do Autohide Mouse Pointer
sudo apt-get install unclutter

#Enalbe unclutter with 3 second disappearing delay
echo "@unclutter -display :0 -idle 3 -root -noevents" >> ~/.config/lxsession/LXDE-pi/autostart

#Disable screensaver
echo "@xset s noblank" >> ~/.config/lxsession/LXDE-pi/autostart
echo "@xset s off" >> ~/.config/lxsession/LXDE-pi/autostart
echo "@xset -dpm" >> ~/.config/lxsession/LXDE-pi/autostart
echo "xserver-command=X -s 0 -dpms" >> /etc/lightdm/lightdm.conf

#Disable WiFi Power save

cat << EOF | sudo tee /etc/network/if-up.d/off-power-manager
#!/bin/sh
# off-power-manager - Disable the internal power manager of the (built-in) wlan0 device
# Added by MagicMirrorSetup
iw dev wlan0 set power_save off
EOF

sudo chmod 755 /etc/network/if-up.d/off-power-manager
sudo /etc/init.d/networking restart



#Install apache
apt install apache2
chown -R pi:www-data /var/www/html/
chmod -R 770 /var/www/html/

#Install php
apt install php php-mbstring
rm /var/www/html/index.html
echo "<?php phpinfo ();?>" > /var/www/html/index.php

#Install MySQL
apt install mysql-server php-mysql
mysql --user=root
#-----MANUAL STEP------
#DROP USER 'root'@'localhost';
#CREATE USER 'root'@'%' IDENTIFIED BY 'dominik325';
#GRANT ALL PRIVILEGES ON *.* TO 'root'@'%';
#exit
#edit /etc/mysql/mariadb.conf.d/50-server.cnf change to #bind-address 127.0.0.1

#Install pm2 process manager
npm install -g pm2
pm2 startup
sudo env PATH=$PATH:/usr/bin /usr/lib/node_modules/pm2/bin/pm2 startup systemd -u pi --hp /home/pi

#Startup MagicMirror with pm2
cd ~
echo "echo 0 | sudo tee /sys/class/leds/led0/brightness" >> mm.sh
echo "echo 0 | sudo tee /sys/class/leds/led1/brightness" >> mm.sh
echo "cd ~/MagicMirror" >> mm.sh
echo "DISPLAY=:0 npm start" >> mm.sh
chmod +x mm.sh
pm2 start mm.sh
pm2 save

#Install MM modules
cd ~/MagicMirror/modules
git clone https://github.com/BenRoe/MMM-SystemStats
cd MMM-SystemStats/ && npm install

cd ~/MagicMirror/modules
git clone https://github.com/Jopyth/MMM-Remote-Control.git
cd MMM-Remote-Control && npm install

cd ~/MagicMirror/modules
git clone https://github.com/shbatm/MMM-RTSPStream.git
cd MMM-RTSPStream && npm install
#config with "http-server -p 9999"

cd ~/MagicMirror/modules
git clone https://github.com/amcolash/MMM-json-feed

cd ~/MagicMirror/modules
git clone https://github.com/CFenner/MMM-LocalTransport

cd ~/MagicMirror/modules
git clone https://github.com/jclarke0000/MMM-MyCommute.git
cd MMM-MyCommute && npm install



#Resolution fix 1440*900
echo "hdmi_group=2" >> /boot/config.txt
echo "hdmi_mode=46" >> /boot/config.txt


#Chromium install for youtube (chrome-driver)
echo "deb http://security.debian.org/debian-security stretch/updates main" >> /etc/apt/sources.list
apt-get update && sudo apt-get upgrade

apt-get install chromium
apt-get install chromium-driver

#Install PiYouTube
git clone https://github.com/Spajker7/PiYouTube/

pm2 start youtube.sh
pm2 save

#Fix sound
sudo nano /lib/modprobe.d/aliases.conf
#Manual step: add/edit lines:
#options snd-usb-audio index=0
#options snd slots=snd_usb_audio


sudo nano /etc/modprobe.d/alsa-blacklist.conf
#Manual step: add/edit lines:
#blacklist snd_bcm2835






