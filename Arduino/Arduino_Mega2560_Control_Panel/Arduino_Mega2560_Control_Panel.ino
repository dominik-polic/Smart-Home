//-----LIBRARIES-----
#include <EEPROM.h>
#include <OneWire.h>                //Temperature sensor
#include <DallasTemperature.h>      //Temperature sensor
#include <Encoder.h>
#include <Wire.h> 
#include <LiquidCrystal_I2C.h>
#include <Servo.h>

//-----EEPROM ADDRESSES-----
#define EEPROM_GATE2_ADDR 0            //Gate2 status memory

//-----CONSTANTS-----
#define DEBOUNCE_DELAY 55          //Delay for on-board buttons
#define GATE2_DELAY 15000          //Time to wait for gate2 to open/close
#define MAX_LIGHT_VAL 255          //Maximum light brightness (to prevent LEDs from dying)
#define POT_MIN 5                  //Min potvalue to compensate for imperfect pots
#define POT_MAX 1018               //Max pot value to compensate for imperfect pots
#define LCD_MODES_MAX 8            //Number of options LCD can scroll through
#define BACKLIGHT_DURATION 5000    //Backlight on duration
#define LCD_UPDATE_INTERVAL 1000   //LCD update interval
#define T_UPDATE_INTERVAL 30000    //Temperature update interval
#define COOL_KICK_MAX_TIME 2500    //Maximum time to wait for kick to success
#define WIFI_SERIAL_WAIT 1000      //Time to wait for Serial message from ESP8266

//-----PIN DEFINITIONS-----
//Rotary encoder
#define PIN_MODE_0   45              //Input - Rotary switch mode 0
#define PIN_MODE_1   53              //Input - Rotary switch mode 1
#define PIN_MODE_2   51              //Input - Rotary switch mode 2
#define PIN_MODE_3   49              //Input - Rotary switch mode 3
#define PIN_MODE_4   47              //Input - Rotary switch mode 4
#define PIN_MODE_5   43              //Input - Rotary switch mode 5
//Toggle switches
#define PIN_ENABLE_KEYBOARD 52       //Input - Keyboard enable [TODO]
#define PIN_ENABLE_RGB 50            //Input - RGB enable
#define PIN_ENABLE_BT 48             //Input - Bluetooth enable [TODO]
#define PIN_ENABLE_WIFI 46           //Input - WiFi enable [TODO]
#define PIN_ENABLED_OVERRIDE 40      //Input - RGB panel override
//Buttons
#define PIN_RESET_WIFI 44            //Input - Wifi Reconnect [TODO]
#define PIN_RESET_ARDUINO 42         //Input - Reset Arduino
#define PIN_BUTTON_GATE2 41          //Input - Open/close gate2
#define PIN_BUTTON_GATE1 39          //Input - Open gate1
#define PIN_BUTTON_LIGHT 37          //Input - Turn light on/off
#define PIN_BUTTON_BELL 35           //Input - Ring a bell
#define PIN_BUTTON_OVERRIDE 31       //Input - Enable RGB override
//Rotary Encoder
#define PIN_ENCODER_1 2              //Input/Interrupt - Rotary encoder pin1
#define PIN_ENCODER_2 A1             //Input - Rotary encoder pin2
#define PIN_ENCODER_BTN A13          //Input - Rotary encoder button
//Potentiometers (Analog Pins)
#define PIN_POT_LIGHT A6             //Input/Analog - master light brightness pot
#define PIN_POT_RGB_BRIGHTNESS 8     //Input/Analog - RGB brightness
#define PIN_POT_RGB_SPEED 9          //Input/Analog - RGB speed
#define PIN_POT_RGB_BLUE 10          //Input/Analog - RGB blue
#define PIN_POT_RGB_GREEN 11         //Input/Analog - RGB green
#define PIN_POT_RGB_RED 12           //Input/Analog - RGB red
//LED pins
#define PIN_LED_L1 13                //Output - L1 [POWER]
#define PIN_LED_R1 27                //Output - R1 [LIGHT]
#define PIN_LED_L2 23                //Output - L2 []
#define PIN_LED_R2 25                //Output - R2
#define PIN_LED_L3 28                //Output - L3
#define PIN_LED_R3 29                //Output - R3
#define PIN_LED_L4 30                //Output - L4
#define PIN_LED_R4 32                //Output - R4 
#define PIN_LED_R5 34                //Output - R5
#define PIN_LED_L5 36                //Output - L5 [RGB_ENABLED]
#define ONE_WIRE_BUS A15             //Input/Output - OneWire bus for temperature sensor
//External control
#define PIN_OUT_BLUE  9              //Output/PWM - RGB Blue
#define PIN_OUT_GREEN  10            //Output/PWM - RGB Green
#define PIN_OUT_RED  8               //Output/PWM - RGB Red 
#define PIN_OUT_GATE1 22             //Output - gate1 control
#define PIN_OUT_GATE2 24             //Output - gate2 control
#define PIN_IN_BELL 26               //Input - Bell outside
#define PIN_OUT_BELL A14             //Output - Bell control
#define PIN_OUT_LIGHT 11             //Output/PWM - Room light control
#define PIN_OUT_DOOR_UNLOCK 5        //Output - Signal to unlock room door
#define PIN_OUT_DOOR_LOCK 6          //Output - Signal to close room door
#define PIN_OUT_SERVO_OVERRIDE 7     //Output/PWM - Servo

