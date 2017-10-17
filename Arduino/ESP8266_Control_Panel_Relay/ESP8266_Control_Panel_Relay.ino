//Include all important libraries
#include <ESP8266WiFi.h>
#include <ESP8266mDNS.h>
#include <WiFiUdp.h>
#include <ArduinoOTA.h>
#include <Arduino.h>
#include <ESP8266WiFiMulti.h>
#include <ESP8266HTTPClient.h>

//Define configurable parameters
#define UPDATE_INTERVAL 500  //Update interval in ms
#define ARDUINO_TIMEOUT 250  //Time to wait for UART response from arduino
#define BAUD_RATE 9600  //Serial baued rate

//Define WiFi variables
ESP8266WiFiMulti WiFiMulti;
const char* ssid = "PoliNET";
const char* password = "12345678";

//Define important variables
String payload = "none";
long lastUpdate = 0;


//Status variable sets

//Define arduino current status variables
int arduino_lighton = -1;
int arduino_masterlightbrightness = -1;
int arduino_gate2locked = -1;
int arduino_rgbmode = -1;
int arduino_rgbspeed = -1;
int arduino_rgbbrightness = -1;
int arduino_redbrightness = -1;
int arduino_greenbrightness = -1;
int arduino_bluebrightness = -1;
int arduino_rgboverride = -1;





//Setup function - only ran on startup
void setup() {
  //On-board LED
  pinMode(2, OUTPUT);

  //Start serial for communication with Arduino MEGA2560
  Serial.begin(BAUD_RATE);

  //Setup WiFi
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  //If wifi is not connected -> reboot
  while (WiFi.waitForConnectResult() != WL_CONNECTED) {
    delay(5000);
    ESP.restart();
  }

  
  //Setup OTA server
  ArduinoOTA.setHostname("MAIN_PANEL_ESP");
  ArduinoOTA.onStart([]() {
    String type;
    if (ArduinoOTA.getCommand() == U_FLASH)
      type = "sketch";
    else // U_SPIFFS
      type = "filesystem";
  });
  ArduinoOTA.onEnd([]() {
  });
  ArduinoOTA.onProgress([](unsigned int progress, unsigned int total) {
  });
  ArduinoOTA.onError([](ota_error_t error) {
  });
  ArduinoOTA.begin();
}



//Loop function - runs all the time
void loop() {

  //Handle the OTA update request
  ArduinoOTA.handle();


  //Only update everything once in a while
  if (millis() > lastUpdate + UPDATE_INTERVAL) {
    lastUpdate = millis();

    //Check WiFi connection and reboot if needed
    if (WiFi.status() != WL_CONNECTED) {
      ESP.restart();
    }



    //All the parsing goes on here........

    //1. Read from arduino
    getArduinoStatus();

    //2. Update the status in MySQL
    updateMySQLCurrent();

    //3. Read desired status from MySQL and write the change to arduino
    getMySQLDesiredAndUpdate();

    flushTheSerial();

  }

  
}






//Function for communicating with the MySQL server via PHP
int webAction(String web_node, String web_row, String web_action, String web_value) {
  String web_code = "http://192.168.1.20/node_mysql.php?node=" + web_node + "&row=" + web_row + "&action=" + web_action + "&value=" + web_value;

  payload = "error";
  if ((WiFiMulti.run() == WL_CONNECTED)) {

    HTTPClient http;
    http.begin(web_code);
    int httpCode = http.GET();
    if (httpCode > 0) {
      if (httpCode == HTTP_CODE_OK) {
        payload = http.getString();
        payload.remove(0,3);
      }
    }

    http.end();
  }

  //Return 1 if operation successful, 0 otherwise
  if (payload == "error") return 0;
  return 1;
}



//Function to read from arduino
void getArduinoStatus() {

  //Update light status
  Serial.write("Ia");
  waitForSerial(&arduino_lighton);

  //Update master light brightness
  Serial.write("Ii");
  waitForSerial(&arduino_masterlightbrightness);

  //Update gate2 status
  Serial.write("Ib");
  waitForSerial(&arduino_gate2locked);

  //Update rgb mode
  Serial.write("Id");
  waitForSerial(&arduino_rgbmode);

  //Update rgb soeed
  Serial.write("Ie");
  waitForSerial(&arduino_rgbspeed);

  //Update rgb brighhtness
  Serial.write("If");
  waitForSerial(&arduino_rgbbrightness);

  //Update R/G/B brightness
  Serial.write("Ig");
  waitForSerial(&arduino_redbrightness);
  waitForSerial(&arduino_greenbrightness);
  waitForSerial(&arduino_bluebrightness);

  //Update rgb panel override status
  Serial.write("Ih");
  waitForSerial(&arduino_rgboverride);



}

//Function that waits for response from the arduino
void waitForSerial(int *wfs_address) {
  delay(50);
  long wfs_start_millis = millis();
  while (!Serial.available() && millis() < wfs_start_millis + ARDUINO_TIMEOUT);
  int nestotamo12=Serial.read();
  *wfs_address = nestotamo12;
  
}


