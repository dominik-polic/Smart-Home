#include <ESP8266WiFi.h>
#include <ESP8266mDNS.h>
#include <WiFiUdp.h>
#include <ArduinoOTA.h>
#include <Arduino.h>
#include <ESP8266WiFiMulti.h>
#include <ESP8266HTTPClient.h>

#define UPDATE_INTERVAL 1000
#define REQUEST_STATE "http://192.168.1.20/node_mysql.php?node=door_main&row=node_state_desired&action=read"
#define UPDATE_STATE_LOCKED "http://192.168.1.20/node_mysql.php?node=door_main&row=node_state_current&action=write&value=locked"
#define UPDATE_STATE_UNLOCKED "http://192.168.1.20/node_mysql.php?node=door_main&row=node_state_current&action=write&value=unlocked"
#define UPDATE_REQUEST_EXECUTED "http://192.168.1.20/node_mysql.php?node=door_main&row=node_state_desired&action=write&value=no_change"

ESP8266WiFiMulti WiFiMulti;
String payload="none";
long lastUpdate=0;

const char* ssid = "PoliNET";
const char* password = "12345678";

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

  // Port defaults to 8266
  // ArduinoOTA.setPort(8266);

  // Hostname defaults to esp8266-[ChipID]
  // ArduinoOTA.setHostname("myesp8266");

  // No authentication by default
  // ArduinoOTA.setPassword("admin");

  // Password can be set with it's md5 value as well
  // MD5(admin) = 21232f297a57a5a743894a0e4a801fc3
  // ArduinoOTA.setPasswordHash("21232f297a57a5a743894a0e4a801fc3");
  ArduinoOTA.setHostname("HOUSE_DOOR_ESP");
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
  digitalWrite(2,HIGH);
  delay(10);
  digitalWrite(2,LOW);
  lastUpdate=millis();
  wifiAction(REQUEST_STATE);
  executeAction(payload);

if(WiFi.status() != WL_CONNECTED) {
  ESP.restart();
}
}

if(Serial.available()){
  int lock_status=Serial.read(); //0-uncloked, 1-locked
  if(lock_status=='0'){
    wifiAction(UPDATE_STATE_UNLOCKED);
  }else if(lock_status='1'){    
    wifiAction(UPDATE_STATE_LOCKED);
  }
}


}


void executeAction(String action){
if(action=="OK:no_change"){
}else{
  if(action=="OK:unlocked"){
    Serial.write('0');
    wifiAction(UPDATE_REQUEST_EXECUTED);
  }else if(action=="OK:locked"){
    Serial.write('1');
    wifiAction(UPDATE_REQUEST_EXECUTED);
    
  }

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