//-----Library intitializations-----
LiquidCrystal_I2C lcd(0x27,16,2);             //LCD initialization, 16*2 chars
Servo coolServo;                              //Servo initialization
Encoder myEnc(PIN_ENCODER_1, PIN_ENCODER_2);  //Encoder initialization
OneWire oneWire(ONE_WIRE_BUS);                //OneWire Initialization
DallasTemperature sensors(&oneWire);          //Temperature sensor initialization

//-----Status values-----
int masterLightBrightness=255;    //Brightness of the master light
int masterLightOn=0;              //Master light on/off status
int lightTimerActive=0;           //30s light-off countdown active
int lcdMode=0;                    //Current LCD display mode
int backlightEnabled=1;           //LCD backlight status
int rgbOverride=0;                //RGB panel override status
int lastRotarySwitchStatus=0;     //Last rotary switch mode
int rotaryEncoderVal=0;           //Rotary encoder selected option
int lastRotaryEncoderVal=0;       //Rotary encoder last option
float currTempC=-1;               //Current temperature
byte gate2Open=0;                 //Gate2 open/closed status

//-----Timer values-----
long lastGate2Time=0;             //Timer to check if gate2 stopped moving
long lastLightTimer=0;            //30s lights-off countdown timer
long lastTemp=0;                  //Time of thelast temperature update
long lastTimeLcd=0;               //Last LCD refresh
long lastBacklight=0;             //Backlight enabled timer
long lastTimeRGB=0;               //RGB jump/fade timer

//-----Temp values-----
char wifiChar='*';                //Char received from wifi

//-----RGB values-----
int blueBrightness = 0;           //Desired blue brightness
int greenBrightness = 0;          //Desired green brightness
int redBrightness = 0;            //Desired red brightness
int totalBrightness=255;          //Max brightness
int modeRGB=10;                   //RGB mode
int colorSpeed=155;               //RGB speed
int colorStep=0;                  //RGB jump current step
  

