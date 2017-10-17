echo 0 | sudo tee /sys/class/leds/led0/brightness
echo 0 | sudo tee /sys/class/leds/led1/brightness
/home/pi/env/bin/python3 -u assistant.py
