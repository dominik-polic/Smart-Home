  /*
-Created by Dominik Polic on 2016-3-22
-Libraries required: EEPROM.h, Keypad.h and a4988.h
-Feel free to use or modify this code as much as you want

Description/user manual:
This program uses the numeric keypad (matrix keyboard) from ebay to allow you to lock/unlock your door with a pin. 
The pin is stored in EEPROM and is saved when the device looses power. 
You can change the pin by typing: A3250 followed by the new 4-digit pin. Be careful though, there is a timeout so you need to be fast enough. 
You might also use an external signal to trigger the lock/unlock by pulling pins A3 or A4 low. 
The default configured keypad is 4x4, so use pins 2...9 for it (just plug hte connector to those pins, and if you encounter problems, reverse it). 
If you enter the wrong digit press the * key or wait a few seconds and enter the pin from the start.
If your door does not unlock completely, don't worry. Press the "B" key, followed by your pin to unlock it a bit more ;)

Connection diagram:
External device:
-Lock signal->A4
-Unlock signal->A3
A4988:
-MS1->A0
-MS2->A1
-MS3->A2
-DIR->12
-STEP->11
-ENABLE->10

*/

//Start of the actual program:




#include <EEPROM.h>
#include <Keypad.h>
#include <a4988.h>

#define unlockturns 2.5 //Defines how many turns of a motor are required to lock/unlock your door

#define stateAddr 0
#define digit1Addr 1
#define digit2Addr 2
#define digit3Addr 3
#define digit4Addr 4

#define remoteUnlockPin A3
#define remoteLockPin A4

byte stateCurrent=0;//0-unlocked, 1-locked

//Matrix keypad definitions
const byte ROWS = 4; //four rows
const byte COLS = 4; //four columns

//This is the default unlock pin (0000). You can change it here, or via the matrix keypad
byte digit1='0';
byte digit2='0';
byte digit3='0';
byte digit4='0';
char temp='q';

//Changing these digits will change the MASTER PIN. Default is 3250
char master1='3';
char master2='2';
char master3='5';
char master4='0';

//define the symbols on the buttons of the keypads
char hexaKeys[ROWS][COLS] = {
  {'1','2','3','A'},
  {'4','5','6','B'},
  {'7','8','9','C'},
  {'*','0','#','D'}
};

//Define the Matrix Keypad pins
byte colPins[ROWS] = {5, 4, 3, 2}; //connect to the row pinouts of the keypad
byte rowPins[COLS] = {9, 8, 7, 6}; //connect to the column pinouts of the keypad

//initialize an instance of class NewKeypad
Keypad customKeypad = Keypad( makeKeymap(hexaKeys), rowPins, colPins, ROWS, COLS); 





//Define A4988 pinout
#define MS1PIN A0
#define MS2PIN A1
#define MS3PIN A2
#define DIRPIN 12
#define STEPPIN 11
#define ENABLEPIN 10
#define MOTOR_STEPS 200

a4988 myA4988(MOTOR_STEPS, MS1PIN, MS2PIN, MS3PIN, DIRPIN, ENABLEPIN, STEPPIN);



void setup(){


  digit1=EEPROM.read(digit1Addr);
  digit2=EEPROM.read(digit2Addr);
  digit3=EEPROM.read(digit3Addr);
  digit4=EEPROM.read(digit4Addr);
  stateCurrent=EEPROM.read(stateAddr);
  Serial.begin(9600);


myA4988.enable(1);
  myA4988.setStepMode(16);
  myA4988.setDelay(500);
  delay(10000);
  
  
}
  
void loop(){
  if(analogRead(remoteUnlockPin)>900)   changeLockState(1);  
  if(analogRead(remoteLockPin)>900)  changeLockState(2);

 
  char customKey = customKeypad.getKey();  
    
    switch(customKey){
      case NO_KEY:
      break;
      case '*':
      break;
      
      case 'A':
        if(customKeypad.waitForKey()==master1&&customKeypad.waitForKey()==master2&&customKeypad.waitForKey()==master3&&customKeypad.waitForKey()==master4){
         Serial.println("CHANGING KEY!!!");
        digit1=customKeypad.waitForKey2(); 
        digit2=customKeypad.waitForKey2(); 
        digit3=customKeypad.waitForKey2(); 
        digit4=customKeypad.waitForKey2(); 
        EEPROM.write(digit1Addr,digit1);
        EEPROM.write(digit2Addr,digit2);
        EEPROM.write(digit3Addr,digit3);
        EEPROM.write(digit4Addr,digit4);
        Serial.print("NEW KEY: ");
        Serial.write(digit1);
        Serial.write(digit2);
        Serial.write(digit3);
        Serial.write(digit4);
        }
      
      break;
      
      case 'B':
        if(customKeypad.waitForKey()==digit1&&customKeypad.waitForKey()==digit2&&customKeypad.waitForKey()==digit3&&customKeypad.waitForKey()==digit4){
          emergencyUnlock();
          
          
        }      
      break;
      
      
      
      default:
        if(customKey==digit1&&customKeypad.waitForKey()==digit2&&customKeypad.waitForKey()==digit3&&customKeypad.waitForKey()==digit4){
         
            Serial.print("Unlock");
            changeLockState(0);
           while(customKeypad.getKey()!=NO_KEY); 
          
        }else{
         Serial.print("Wrong code"); 
        }
      break;
      
    }
}



void changeLockState(int action){

myA4988.enable(0);

 if(stateCurrent==0&&(action==0||action==2)){   
   stateCurrent=1;
   EEPROM.write(stateAddr,stateCurrent);   
   //LOCK
  lock();



   
 }else if(stateCurrent==1&&(action==0||action==1)){
   stateCurrent=0;
   EEPROM.write(stateAddr,stateCurrent);
   //UNLOCK
   unlock();
   
   
 }
 
 myA4988.enable(1);
 
}

void unlock(){
 myA4988.setStepMode(16);
  myA4988.setDelay(600);  
  myA4988.setDirection(0);
  myA4988.step(int(3200*unlockturns)); 
}

void lock(){
 myA4988.setStepMode(16);
  myA4988.setDelay(600);  
  myA4988.setDirection(1);
  myA4988.step(int(3200*unlockturns)); 
}

void emergencyUnlock(){
  myA4988.enable(0);
   myA4988.setStepMode(16);
  myA4988.setDelay(2000);  
  myA4988.setDirection(0);
  myA4988.step(int(1800)); 
  myA4988.enable(1);
}