void setup() {  
	//-----Initialize everything-----
	//Init LCD
	lcd.init();
	lcd.backlight(); 
	lcdPrint("Booting up...   ",0);  //Display message on LCD
	lcdPrint("                ",1);    
	//Init Servo
	coolServo.attach(PIN_OUT_SERVO_OVERRIDE);
	coolServo.write(0);
	coolServo.detach();
	//Init Temp Sensor
	sensors.begin();
	//Init ESP8266 Serial
	Serial3.begin(9600);
	//Init EEPROM values
	gate2Open=EEPROM.read(EEPROM_GATE2_ADDR); 
	
	//-----Setting pin modes-----
	pinMode(PIN_OUT_BLUE, OUTPUT);
	pinMode(PIN_OUT_GREEN, OUTPUT);
	pinMode(PIN_OUT_RED, OUTPUT);
	pinMode(PIN_OUT_GATE1,OUTPUT);
	pinMode(PIN_OUT_GATE2,OUTPUT);
	pinMode(PIN_OUT_BELL,OUTPUT);
	pinMode(PIN_IN_BELL,INPUT_PULLUP);
	pinMode(PIN_OUT_LIGHT,OUTPUT);
	pinMode(PIN_RESET_WIFI,INPUT_PULLUP);
	pinMode(PIN_RESET_ARDUINO,INPUT_PULLUP);
	pinMode(PIN_BUTTON_GATE2,INPUT_PULLUP);
	pinMode(PIN_BUTTON_GATE1,INPUT_PULLUP);
	pinMode(PIN_BUTTON_BELL,INPUT_PULLUP);
	pinMode(PIN_BUTTON_LIGHT,INPUT_PULLUP);
	pinMode(PIN_BUTTON_OVERRIDE,INPUT_PULLUP);
	pinMode(PIN_MODE_0,INPUT_PULLUP);
	pinMode(PIN_MODE_1,INPUT_PULLUP);
	pinMode(PIN_MODE_2,INPUT_PULLUP);
	pinMode(PIN_MODE_3,INPUT_PULLUP);
	pinMode(PIN_MODE_4,INPUT_PULLUP);
	pinMode(PIN_MODE_5,INPUT_PULLUP);
	pinMode(PIN_ENABLE_KEYBOARD,INPUT_PULLUP);
	pinMode(PIN_ENABLE_WIFI,INPUT_PULLUP);
	pinMode(PIN_ENABLE_BT,INPUT_PULLUP);
	pinMode(PIN_ENABLE_RGB,INPUT_PULLUP);
	pinMode(PIN_ENABLED_OVERRIDE,INPUT_PULLUP);
	pinMode(PIN_OUT_DOOR_LOCK,OUTPUT);
	pinMode(PIN_OUT_DOOR_UNLOCK,OUTPUT);
	pinMode(PIN_LED_L1,OUTPUT);
	pinMode(PIN_LED_R1,OUTPUT);
	pinMode(PIN_LED_L2,OUTPUT);
	pinMode(PIN_LED_R2,OUTPUT);
	pinMode(PIN_LED_L3,OUTPUT);
	pinMode(PIN_LED_R3,OUTPUT);
	pinMode(PIN_LED_L4,OUTPUT);
	pinMode(PIN_LED_R4,OUTPUT);
	pinMode(PIN_LED_R5,OUTPUT);
	pinMode(PIN_LED_L5,OUTPUT);  
	pinMode(PIN_ENCODER_1,INPUT_PULLUP);
	pinMode(PIN_ENCODER_2,INPUT_PULLUP);
	pinMode(PIN_ENCODER_BTN,INPUT_PULLUP);  
	
	//Set initial states
	digitalWrite(PIN_OUT_BELL, HIGH);        
	digitalWrite(PIN_LED_L1, HIGH);
	digitalWrite(PIN_LED_R1, LOW);
	digitalWrite(PIN_LED_L2, LOW);
	digitalWrite(PIN_LED_R2, LOW);
	digitalWrite(PIN_LED_L3, LOW);
	digitalWrite(PIN_LED_R3, LOW);
	digitalWrite(PIN_LED_L4, LOW);
	digitalWrite(PIN_LED_R4, LOW);
	digitalWrite(PIN_LED_R5, LOW);
	digitalWrite(PIN_LED_L5, LOW);  
	digitalWrite(PIN_OUT_DOOR_LOCK,LOW);
	digitalWrite(PIN_OUT_DOOR_UNLOCK,LOW);        
	
	//Finish booting up
	lcd.clear();
	lcdPrint("Setup finished!",0);  //Display success message on LCD
	
}
	
void loop() {  
	updateLight();
	updateSwitches();
	updateButtons();
	updateLCD();
	updateTemperature();
	updateEncoder();
	updateWifi();
	processRGB();
}

void gate1Open(){	//Sends signal to open gate1
	lcdPrint("Opening gate 1..",0);
	digitalWrite(PIN_OUT_GATE1,HIGH);
	delay(200);
	digitalWrite(PIN_OUT_GATE1,LOW);
	
}

