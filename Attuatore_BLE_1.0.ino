#include <SPI.h>
#include <RF24.h>
#include <BTLE.h> //https://github.com/floe/BTLE

RF24 radio(9,10);

BTLE btle(&radio);
bool state=true;
bool rDebug=true;
#define MONOSTABLE
#define ID  14  //0x0E
#define ACTION 29  //0x1D

#define OUT_PIN 7 //The Arduino PIN

#define SWITCH_TIME 2000  //the out pin will switch the state after the switch time

#define BAUDRATE 57600

//Example command from Android side. You can use NRFConnect or the AndroidBLEAdvertiser
//FFFF
//1D0EABAA


int id, action, guid, guid_last, chksum;

void setup() {
  pinMode(A0, INPUT_PULLUP);
  pinMode(OUT_PIN, OUTPUT);
  #ifdef MONOSTABLE
    digitalWrite(OUT_PIN, HIGH);
  #endif

  fLog("BTLE advertisement receiver");
    
  btle.begin("Attuatore1.0"); //Start BTLE
}

void loop() {
  if (btle.listen()) { //Start listening advertising
    int rLastChar = (btle.buffer.pl_size) - 7; //LAst 7 chars are unuseful
    
    if(!digitalRead(A0)|| rDebug){  //A0 is used as debug pin
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
            digitalWrite(OUT_PIN, LOW);
            delay(SWITCH_TIME);
            digitalWrite(OUT_PIN, HIGH);        
          #endif
        }else
          fLog("Invalid ID or Action");      
    }else
      fLog("Invalid postfix");    
  }
}

void fLog(String pMessage){
  if(!digitalRead(A0)|| rDebug){
    Serial.begin(BAUDRATE);
    Serial.println(pMessage);
    Serial.end();
  }
}
