#include "MIDIUSB.h"
#include "PitchToNote.h"
#define NUM_BUTTONS  5
#define CHORD_DELAY 3000


#define TRIG_PIN 5
#define VAL1_PIN A2
#define VAL2_PIN A1
#define VAL3_PIN A3
#define VAL4_PIN A0
#define VAL5_PIN A4

#define TRIG_BOUNCE 150
#define TRIG_TRESHOLD 600
#define V5_TRESHOLD 900
#define V4_TRESHOLD 800  
#define V3_TRESHOLD 740  
#define V2_TRESHOLD 750  
#define V1_TRESHOLD 550  
#define DEBUG false
int lastChord=-1;
int lastTrig=0;
long lastChordTime=0;
uint8_t selectedChord = 0x00;


int intensity=70;

int ch=0;

void setup() {
  Serial.begin(115200);
}


void loop() {
  if(DEBUG){
  Serial.print("TRIG: ");
    Serial.print(analogRead(TRIG_PIN));
    Serial.print(", V1:");
    Serial.print(analogRead(VAL1_PIN));
    Serial.print(", V2:");
    Serial.print(analogRead(VAL2_PIN));
    Serial.print(", V3:");
    Serial.print(analogRead(VAL3_PIN));
    Serial.print(", V4:");
    Serial.print(analogRead(VAL4_PIN));
    Serial.print(", V5:");
    Serial.println(analogRead(VAL5_PIN));
    delay(50);
  }
  if(analogRead(TRIG_PIN)>TRIG_TRESHOLD+TRIG_BOUNCE)
    lastTrig=0;
  if(analogRead(TRIG_PIN)<TRIG_TRESHOLD&&lastTrig==0){
    lastTrig=1;
        readButtons();
        playChord(selectedChord);
      
  } 
  
  
  if(millis()>lastChordTime+CHORD_DELAY&&lastChord!=-1){
    killChord(lastChord);
    lastChord=-1; 
  }
 
 
}

// First parameter is the event type (0x0B = control change).
// Second parameter is the event type, combined with the channel.
// Third parameter is the control number number (0-119).
// Fourth parameter is the control value (0-127).

void controlChange(byte channel, byte control, byte value) {
  midiEventPacket_t event = {0x0B, 0xB0 | channel, control, value};
  MidiUSB.sendMIDI(event);
}

void readButtons()
{
  if(analogRead(VAL1_PIN)>V1_TRESHOLD)
    bitWrite(selectedChord,4,0);
  else
    bitWrite(selectedChord,4,1);

  if(analogRead(VAL2_PIN)>V2_TRESHOLD)
    bitWrite(selectedChord,3,0);
  else
    bitWrite(selectedChord,3,1);

  if(analogRead(VAL3_PIN)>V3_TRESHOLD)
    bitWrite(selectedChord,2,0);
  else
    bitWrite(selectedChord,2,1);

  if(analogRead(VAL4_PIN)>V4_TRESHOLD)
    bitWrite(selectedChord,1,0);
  else
    bitWrite(selectedChord,1,1);

  if(analogRead(VAL5_PIN)>V5_TRESHOLD)
    bitWrite(selectedChord,0,0);
  else
    bitWrite(selectedChord,0,1);
  
}





// First parameter is the event type (0x09 = note on, 0x08 = note off).
// Second parameter is note-on/note-off, combined with the channel.
// Channel can be anything between 0-15. Typically reported to the user as 1-16.
// Third parameter is the note number (48 = middle C).
// Fourth parameter is the velocity (64 = normal, 127 = fastest).

void noteOn(byte channel, byte pitch, byte velocity) {
  midiEventPacket_t noteOn = {0x09, 0x90 | channel, pitch, velocity};
  MidiUSB.sendMIDI(noteOn);
}

void noteOff(byte channel, byte pitch, byte velocity) {
  midiEventPacket_t noteOff = {0x08, 0x80 | channel, pitch, velocity};
  MidiUSB.sendMIDI(noteOff);
}



void playChord_C(int p){
  noteOn(ch, pitchC3+p, intensity);
  noteOn(ch, pitchE3+p, intensity);
  noteOn(ch, pitchG3+p, intensity);
  noteOn(ch, pitchC4+p, intensity);
  noteOn(ch, pitchE4+p, intensity);
  MidiUSB.flush();
}