char wifiGet(boolean wait){	//Get char from WiFi
	if(wait){ 
		long tempTime2=millis();
		while(!Serial3.available()&&millis()<tempTime2+WIFI_SERIAL_WAIT);    
	}
	if(!Serial3.available()) return 0;
	else return Serial3.read();
	return 0;
}

void lcdWake(){		//Displays update on lcd instantly
	backlightEnabled=1;
	lastBacklight=millis();
	lcd.backlight();
}

void toggleLight(){		//Toggles the master room light
	if(masterLightOn==0){
        masterLightOn=1;      
		lcdPrint("Lights on...    ",0);
    }else{
        masterLightOn=0;     
		lcdPrint("Lights off...   ",0);
    }	
	updateLight();
}

void updateLight(){    //Turns light on/off and update brightness
	if(masterLightOn==1){
		if(rgbOverride==1) masterLightBrightness=map(analogRead(PIN_POT_LIGHT),POT_MIN,POT_MAX,MAX_LIGHT_VAL,0);  //If override enabled, get a reading from pot 
		if(masterLightBrightness<0)masterLightBrightness=0;                                                       //Keep values in range
		if(masterLightBrightness>MAX_LIGHT_VAL)masterLightBrightness=MAX_LIGHT_VAL;
		
		analogWrite(PIN_OUT_LIGHT,masterLightBrightness);      //Turn light on and apply brightness
		digitalWrite(PIN_LED_R1,HIGH);                         //Turn status LED on
	} else{
		digitalWrite(PIN_OUT_LIGHT,LOW);                      //Turn light off
		digitalWrite(PIN_LED_R1,LOW);                         //Turn status LED off
	}
	
	//Light timer check: 30s
	if(lightTimerActive==1&&millis()>=lastLightTimer+30000){
		lightTimerActive=0;
		masterLightOn=0;
		digitalWrite(PIN_LED_R1,LOW);        
		lcdPrint("Lights off...   ",0);    
	}
}

void updateSwitches(){	//Checks for toggle switch updates
	//Keyboard enable [TODO]
	if(digitalRead(PIN_ENABLE_KEYBOARD)==LOW){
	}else{
	}
	
	//RGB Override
	if(digitalRead(PIN_ENABLED_OVERRIDE)==LOW) rgbOverride=1; 
	else rgbOverride=0;
	

}

void updateButtons(){	//Checks for pressed buttons
	//Restart arduino	
	if(isPressed(PIN_RESET_ARDUINO,LOW)){
		digitalWrite(PIN_LED_L1,LOW);		
		lcdPrint("Restarting!      ",0);
		delay(2000);		
		asm volatile ("  jmp 0");  
	}
	
	//Restart wifi [TODO]
	if(isPressed(PIN_RESET_WIFI,LOW)){
		
	}
	
	//Gate1
	if(isPressed(PIN_BUTTON_GATE1,LOW)){
		gate1Open();
    }
	
	//Gate2
	if(isPressed(PIN_BUTTON_GATE2,LOW)){
		action_gate2Open(false,true);
	}
	
	//Light
	if(isPressed(PIN_BUTTON_LIGHT,LOW)){
		toggleLight();
	}
	
	//Bell buton
	if(isPressed(PIN_BUTTON_BELL,LOW)){
		bellRing();
		lcdPrint("Bell Button...      ",0);
	}
	
	//Bell outside
	if(isPressed(PIN_IN_BELL,LOW)){
		bellRing();
		lcdPrint("Bell Outside...      ",0);
	}
	
	//RGB Override
	if(isPressed(PIN_BUTTON_OVERRIDE,LOW)){
		coolKick();
	}
	
	
}

void coolKick(){	//RGB Override
	if(digitalRead(PIN_ENABLED_OVERRIDE)==LOW){
		coolServo.attach(PIN_OUT_SERVO_OVERRIDE);
		coolServo.write(180);		  
		long temp_ms=millis();
		while(digitalRead(PIN_ENABLED_OVERRIDE)==LOW&&millis()<temp_ms+COOL_KICK_MAX_TIME);
		delay(80);  		  
		coolServo.write(0);
		delay(200);
		coolServo.detach();
	}
}

