#include <ESP8266WiFi.h>
#include <ESP8266mDNS.h>
#include <WiFiUdp.h>
#include <ArduinoOTA.h>
#include <Arduino.h>
#include <ESP8266WiFiMulti.h>
#include <ESP8266HTTPClient.h>

#define UPDATE_INTERVAL 1000

#define UPDATE_INTERVAL 1000
#define REQUEST_MODE "http://192.168.1.20/node_mysql.php?node=rgbmode_tv&row=node_state_desired&action=read"
#define REQUEST_COLOR "http://192.168.1.20/node_mysql.php?node=rgbcolor_tv&row=node_state_desired&action=read"
#define REQUEST_SPEED "http://192.168.1.20/node_mysql.php?node=rgbspeed_tv&row=node_state_desired&action=read"
#define REQUEST_BRIGHTNESS "http://192.168.1.20/node_mysql.php?node=rgbbrightness_tv&row=node_state_desired&action=read"


ESP8266WiFiMulti WiFiMulti;
String payload="none";
long lastUpdate=0;

const char* ssid = "PoliNET";
const char* password = "12345678";

int rgbMode=0;
int rgbBrightness=0;
int rgbSpeed=0;
int redBrightnesss=0;
int greenBrightness=0;
int blueBrightness=0;

void setup() {
  pinMode(2,OUTPUT);
  Serial.begin(115200);
  //Serial.println("Booting");
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  while (WiFi.waitForConnectResult() != WL_CONNECTED) {
    //Serial.println("Connection Failed! Rebooting...");
    delay(5000);
  WiFi.begin(ssid, password);
  }

  ArduinoOTA.setHostname("RGB_TV_ESP");
  ArduinoOTA.onStart([]() {
    String type;
    if (ArduinoOTA.getCommand() == U_FLASH)
      type = "sketch";
    else // U_SPIFFS
      type = "filesystem";

    // NOTE: if updating SPIFFS this would be the place to unmount SPIFFS using SPIFFS.end()
    //Serial.println("Start updating " + type);
  });
  ArduinoOTA.onEnd([]() {
    //Serial.println("\nEnd");
  });
  ArduinoOTA.onProgress([](unsigned int progress, unsigned int total) {
    //Serial.printf("Progress: %u%%\r", (progress / (total / 100)));
  });
  ArduinoOTA.onError([](ota_error_t error) {
    //Serial.printf("Error[%u]: ", error);
    /*if (error == OTA_AUTH_ERROR) Serial.println("Auth Failed");
    else if (error == OTA_BEGIN_ERROR) Serial.println("Begin Failed");
    else if (error == OTA_CONNECT_ERROR) Serial.println("Connect Failed");
    else if (error == OTA_RECEIVE_ERROR) Serial.println("Receive Failed");
    else if (error == OTA_END_ERROR) Serial.println("End Failed");*/
  });
  ArduinoOTA.begin();
  //Serial.println("Ready");
  //Serial.print("IP address: ");
  //Serial.println(WiFi.localIP());
}

void loop() {
  ArduinoOTA.handle();
if(millis()>lastUpdate+UPDATE_INTERVAL){

  lastUpdate=millis();
  wifiAction(REQUEST_STATE);
  executeAction(payload);

if(WiFi.status() != WL_CONNECTED) {
  ESP.restart();
}
}


}


void executeAction(String action){
  temp_success=1;
//Check for rgb mode change
  wifiAction(REQUEST_MODE);
switch()
  
  if (temp_success == 1&&payload== "0") Serial.write("p");
  if (temp_success == 1&&payload == "1") Serial.write("t");
  if (temp_success == 1&&payload == "2") Serial.write("c");
  if (temp_success == 1&&payload == "3") Serial.write("d");
  if (temp_success == 1&&payload == "4") Serial.write("o");
  if (temp_success == 1&&payload == "5") Serial.write("u");
  if (temp_success == 1 && payload!="no_change") temp_success = webAction("rgbmode_dominik", "node_state_desired", "write", "no_change");

  //Check for RGB speed change
  if (temp_success == 1)  temp_success = webAction("rgbspeed_dominik", "node_state_desired", "read", "not_important");
  if (temp_success == 1&&payload != "no_change") {
    Serial.write("k");
    Serial.write(payload.toInt());
  }
  if (temp_success == 1 && payload!="no_change") temp_success = webAction("rgbspeed_dominik", "node_state_desired", "write", "no_change");

  //Check for RGB brightness change
  if (temp_success == 1)  temp_success = webAction("rgbbrightness_dominik", "node_state_desired", "read", "not_important");
  if (temp_success == 1&&payload != "no_change") {
    Serial.write("l");
    Serial.write(payload.toInt());
  }
  if (temp_success == 1 && payload!="no_change") temp_success = webAction("rgbbrightness_dominik", "node_state_desired", "write", "no_change");

  //Check for light brightness change
  if (temp_success == 1)  temp_success = webAction("lightbrightness_dominik", "node_state_desired", "read", "not_important");
  if (temp_success == 1&&payload != "no_change") {
    Serial.write("L");
    Serial.write(payload.toInt());
  }
  if (temp_success == 1 && payload!="no_change") temp_success = webAction("lightbrightness_dominik", "node_state_desired", "write", "no_change");


  //Check for RGB color change
  if (temp_success == 1)  temp_success = webAction("rgbcolor_dominik", "node_state_desired", "read", "not_important");
  if (temp_success == 1&&payload != "no_change") {
    Serial.write("n");
    
    String temp_nesto = "000";
    char temp_str[20] = "000:000:000";
    payload.toCharArray(temp_str, 20);
    const char temp_s[2] = ":";
    char *temp_token;
    temp_token = strtok(temp_str, temp_s);

    while ( temp_token != NULL ){
      temp_nesto = temp_token;
      Serial.write(temp_nesto.toInt());
      temp_token = strtok(NULL, temp_s);
      
    }


  }
  if (temp_success == 1 && payload!="no_change") temp_success = webAction("rgbcolor_dominik", "node_state_desired", "write", "no_change");



  

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