void playChord_D(int p){
  noteOn(ch, pitchD3+p, intensity);
  noteOn(ch, pitchA3+p, intensity);
  noteOn(ch, pitchD4+p, intensity);
  noteOn(ch, pitchG4b+p, intensity);
  MidiUSB.flush();
}

void playChord_E(int p){
  noteOn(ch, pitchE2+p, intensity);
  noteOn(ch, pitchB2+p, intensity);
  noteOn(ch, pitchE3+p, intensity);
  noteOn(ch, pitchA3b+p, intensity);
  MidiUSB.flush();
}

void playChord_F(int p){
  noteOn(ch, pitchF2+p, intensity);
  noteOn(ch, pitchC3+p, intensity);
  noteOn(ch, pitchF3+p, intensity);
  noteOn(ch, pitchA3+p, intensity);
  noteOn(ch, pitchC4+p, intensity);
  MidiUSB.flush();
}

void playChord_G(int p){
  noteOn(ch, pitchG2+p, intensity);
  noteOn(ch, pitchD3+p, intensity);
  noteOn(ch, pitchG3+p, intensity);
  noteOn(ch, pitchB3+p, intensity);
  noteOn(ch, pitchD4+p, intensity);
  noteOn(ch, pitchG4+p, intensity);
  MidiUSB.flush();
}


void playChord_A(int p){
  noteOn(ch, pitchA2+p, intensity);
  noteOn(ch, pitchE3+p, intensity);
  noteOn(ch, pitchA3+p, intensity);
  noteOn(ch, pitchD4b+p, intensity);
  noteOn(ch, pitchE4+p, intensity);
  MidiUSB.flush();
}

void playChord_B(int p){
  noteOn(ch, pitchB2+p, intensity);
  noteOn(ch, pitchG3b+p, intensity);
  noteOn(ch, pitchB3+p, intensity);
  noteOn(ch, pitchD4+p, intensity);
  noteOn(ch, pitchG4b+p, intensity);
  MidiUSB.flush();
}

void playChord_NONE(){
  noteOn(ch, pitchE2, intensity);
  noteOn(ch, pitchA2, intensity);
  noteOn(ch, pitchD3, intensity);
  noteOn(ch, pitchG3, intensity);
  noteOn(ch, pitchB3, intensity);
  noteOn(ch, pitchE4, intensity);
  MidiUSB.flush();
}






void playChord_Cm(int p){
  noteOn(ch, pitchC3+p, intensity);
  noteOn(ch, pitchG3+p, intensity);
  noteOn(ch, pitchC4+p, intensity);
  noteOn(ch, pitchE4b+p, intensity);
  noteOn(ch, pitchG4+p, intensity);
  MidiUSB.flush();
}

void playChord_Dm(int p){
  noteOn(ch, pitchD3+p, intensity);
  noteOn(ch, pitchA3+p, intensity);
  noteOn(ch, pitchD4+p, intensity);
  noteOn(ch, pitchF4+p, intensity);
  MidiUSB.flush();
}

void playChord_Em(int p){
  noteOn(ch, pitchE2+p, intensity);
  noteOn(ch, pitchB2+p, intensity);
  noteOn(ch, pitchE3+p, intensity);
  noteOn(ch, pitchG3+p, intensity);
  noteOn(ch, pitchB3+p, intensity);
  MidiUSB.flush();
}

void playChord_Fm(int p){
  noteOn(ch, pitchF2+p, intensity);
  noteOn(ch, pitchC3+p, intensity);
  noteOn(ch, pitchF3+p, intensity);
  noteOn(ch, pitchA3b+p, intensity);
  noteOn(ch, pitchC4+p, intensity);
  noteOn(ch, pitchF4+p, intensity);
  MidiUSB.flush();
}


void playChord_Gm(int p){
  noteOn(ch, pitchG2+p, intensity);
  noteOn(ch, pitchD3+p, intensity);
  noteOn(ch, pitchG3+p, intensity);
  noteOn(ch, pitchB3b+p, intensity);
  noteOn(ch, pitchD4+p, intensity);
  noteOn(ch, pitchG4+p, intensity);
  MidiUSB.flush();
}