boolean isPressed(int desired_pin, boolean desired_state){	//Checks if button is pressed and debounces
	if(digitalRead(desired_pin)==desired_state){
		delay(DEBOUNCE_DELAY);
		if(digitalRead(desired_pin)==desired_state){
			while(digitalRead(desired_pin)==desired_state);
			delay(DEBOUNCE_DELAY);
			return true;
		}
	}
	return false;
}

void updateLCD(){	//Updates LCD mode
	//Turn backlight off after a certain amount of time
	if(millis()>lastBacklight+BACKLIGHT_DURATION&&backlightEnabled==1){
		backlightEnabled=0;
		lcd.noBacklight();
		lcdPrint("Panel working...",-1); 	  
	}
	
	//Update LCD regulary
	if(millis()>=lastTimeLcd+LCD_UPDATE_INTERVAL){
		lastTimeLcd=millis();	//Refresh interval
		if(lcdMode==0) lcdPrint("Time:"+String(millis()/1000)+"s          ",2);		//Mode 0 - Up time
		else if(lcdMode==1) lcdPrint("Temp:"+String(currTempC)+"deg C      ",2);	//Mode 1 - Temperature
		else if(lcdMode==2) lcdPrint("DEPRECATED 1      ",2);      					//Mode 2 - Deprecated
		else if(lcdMode==3){														//Mode 3 - Light timer
			if(masterLightOn==0){
				lcdPrint("Light is off...  ",2);
			}else{
				if(lightTimerActive==0)
					lcdPrint("Light timer 30s",2);    
				else
					lcdPrint("Light timer "+String(int((lastLightTimer+30000-millis())/1000))+"s",2);
				  
			}
		}
		else if(lcdMode==4) lcdPrint("DEPRECATED 2      ",2);       				//Mode 4 - Deprecated		  
		else if(lcdMode==5) lcdPrint("LOCK ROOM DOOR     ",2);		  				//Mode 5 - Lock room
		else if(lcdMode==6) lcdPrint("UNLOCK ROOM DOOR   ",2);		  				//Mode 6 - Unlock room
		else if(lcdMode==7) lcdPrint("WARNING:FORCE_G ",2);							//Mode 7 - Force_G
		

	}
	
	
	
}

void updateTemperature(){	//Gets update from the temp snesor
	if(millis()>(lastTemp+T_UPDATE_INTERVAL)){
		lastTemp=millis();   
		sensors.requestTemperatures(); 
		currTempC=sensors.getTempCByIndex(0); 
	   
	}
}

void unlockRoomDoor(){	//Sends signal to unclock room door
	digitalWrite(PIN_OUT_DOOR_UNLOCK,HIGH);									
	delay(1000);
	digitalWrite(PIN_OUT_DOOR_UNLOCK,LOW); 
}

void bellRing(){	//Sends signal to ringa a bell
	digitalWrite(PIN_OUT_BELL,LOW);
	delay(500);
	digitalWrite(PIN_OUT_BELL,HIGH);
}

void lockRoomDoor(){	//Sends signal to lock room door
	digitalWrite(PIN_OUT_DOOR_LOCK,HIGH);
	delay(1000);
	digitalWrite(PIN_OUT_DOOR_LOCK,LOW); 
}

