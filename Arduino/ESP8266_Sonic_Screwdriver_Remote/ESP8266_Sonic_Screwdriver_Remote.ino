#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ArduinoOTA.h>
#include <ESP8266WiFiMulti.h>
#include <ESP8266HTTPClient.h>
#define pin1 4
#define pin2 14
#define pin3 12
#define pin4 13
#define pin5 3
#define pin6 1
#define WAIT_TIME 1000


#define LIGHT_ON "http://192.168.1.5/writer_mysql.php?user=sonic&action=on&node=light_dominik"
#define LIGHT_OFF "http://192.168.1.5/writer_mysql.php?user=sonic&action=off&node=light_dominik"
#define DOOR_MAIN_LOCKED "http://192.168.1.5/writer_mysql.php?user=sonic&action=locked&node=door_main"
#define DOOR_MAIN_UNLOCKED "http://192.168.1.5/writer_mysql.php?user=sonic&action=unlocked&node=door_main"
#define DOOR_DOMINIK_LOCKED "http://192.168.1.5/writer_mysql.php?user=sonic&action=locked&node=door_dominik"
#define DOOR_DOMINIK_UNLOCKED "http://192.168.1.5/writer_mysql.php?user=sonic&action=unlocked&node=door_dominik"
#define GATE2_CLOSED "http://192.168.1.5/writer_mysql.php?user=sonic&action=closed&node=gate2"
#define GATE2_OPEN "http://192.168.1.5/writer_mysql.php?user=sonic&action=open&node=gate2"
#define GATE1_PULSE "http://192.168.1.5/writer_mysql.php?user=sonic&action=pulse&node=gate1"
#define BELL_PULSE "http://192.168.1.5/writer_mysql.php?user=sonic&action=pulse&node=bell"
#define RGB_DISABLED "http://192.168.1.5/writer_mysql.php?user=sonic&action=0&node=rgbmode_dominik"
#define RGB_FADE "http://192.168.1.5/writer_mysql.php?user=sonic&action=2&node=rgbmode_dominik"
#define RGB_BLINK "http://192.168.1.5/writer_mysql.php?user=sonic&action=3&node=rgbmode_dominik"
#define RGB_CUSTOM "http://192.168.1.5/writer_mysql.php?user=sonic&action=1&node=rgbmode_dominik"
#define COOL_KICK "http://192.168.1.5/writer_mysql.php?user=sonic&action=true&node=rgboverride_dominik"

ESP8266WiFiMulti WiFiMulti;
String payload="none";

boolean isActive=false;
boolean lastButton=false;

int pin[6]={pin1,pin2,pin3,pin4,pin5,pin6};

char buffer[1024];
const char* ssid = "PoliNET";
const char* password = "12345678";
long count=0;
long brightness=0;
boolean countdown=false;
int codebitmask=0;
char bitmask_msg[150];
long lastAction=0;



void setup() {
  
    

pinMode(pin1,INPUT_PULLUP);
pinMode(pin2,INPUT_PULLUP);
pinMode(pin3,INPUT_PULLUP);
pinMode(pin4,INPUT_PULLUP);
pinMode(pin5,INPUT_PULLUP);
pinMode(pin6,INPUT_PULLUP);
pinMode(2,OUTPUT);
pinMode(5,OUTPUT);
digitalWrite(2,HIGH);
digitalWrite(5,LOW);


  WiFiMulti.addAP("PoliNET", "12345678");



   ArduinoOTA.setHostname("ESP09");

  

  ArduinoOTA.onStart([]() {
    String type;
    if (ArduinoOTA.getCommand() == U_FLASH)
      type = "sketch";
    else // U_SPIFFS
      type = "filesystem";

 
    // NOTE: if updating SPIFFS this would be the place to unmount SPIFFS using SPIFFS.end()

  });
  ArduinoOTA.onEnd([]() {
  });
  ArduinoOTA.onProgress([](unsigned int progress, unsigned int total) {
  });
  ArduinoOTA.onError([](ota_error_t error) {

    
  });
  ArduinoOTA.begin();
}

void loop() {
  codebitmask=!digitalRead(pin5)*1+!digitalRead(pin4)*2+!digitalRead(pin3)*4+!digitalRead(pin2)*8+!digitalRead(pin1)*16;
  ArduinoOTA.handle();



if(digitalRead(pin6)==LOW&&lastButton==false){
  isActive=true;
  delay(20);
  lastButton=true;
  //sprintf(bitmask_msg,"http://192.168.1.5/writer_mysql.php?user=sonic&action=%d&node=sonic_screwdriver",codebitmask);
  
  if(millis()>lastAction+WAIT_TIME){
  parseAction(codebitmask);
  lastAction=millis();
  }
}else if(digitalRead(pin6)==HIGH){
  lastButton=false;
  isActive=false;
}


if(isActive==true){
 
   count = count+1;
 if(count<5)
 {
   tone(5,1471);
   delay(10);
   tone(5,1575);
   delay(5);
 }
 else
 {
   tone(5,1470);
   delay(10);
   tone(5,1575);
   delay(5);
   if(count>10)
   {
     count=0;
   }
 }
 

 
digitalWrite(2,LOW);
}else{
tone(5,0);
digitalWrite(2,HIGH);

}
}


void wifiAction(String req_code){
  payload="error";
  if((WiFiMulti.run() == WL_CONNECTED)) {
  
          HTTPClient http;
          http.begin(req_code);
          int httpCode = http.GET();  
          if(httpCode > 0) {
              if(httpCode == HTTP_CODE_OK) {
                  payload = http.getString();                  
              }
          }
  
          http.end();
      }

}

void parseAction(int bitmask){
  String msg;
switch(bitmask){
case 0b00000:
msg=DOOR_MAIN_UNLOCKED;
break;
case 0b00001:
msg=DOOR_MAIN_LOCKED;
break;
case 0b00010:
msg=GATE1_PULSE;
break;
//Undefined: 0b00011
case 0b00100:
msg=GATE2_OPEN;
break;
case 0b00101:
msg=GATE2_CLOSED;
break;
case 0b00110:
msg=DOOR_DOMINIK_UNLOCKED;
break;
case 0b00111:
msg=DOOR_DOMINIK_LOCKED;
break;
case 0b01000:
msg=LIGHT_OFF;
break;
case 0b01001:
msg=LIGHT_ON;
break;
//Undefined: 0b0101x,0b10x,0b11x
case 0b10000:
msg=RGB_DISABLED;
break;
//Undefined: 0b10001
case 0b10010:
msg=RGB_FADE;
break;
//Undefined: 0b10011
case 0b10100:
msg=RGB_BLINK;
break;
//Undefined: 0b10101
case 0b10110:
msg=RGB_CUSTOM;
break;
//Undefined: 0b10111
case 0b11000:
msg=BELL_PULSE;
break;
//Undefined: 0b11001
case 0b11010:
msg=COOL_KICK;
break;
//Undefined: 0b11011,0b1110x,0b1111x


default:
break;
}

wifiAction(msg);
}