void playChord_Am(int p){
  noteOn(ch, pitchA2+p, intensity);
  noteOn(ch, pitchE2+p, intensity);
  noteOn(ch, pitchA3+p, intensity);
  noteOn(ch, pitchC4+p, intensity);
  noteOn(ch, pitchE4+p, intensity);
  MidiUSB.flush();
}

void playChord_Bm(int p){
  noteOn(ch, pitchB2+p, intensity);
  noteOn(ch, pitchG3b+p, intensity);
  noteOn(ch, pitchB3+p, intensity);
  noteOn(ch, pitchD4+p, intensity);
  noteOn(ch, pitchG3b+p, intensity);
  MidiUSB.flush();
}


void killChord_C(int p){
  noteOff(ch, pitchC3+p, intensity);
  noteOff(ch, pitchE3+p, intensity);
  noteOff(ch, pitchG3+p, intensity);
  noteOff(ch, pitchC4+p, intensity);
  noteOff(ch, pitchE4+p, intensity);
  MidiUSB.flush();
}


void killChord_D(int p){
  noteOff(ch, pitchD3+p, intensity);
  noteOff(ch, pitchA3+p, intensity);
  noteOff(ch, pitchD4+p, intensity);
  noteOff(ch, pitchG4b+p, intensity);
  MidiUSB.flush();
}

void killChord_E(int p){
  noteOff(ch, pitchE2+p, intensity);
  noteOff(ch, pitchB2+p, intensity);
  noteOff(ch, pitchE3+p, intensity);
  noteOff(ch, pitchA3b+p, intensity);
  noteOff(ch, pitchB3+p, intensity);
  noteOff(ch, pitchE4+p, intensity);
  MidiUSB.flush();
}

void killChord_F(int p){
  noteOff(ch, pitchF2+p, intensity);
  noteOff(ch, pitchC3+p, intensity);
  noteOff(ch, pitchF3+p, intensity);
  noteOff(ch, pitchA3+p, intensity);
  noteOff(ch, pitchC4+p, intensity);
  MidiUSB.flush();
}

void killChord_G(int p){
  noteOff(ch, pitchG2+p, intensity);
  noteOff(ch, pitchD3+p, intensity);
  noteOff(ch, pitchG3+p, intensity);
  noteOff(ch, pitchB3+p, intensity);
  noteOff(ch, pitchD4+p, intensity);
  noteOff(ch, pitchG4+p, intensity);
  MidiUSB.flush();
}


void killChord_A(int p){
  noteOff(ch, pitchA2+p, intensity);
  noteOff(ch, pitchE3+p, intensity);
  noteOff(ch, pitchA3+p, intensity);
  noteOff(ch, pitchD4b+p, intensity);
  noteOff(ch, pitchE4+p, intensity);
  MidiUSB.flush();
}

void killChord_B(int p){
  noteOff(ch, pitchB2+p, intensity);
  noteOff(ch, pitchG3b+p, intensity);
  noteOff(ch, pitchB3+p, intensity);
  noteOff(ch, pitchD4+p, intensity);
  noteOff(ch, pitchG4b+p, intensity);
  MidiUSB.flush();
}

void killChord_NONE(){
  noteOff(ch, pitchE2, intensity);
  noteOff(ch, pitchA2, intensity);
  noteOff(ch, pitchD3, intensity);
  noteOff(ch, pitchG3, intensity);
  noteOff(ch, pitchB3, intensity);
  noteOff(ch, pitchE4, intensity);
  MidiUSB.flush();
}






void killChord_Cm(int p){
  noteOff(ch, pitchC3+p, intensity);
  noteOff(ch, pitchG3+p, intensity);
  noteOff(ch, pitchC4+p, intensity);
  noteOff(ch, pitchE4b+p, intensity);
  noteOff(ch, pitchG4+p, intensity);
  MidiUSB.flush();
}

void killChord_Dm(int p){
  noteOff(ch, pitchD3+p, intensity);
  noteOff(ch, pitchA3+p, intensity);
  noteOff(ch, pitchD4+p, intensity);
  noteOff(ch, pitchF4+p, intensity);
  MidiUSB.flush();
}

void killChord_Em(int p){
  noteOff(ch, pitchE2+p, intensity);
  noteOff(ch, pitchB2+p, intensity);
  noteOff(ch, pitchE3+p, intensity);
  noteOff(ch, pitchG3+p, intensity);
  noteOff(ch, pitchB3+p, intensity);
  MidiUSB.flush();
}