void updateEncoder(){	//Checks if encoder status changed and process it
	rotaryEncoderVal=myEnc.read()/4;												//Update encoder value
	if (rotaryEncoderVal != lastRotaryEncoderVal) {									//If value has changed recently - update mode    
		lcdMode+=rotaryEncoderVal-lastRotaryEncoderVal;								//Update mode
		if(lcdMode<0) lcdMode=0;													//Keep mode in range
		if(lcdMode>(LCD_MODES_MAX-1)) lcdMode=LCD_MODES_MAX-1;
		lcdWake();
		lastRotaryEncoderVal = rotaryEncoderVal;     
		lastTimeLcd=0;
    }
  
	if(digitalRead(PIN_ENCODER_BTN)==LOW){											//If encoder button pressed
		delay(DEBOUNCE_DELAY);														//Debounce
		while(digitalRead(PIN_ENCODER_BTN)==LOW);
		lcdWake();
		if(lcdMode==2) lastTemp=0;													//Request temperature update
		else if(lcdMode==3){
			if(masterLightOn==1){
				if(lightTimerActive!=1)lightTimerActive=1;							//Start light timer
				else if(millis()>=lastLightTimer+1500) lightTimerActive=0;			//Stop light timer
			}
			lastLightTimer=millis();												//Update timer start time 
	    }else if(lcdMode==4){
			lastTimeLcd=0;
		}else if(lcdMode==5){														//Unlock room door
			unlockRoomDoor();
		}else if(lcdMode==6){														//Lock room door
			lockRoomDoor();
		}else if(lcdMode==7){														//Force gate2
			digitalWrite(PIN_OUT_GATE2,HIGH);
			delay(200);
			digitalWrite(PIN_OUT_GATE2,LOW);   
	   }
	}
}

void setRGBMode(int mode, boolean isWifi){	//Sets current RGB mode
	if((!isWifi&&rgbOverride)||(isWifi&&!rgbOverride)){
		switch(mode){
			case 0:
				modeRGB=0;
				break;
			
			case 1:
				modeRGB=1;    
				lastTimeRGB=millis();
				colorStep=0;
				redBrightness=0;
				blueBrightness=255;
				greenBrightness=0;
				break;
			case 2:
				modeRGB=2;
				lastTimeRGB=millis();
				redBrightness=0;
				blueBrightness=255;
				greenBrightness=0;
				colorStep=0;
				break;
			case 10:
				modeRGB=10;
				redBrightness=0;
				blueBrightness=0;
				greenBrightness=0;    
				break;
			case 11:
				modeRGB=11;
				lastTimeRGB=millis();
				colorStep=0;
				redBrightness=0;
				blueBrightness=0;
				greenBrightness=0;    
				break;
			case 12:
				modeRGB=12;
				lastTimeRGB=millis();
				colorStep=0;
				redBrightness=0;
				blueBrightness=0;
				greenBrightness=0;
				break;
			
		}
	}
}

void updateWifi(){	//Gets a command from WiFi
	wifiChar=wifiGet(false);
	if(wifiChar!=0){
    //Execute WiFi commands
		switch(wifiChar) {

			case 'b':	//Test connection
				digitalWrite(13,LOW);
				delay(1000);
				digitalWrite(13,HIGH);
				break;

			case 'c':	//RGB mode 1
				setRGBMode(1,true);
				break;

			case 'd':	//RGB mode 2
				setRGBMode(2,true);
				break;

			case 'g':	//Gate1 open
				gate1Open();
				break;

			case '+':	//Light on
				masterLightOn=1;
				updateLight();    
				break;

			case '-':	//Light off
				masterLightOn=0;
				updateLight(); 
				break;

			case 'F':	//Open gate2
				action_gate2Open(true,false);
				break;
			  
			case 'f':	//Close gate2
				action_gate2Open(false,false);
				break;
				
			case 'p':	//RGB mode 10
				setRGBMode(10,true);
				break;
			  
			case 'o':	//RGB mode 11
				setRGBMode(11,true);	
				break;
			  
			case 'u':	//RGB mode 12
				setRGBMode(12,true);
				break;

			case 'k':	//Update RGB speed
				colorSpeed=wifiGet(true)*2;
				break;

			case 'l':	//Update RGB brightness
				totalBrightness=wifiGet(true)*2;
				break;
			  
			case 'L':	//Update master brightness
				masterLightBrightness=wifiGet(true)*2;
				break;

			case 'n':	//Custom RGB color
				modeRGB=0;
				redBrightness=wifiGet(true)*2;
				greenBrightness=wifiGet(true)*2;
				blueBrightness=wifiGet(true)*2;
				break;	   

			case 't':	//Disable RGB
				setRGBMode(10,true);
				break;

			case 'a':	//Disable RGB override
				coolKick();
                                setRGBMode(10,true);	
                                lcdWake();			
				break;

			case 'Y':
				lockRoomDoor();
				break;
			
			case 'X':
				unlockRoomDoor();
				break;
			
			case 'Z':
				bellRing(); 
				break;
			
			case 'I'://Some info request
				parseInfoRequest();
				break;
			
		}
	}	
}

