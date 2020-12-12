#include <SPI.h>
#include <RF24.h>
#include <BTLE.h> //https://github.com/floe/BTLE

RF24 radio(9,10);

BTLE btle(&radio);
bool state=true;
bool rDebug=false;
#define MONOSTABLE
#define ID  14  //0x0E
#define ACTION 29  //0x1D

#define OUT_PIN 7 //The Arduino PIN
#define OUT_PIN_N 8 //The Arduino PIN

#define DEBUG_PIN 2
#define LED A0 //State LED


#define SWITCH_TIME 2000  //the out pin will switch the state after the switch time
#define DEBOUNCE_TIME 5000  //the out pin will switch the state after the switch time

#define BAUDRATE 57600

//Example command from Android side. You can use NRFConnect or the AndroidBLEAdvertiser
//FFFF
//1D0EABAA


int id, action, guid, guid_last, chksum;

long rTime=0;
bool rState=false;

const char* rDeviceName = "Attuatore1.0"; 

void setup() {
  pinMode(DEBUG_PIN, INPUT_PULLUP);
  pinMode(OUT_PIN, OUTPUT);
  pinMode(OUT_PIN_N, OUTPUT);
  pinMode(LED, OUTPUT);

  #ifdef MONOSTABLE
    digitalWrite(OUT_PIN, LOW);
    digitalWrite(OUT_PIN_N, HIGH);
  #endif

  fLog("BTLE advertisement receiver");
       
  btle.begin(rDeviceName); //Start BTLE
}

void loop() {
  if (btle.listen()) { //Start listening advertising if an advertise arrive
    int rLastChar = (btle.buffer.pl_size) - 7; //LAst 7 chars are unuseful
    
    if(!digitalRead(DEBUG_PIN)|| rDebug){  //A0 is used as debug pin
      String str = ""; String str2 = "";
      for (uint8_t i = 2; i <= rLastChar; i++)  {
        str += (char (btle.buffer.payload[i]));   
        str2+=String(i) + ":" + (int (btle.buffer.payload[i]))+" ";
      }
      Serial.begin(BAUDRATE);
      Serial.print("Got payload: ");
      Serial.println(str);
      Serial.println(str2);

      Serial.print("Last Char: ");
      Serial.println(rLastChar);
      Serial.end();
    }
    if(int(btle.buffer.payload[rLastChar])==0xAA &&  
      int(btle.buffer.payload[rLastChar-1])==0xAB){ //0xAA and 0xAB are just a dummy postfix
        fLog("*** ACTION ***");
        id = int(btle.buffer.payload[rLastChar-2]);
        action = int(btle.buffer.payload[rLastChar-3]);

        if(id==ID && action == ACTION){  
          #ifdef MONOSTABLE          
            digitalWrite(OUT_PIN, HIGH);
            digitalWrite(OUT_PIN_N, LOW);
            delay(SWITCH_TIME);
            digitalWrite(OUT_PIN, LOW);  
            digitalWrite(OUT_PIN_N, HIGH);
            delay(DEBOUNCE_TIME);      
          #endif
        }else
          fLog("Invalid ID or Action");      
    }else
      fLog("Invalid postfix");  
  }else
  {
    //Blink slow if is working
    rTime=blink(rTime, 2000);
  }

  // btle.hopChannel();

  if(!radio.isChipConnected()){
    fLog("Chip not connected");
    //Blink fast if is not working
    rTime=blink(rTime, 200);

    //Restart BLE
    //btle.begin(rDeviceName); //Start BTLE
  }
}

long blink(long rTime, long rInterval){
  long rNow=millis();
  if(rNow-rTime>rInterval){
    rState=!rState;
    digitalWrite(LED, rState); 
    rTime=rNow; 
  }  
  return rTime;
}

void blink2(int pTimes){
  for(int i=0; i<pTimes; i++){
    digitalWrite(LED, HIGH);
    delay(200);
    digitalWrite(LED, LOW);
    delay(200);
  }
}

void fLog(String pMessage){
  if(!digitalRead(DEBUG_PIN)|| rDebug){
    Serial.begin(BAUDRATE);
    Serial.println(pMessage);
    Serial.end();
  }
}