void killChord_Fm(int p){
  noteOff(ch, pitchF2+p, intensity);
  noteOff(ch, pitchC3+p, intensity);
  noteOff(ch, pitchF3+p, intensity);
  noteOff(ch, pitchA3b+p, intensity);
  noteOff(ch, pitchC4+p, intensity);
  noteOff(ch, pitchF4+p, intensity);
  MidiUSB.flush();
}


void killChord_Gm(int p){
  noteOff(ch, pitchG2+p, intensity);
  noteOff(ch, pitchD3+p, intensity);
  noteOff(ch, pitchG3+p, intensity);
  noteOff(ch, pitchB3b+p, intensity);
  noteOff(ch, pitchD4+p, intensity);
  noteOff(ch, pitchG4+p, intensity);
  MidiUSB.flush();
}

void killChord_Am(int p){
  noteOff(ch, pitchA2+p, intensity);
  noteOff(ch, pitchE2+p, intensity);
  noteOff(ch, pitchA3+p, intensity);
  noteOff(ch, pitchC4+p, intensity);
  noteOff(ch, pitchE4+p, intensity);
  MidiUSB.flush();
}

void killChord_Bm(int p){
  noteOff(ch, pitchB2+p, intensity);
  noteOff(ch, pitchG3b+p, intensity);
  noteOff(ch, pitchB3+p, intensity);
  noteOff(ch, pitchD4+p, intensity);
  noteOff(ch, pitchG3b+p, intensity);
  MidiUSB.flush();
}







void playChord(int no){
  
  if(lastChord!=no&&lastChord!=-1) killChord(lastChord);
  lastChord=no;
  lastChordTime=millis();
  
  Serial.print("Playing chord no: ");
  int p = bitRead(no, 4); 
  no=no%16;
  Serial.println(selectedChord);
  
  switch(no){
    
    case 0:
    if(p) toggleVolume(0); 
    else
    playChord_NONE();
    break;
    case 1:
      playChord_C(p);
      break;
      case 2:
      playChord_D(p);
      break;
      case 3:
      playChord_E(p);
      break;
      case 4:
      playChord_F(p);
      break;
      case 5:
      playChord_G(p);
      break;
      case 6:
      playChord_A(p);
      break;
      case 7:
      playChord_B(p);
      break;
      case 8:          
      if(p) toggleVolume(1);
      break;
      
      case 9:
        playChord_Cm(p);
      break;
      case 10:
        playChord_Dm(p);
      break;
      case 11:
        playChord_Em(p);
      break;
      case 12:
        playChord_Fm(p);
      break;
      case 13:
        playChord_Gm(p);
      break;
      case 14:
        playChord_Am(p);
      break;
      case 15:
        playChord_Bm(p);
      break;
      
  }
  
}

void killChord(int no){
  Serial.print("Killing chord no: ");
  int p = bitRead(no, 4); 
  no=no%16;
  Serial.println(no);
switch(no){
    case 0:
    killChord_NONE();
    
    break;
    case 1:
      killChord_C(p);
      break;
      case 2:
      killChord_D(p);
      break;
      case 3:
      killChord_E(p);
      break;
      case 4:
      killChord_F(p);
      break;
      case 5:
      killChord_G(p);
      break;
      case 6:
      killChord_A(p);
      break;
      case 7:
      killChord_B(p);
      break;
      case 8:
      killChord_NONE();
      break;
      
      case 9:
        killChord_Cm(p);
      break;
      case 10:
        killChord_Dm(p);
      break;
      case 11:
        killChord_Em(p);
      break;
      case 12:
        killChord_Fm(p);
      break;
      case 13:
        killChord_Gm(p);
      break;
      case 14:
        killChord_Am(p);
      break;
      case 15:
        killChord_Bm(p);
      break;
      
  }
  
}



void toggleVolume(int increase){
  if(increase==0){
    intensity-=10;
    if(intensity<0)
    intensity=0;
    Serial.print("Intensity: ");
    Serial.println(intensity);

    
  }else{
    intensity+=10;
    if(intensity>127)
    intensity=127;
    Serial.print("Intensity: ");
    Serial.println(intensity);
  }
}