void parseInfoRequest(){	//Process WiFi status request
	while(!Serial3.available());
	char infoData=Serial3.read();  
	switch (infoData){
		case 'a':  //Ligths on status
			if(masterLightOn==1)
				Serial3.write(1);//Lights on
			else
				Serial3.write(0);//Lights off
			break;
   
		case 'b':  //Car gate Locked status
			if(gate2Open==0)
				Serial3.write(1);//Locked
			else
				Serial3.write(0);//Unlocked
			break;
      
		case 'd':  //Current RGB Mode
			Serial3.write(modeRGB);
			break;
   
		case 'e'://RGB Speed
			Serial3.write(colorSpeed/2);
		break;
   
		case 'f'://RGB Brightness
			Serial3.write(totalBrightness/2);
			break;
   
		case 'g'://RGB Colors
			Serial3.write(redBrightness/2);
			Serial3.write(greenBrightness/2);
			Serial3.write(blueBrightness/2);
			break;
   
		case 'h'://Manual RGB Active
			Serial3.write(rgbOverride);
			break;
   
		case 'i'://Master Light Brightness
			Serial3.write(masterLightBrightness/2);
			break;   
   
		default:
			Serial3.write(0);
			break; 
   
	} 
  
}

void action_gate2Open(boolean state, boolean toggle){	//Opens or closes the gate
	if(millis()>lastGate2Time+GATE2_DELAY){
		boolean change=false;
		if((toggle==true||state==true)&&gate2Open==0){
			gate2Open=1;
			EEPROM.write(EEPROM_GATE2_ADDR,gate2Open);
			change=true;
			lcdPrint("Opening gate 2   ",0);   
		}else if((toggle==true||state==false)&&gate2Open==1){ 
			gate2Open=0;
			EEPROM.write(EEPROM_GATE2_ADDR,gate2Open);
			change=true;
			lcdPrint("Closing gate 2   ",0);   
		}else{
			lcdPrint("Gate 2 unknown err!",0);
		} 
		if(change==true){
			lastGate2Time=millis();
			digitalWrite(PIN_OUT_GATE2,HIGH);
			delay(200);
			digitalWrite(PIN_OUT_GATE2,LOW);   
		} 
	}
}

void lcdPrint(String text, int line){	//Prints a string onto LCD
	if(line!=-1&&line!=2) lcdWake();	//Turn backlight on if needed
	if(line==-1) line=0; 
	if(line==2) line=1;
	lcd.setCursor(0,line);
	lcd.print(text+"                  "); 

}

void checkValuesRange(){	//Keeps values in range	
	if(redBrightness>255) redBrightness=255;
    if(greenBrightness>255) greenBrightness=255;
    if(blueBrightness>255) blueBrightness=255;
    if(totalBrightness>255) totalBrightness=255;
    if(colorSpeed>255) colorSpeed=255;  
    if(redBrightness<0) redBrightness=0;
    if(greenBrightness<0) greenBrightness=0;
    if(blueBrightness<0) blueBrightness=0;
    if(totalBrightness<0) totalBrightness=0;
    if(colorSpeed<0) colorSpeed=0;	
}

