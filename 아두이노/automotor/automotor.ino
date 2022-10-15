
/*
  Rui Santos
  Complete project details at our blog.
    - ESP32: https://RandomNerdTutorials.com/esp32-firebase-realtime-database/
    - ESP8266: https://RandomNerdTutorials.com/esp8266-nodemcu-firebase-realtime-database/
  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files.
  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  Based in the RTDB Basic Example by Firebase-ESP-Client library by mobizt
  https://github.com/mobizt/Firebase-ESP-Client/blob/main/examples/RTDB/Basic/Basic.ino
*/
#include "DHT.h"
#include <Arduino.h>
#if defined(ESP32)
#elif defined(ESP8266)
  #include <ESP8266WiFi.h>
#endif
#include <Firebase_ESP_Client.h>

//Provide the token generation process info.
#include "addons/TokenHelper.h"
//Provide the RTDB payload printing info and other helper functions.
#include "addons/RTDBHelper.h"

// Insert your network credentials
#define WIFI_SSID "son"
#define WIFI_PASSWORD "123456789"

// Insert Firebase project API Key
#define API_KEY "AIzaSyDqFzxgzics8NUugrOKBHB1lemosv32QUM"

// Insert RTDB URLefine the RTDB URL */
#define DATABASE_URL "https://smartfarmactivity-default-rtdb.firebaseio.com/" 

//Define Firebase Data object
FirebaseData fbdo;

FirebaseAuth auth;
FirebaseConfig config;
#define DHTPIN 19        // GPIO23



#define DHTTYPE DHT11   // DHT 22  (AM2302), AM2321


unsigned long sendDataPrevMillis = 0;
bool signupOK = false;
int intValue;
float floatValue;
DHT dht(DHTPIN, DHTTYPE);

////////motor code add/////////////


int motor1Pin1 = 25;      // 제어신호 1핀
int motor1Pin2 = 26;      // 제어신호 2핀
int enable1Pin = 27;    // PWM제어를 위한 핀
 
const int freq = 30000;
const int pwmChannel = 0;
const int resolution = 8;
int dutyCycle = 200;
/////////////////////////////////////////////
void setup(){
  Serial.begin(115200);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED){
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();
  //
  Serial.println("DHTxx test!");
  dht.begin();

  /* Assign the api key (required) */
  config.api_key = API_KEY;

  /* Assign the RTDB URL (required) */
  config.database_url = DATABASE_URL;

  /* Sign up */
  if (Firebase.signUp(&config, &auth, "", "")){
    Serial.println("ok");
    signupOK = true;
  }
  else{
    Serial.printf("%s\n", config.signer.signupError.message.c_str());
  }

  /* Assign the callback function for the long running token generation task */
  config.token_status_callback = tokenStatusCallback; //see addons/TokenHelper.h
  
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  ///motor code add/////////////////
  pinMode(motor1Pin1, OUTPUT);
  pinMode(motor1Pin2, OUTPUT);
  pinMode(enable1Pin, OUTPUT);
  
  // configure LED PWM functionalitites
  ledcSetup(pwmChannel, freq, resolution);
  
  // attach the channel to the GPIO to be controlled
  ledcAttachPin(enable1Pin, pwmChannel);


  // testing
  Serial.print("Testing DC Motor...");
//////////////////////////////

}

void loop(){
   delay(2000);
  float h = dht.readHumidity();
  float t = dht.readTemperature();
  float f = dht.readTemperature(true);
  if (isnan(h) || isnan(t) || isnan(f)) {
    Serial.println("Failed to read from DHT sensor!");
    return;
  }
  float hif = dht.computeHeatIndex(f, h);
  float hic = dht.computeHeatIndex(t, h, false);
  //Serial.print("Humidity: ");
//  Serial.print(h);
 // Serial.print(" %\t");
  //Serial.print("Temperature: ");
 // Serial.print(t);
  //Serial.print(" *C ");
//  Serial.print(f);
//  Serial.print(" *F\t");
 // Serial.print("Heat index: ");
 // Serial.print(hic);
 // Serial.print(" *C ");
//  Serial.print(hif);
//  Serial.println(" *F");

  if (Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 15000 || sendDataPrevMillis == 0)){
    sendDataPrevMillis = millis();
    // Write an Int number on the database path test/int
    if (Firebase.RTDB.pushFloat(&fbdo, "/sensorDB/temperature", t)){
     Serial.println("PASSED");
      Serial.println("PATH: " + fbdo.dataPath());
     Serial.println("TYPE: " + fbdo.dataType());
    }
    else {
      Serial.println("FAILED");
      Serial.println("REASON: " + fbdo.errorReason());
    }

    
    // Write an Float number on the database path test/float
    if (Firebase.RTDB.pushInt(&fbdo, "/sensorDB/humidity", h)){
      Serial.println("PASSED");
      Serial.println("PATH: " + fbdo.dataPath());
      Serial.println("TYPE: " + fbdo.dataType());
    }
    else {
      Serial.println("FAILED");
      Serial.println("REASON: " + fbdo.errorReason());
    }
       if (Firebase.RTDB.setInt(&fbdo, "/on_off2/fan", h)){
      Serial.println("PASSED");
      Serial.println("PATH: " + fbdo.dataPath());
      Serial.println("TYPE: " + fbdo.dataType());
    }
    else {
      Serial.println("FAILED");
      Serial.println("REASON: " + fbdo.errorReason());
    }
    
    if (Firebase.RTDB.getInt(&fbdo, "/on_off/fan")) {
      if(fbdo.dataType() == "int"){
        intValue = fbdo.intData();
        Serial.println(intValue);
      if (intValue == 1) {
        Serial.println(intValue);
        Serial.println("motor on");
        //here motor code
        digitalWrite(motor1Pin1, HIGH);
        digitalWrite(motor1Pin2, LOW);
        ledcWrite(pwmChannel, 100);   //세기조절 가능
       }
      else{
          Serial.println("Motor stopped");
          digitalWrite(motor1Pin1, LOW);
          digitalWrite(motor1Pin2, LOW);
        
      }
      }
      
      }else{
        Serial.println("jotTham");
        }
    }
    else {
      Serial.println(fbdo.errorReason());
    }
}