//int webAction(String *payload, String web_node, String web_row, String web_action, String web_value)
//Function to upload current Arduino status to MySQL
void updateMySQLCurrent() {
  //Keep track of the errors
  int success = 1;

  //Update the light status
  if (arduino_lighton == 1 && success == 1) success = webAction("light_dominik", "node_state_current", "write", "on");
  else if (success == 1) success = webAction("light_dominik", "node_state_current", "write", "off");

  //Update the gate2 status
  if (arduino_gate2locked == 1 && success == 1) success = webAction("gate2", "node_state_current", "write", "closed");
  else if (success == 1) success = webAction("gate2", "node_state_current", "write", "open");

  //Update RGB mode
  if (success == 1) success = webAction("rgbmode_dominik", "node_state_current", "write", String(arduino_rgbmode));

  //Update rgb panel override status
  if (arduino_rgboverride == 1 && success == 1) success = webAction("rgboverride_dominik", "node_state_current", "write", "true");
  else if (success == 1) success = webAction("rgboverride_dominik", "node_state_current", "write", "false");

  //Update rgb speed
  if (success == 1) success = webAction("rgbspeed_dominik", "node_state_current", "write", String(arduino_rgbspeed));

  //Update rgb brightness
  if (success == 1) success = webAction("rgbbrightness_dominik", "node_state_current", "write", String(arduino_rgbbrightness));

  //Update light brightness
  if (success == 1) success = webAction("lightbrightness_dominik", "node_state_current", "write", String(arduino_masterlightbrightness));

  //Update rgb color
  if (success == 1) success = webAction("rgbcolor_dominik", "node_state_current", "write", String(arduino_redbrightness) + ":" + String(arduino_greenbrightness) + ":" + String(arduino_bluebrightness));


}


//Function to get the desired status from MySQL and send it to the arduino
void getMySQLDesiredAndUpdate() {

  //Define temporary variables
  int temp_success = 1;

  //Check for reset message
  if (temp_success == 1)  temp_success = webAction("reset", "node_state_desired", "read", "not_important");
  if (temp_success == 1&&payload == "arduino") {
    if (temp_success == 1)  temp_success = webAction("reset", "node_state_desired", "write", "no_change");
    Serial.write("R");
    delay(1000);
    ESP.reset();
  }

  //Check for light change
  if (temp_success == 1)  temp_success = webAction("light_dominik", "node_state_desired", "read", "not_important");
  
  if (temp_success == 1&&payload == "on") Serial.write("+");
  if (temp_success == 1&&payload == "off") Serial.write("-");
  if (temp_success == 1 && payload!="no_change") temp_success = webAction("light_dominik", "node_state_desired", "write", "no_change");

  //Check for gate2 change
  if (temp_success == 1)  temp_success = webAction("gate2", "node_state_desired", "read", "not_important");
  if (temp_success == 1&&payload == "open") Serial.write("F");
  if (temp_success == 1&&payload == "closed") Serial.write("f");
  if (temp_success == 1 && payload!="no_change") temp_success = webAction("gate2", "node_state_desired", "write", "no_change");
  
  //Check for test_led change
  if (temp_success == 1)  temp_success = webAction("test_led", "node_state_desired", "read", "not_important");
  if (temp_success == 1&&payload == "pulse") Serial.write("b");
  if (temp_success == 1 && payload!="no_change") temp_success = webAction("test_led", "node_state_desired", "write", "no_change");

  //Check for rgb mode change
  if (temp_success == 1)  temp_success = webAction("rgbmode_dominik", "node_state_desired", "read", "not_important");
  if (temp_success == 1&&payload == "0") Serial.write("p");
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

  //Check for rgb panel override change
  if (temp_success == 1)  temp_success = webAction("rgboverride_dominik", "node_state_desired", "read", "not_important");
  if (payload == "true") Serial.write("a");
  if (temp_success == 1 && payload!="no_change") temp_success = webAction("rgboverride_dominik", "node_state_desired", "write", "no_change");

  //Check for my door lock change
  if (temp_success == 1)  temp_success = webAction("door_dominik", "node_state_desired", "read", "not_important");
  if (temp_success == 1&&payload == "locked") Serial.write("X");
  if (temp_success == 1&&payload == "unlocked") Serial.write("Y");
  if (temp_success == 1 && payload!="no_change") temp_success = webAction("door_dominik", "node_state_desired", "write", "no_change");

  //Check for bell change
  if (temp_success == 1)  temp_success = webAction("bell", "node_state_desired", "read", "not_important");
  if (temp_success == 1&&payload == "pulse") Serial.write("Z");
  if (temp_success == 1 && payload!="no_change") temp_success = webAction("bell", "node_state_desired", "write", "no_change");

  //Check for gate1 change
  if (temp_success == 1)  temp_success = webAction("gate1", "node_state_desired", "read", "not_important");
  if (temp_success == 1&&payload == "pulse") Serial.write("g");
  if (temp_success == 1 && payload!="no_change") temp_success = webAction("gate1", "node_state_desired", "write", "no_change");


}


//To aviod sync loss
void flushTheSerial(){
  int temp_something;
  while(Serial.available())
    temp_something=Serial.read();

}