void processRGB(){
	if(digitalRead(PIN_ENABLE_RGB)==HIGH){	//Checks if RGB is enabled on the panel
		analogWrite(PIN_OUT_RED,0);
		analogWrite(PIN_OUT_GREEN,0);
		analogWrite(PIN_OUT_BLUE,0);
		digitalWrite(PIN_LED_L5,LOW);	//Status LED - OFF
	}else{
		digitalWrite(PIN_LED_L5,HIGH);	//Status LED - ON
		//Update rotary switch mode
		if(digitalRead(PIN_MODE_0)==LOW&&lastRotarySwitchStatus!=0){
			setRGBMode(10,false);       
			lastRotarySwitchStatus=0;
		}else if(digitalRead(PIN_MODE_1)==LOW&&lastRotarySwitchStatus!=1&&digitalRead(PIN_MODE_0)==HIGH){
			setRGBMode(0,false);
			lastRotarySwitchStatus=1;
		}else if(digitalRead(PIN_MODE_2)==LOW&&lastRotarySwitchStatus!=2){
			setRGBMode(1,false);       
			lastRotarySwitchStatus=2;
		}else if(digitalRead(PIN_MODE_3)==LOW&&lastRotarySwitchStatus!=3){
			setRGBMode(2,false);       
			lastRotarySwitchStatus=3;
		}else if(digitalRead(PIN_MODE_4)==LOW&&lastRotarySwitchStatus!=4){
			setRGBMode(11,false);        
			lastRotarySwitchStatus=4;
		}else if(digitalRead(PIN_MODE_5)==LOW&&lastRotarySwitchStatus!=5){
			setRGBMode(12,false);        
			lastRotarySwitchStatus=5;
		}
		
	
		
		if(rgbOverride){
			totalBrightness=map(analogRead(PIN_POT_RGB_BRIGHTNESS),POT_MIN,POT_MAX,255,0);  //INVERTED
			colorSpeed=map(analogRead(PIN_POT_RGB_SPEED),POT_MIN,POT_MAX,0,255);  //INVERTED
		
		}
		
		
		
		switch(modeRGB){
			case 0:	//Manual color
				if(rgbOverride){        
					redBrightness=map(analogRead(PIN_POT_RGB_RED),POT_MIN,POT_MAX,255,0);  //INVERTED
					greenBrightness=map(analogRead(PIN_POT_RGB_GREEN),POT_MIN,POT_MAX,255,0);  //INVERTED
					blueBrightness=map(analogRead(PIN_POT_RGB_BLUE),POT_MIN,POT_MAX,255,0);  //INVERTED
				} 
				break;
			case 1:	//Fade 7
				if(millis()>(lastTimeRGB+colorSpeed)){
					lastTimeRGB=millis();        
					switch(colorStep){
						case 2:
							redBrightness--;
							blueBrightness++;
							if(redBrightness<=0)
								colorStep=3;
							break;

						case 1:
							greenBrightness--;
							redBrightness++;
							if(greenBrightness<=0)
								colorStep=2;
							break;

						case 0:
							greenBrightness++;
							blueBrightness--;
							if(blueBrightness<=0)
								colorStep=1;
							break;
	
						case 3:
							colorStep=0;
							break;

					}    
					
				}
				break;
			case 2:	//Jump 7
				if(millis()>(lastTimeRGB+colorSpeed*7)+5){
					lastTimeRGB=millis();
					switch(colorStep){
						case 5:
							colorStep=0;
							greenBrightness=0;
							break;
						case 4:
							colorStep=5;
							blueBrightness=totalBrightness;
							break;
						case 3:
							colorStep=4;
							redBrightness=0;
							break;
						case 2:
							colorStep=3;
							greenBrightness=totalBrightness;
							break;  
						case 1:
							colorStep=2;
							blueBrightness=0;
							break;
						case 0:
							colorStep=1;
							redBrightness=totalBrightness;
							break;
					}  
				}			
				break;
			case 10:
				redBrightness=0;
				greenBrightness=0;
				blueBrightness=0;
				break;
			case 11:
				if(millis()>(lastTimeRGB+colorSpeed)){
					lastTimeRGB=millis();        
					switch (colorStep){        
						case 1:
							redBrightness--;
							greenBrightness--;
							blueBrightness--;
							if(redBrightness<=0)
								colorStep=0;
							break;
						case 0:
							redBrightness++;
							greenBrightness++;
							blueBrightness++;
							if(redBrightness>=255)
								colorStep=1;
							break;
          
					}         
					
         
				}
				break;				
		}
		checkValuesRange();	//Compensate for imperfect pots
		
		analogWrite(PIN_OUT_RED,map(redBrightness,0,255,0,totalBrightness));
		analogWrite(PIN_OUT_GREEN,map(greenBrightness,0,255,0,totalBrightness));
		analogWrite(PIN_OUT_BLUE,map(blueBrightness,0,255,0,totalBrightness));
	}	
}
