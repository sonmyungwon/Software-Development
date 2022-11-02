
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
int green = 2;
int blue = 1;
int red = 3;



#define DHTTYPE DHT11   // DHT 22  (AM2302), AM2321


unsigned long sendDataPrevMillis = 0;
bool signupOK = false;
int intValue;
float floatValue;
DHT dht(DHTPIN, DHTTYPE);

////////motor code add/////////////


int motor1Pin1 = 4;      // 제어신호 1핀
int motor1Pin2 = 5;      // 제어신호 2핀
int enable1Pin = 6;    // PWM제어를 위한 핀
int soil_humi = 9;

const int freq = 30000;
const int pwmChannel = 0;
const int resolution = 8;
int dutyCycle = 200;
//////////////////pump add///////////////////////////

int Relaypin = 7;
/////////////

void setup() {
  Serial.begin(115200);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
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
  if (Firebase.signUp(&config, &auth, "", "")) {
    Serial.println("ok");
    signupOK = true;
  }
  else {
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
  pinMode(green, OUTPUT);
  pinMode(blue, OUTPUT);
  pinMode(red, OUTPUT);
  // configure LED PWM functionalitites
  ledcSetup(pwmChannel, freq, resolution);

  // attach the channel to the GPIO to be controlled
  ledcAttachPin(enable1Pin, pwmChannel);

  // testing
  Serial.print("Testing DC Motor...");
  //////////////////////////////
  pinMode(Relaypin, OUTPUT);
}

void loop() {
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
  Serial.print("Humidity: ");
  Serial.print(h);
  Serial.print(" %\t");
  Serial.print("Temperature: ");
  Serial.print(t);
  Serial.print(" *C ");
  //Serial.print("Moisture  Value: ");
  // Serial.println(analogRead(9));

  if (Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 15000 || sendDataPrevMillis == 0)) {
    sendDataPrevMillis = millis();
    // Write an Int number on the database path test/int

    if (Firebase.RTDB.pushFloat(&fbdo, "/user/auto/sensor/temp", t)) {
      Serial.println(t);

      Serial.println("PASSED");
      Serial.println("PATH: " + fbdo.dataPath());
      Serial.println("TYPE: " + fbdo.dataType());
    }
    else {
      Serial.println("FAILED");
      Serial.println("REASON: " + fbdo.errorReason());
    }


    // Write an Float number on the database path test/float
    if (Firebase.RTDB.pushInt(&fbdo, "/user/auto/sensor/humi", h)) {
      Serial.println(h);
      Serial.println("PASSED");
      Serial.println("PATH: " + fbdo.dataPath());
      Serial.println("TYPE: " + fbdo.dataType());
    }
    else {
      Serial.println("FAILED");
      Serial.println("REASON: " + fbdo.errorReason());
    }
    //lightness 센서값 push
    //    if (Firebase.RTDB.pushInt(&fbdo, "/user/auto/sensor/light", L)) {
    //      Serial.println("PASSED");
    //      Serial.println("PATH: " + fbdo.dataPath());
    //      Serial.println("TYPE: " + fbdo.dataType());
    //    }
    //    else {
    //      Serial.println("FAILED");
    //      Serial.println("REASON: " + fbdo.errorReason());
    //    }
    //    //soil_humi 센서값 push
    //    if (Firebase.RTDB.pushInt(&fbdo, "/user/auto/sensor/soil_humi", S)) {
    //      Serial.println("PASSED");
    //      Serial.println("PATH: " + fbdo.dataPath());
    //      Serial.println("TYPE: " + fbdo.dataType());
    //    }
    //    else {
    //      Serial.println("FAILED");
    //      Serial.println("REASON: " + fbdo.errorReason());
    //    }


    /*   if (Firebase.RTDB.setInt(&fbdo, "/user/auto/sensor_Get/humi", h)){
         Serial.println("PASSED");
         Serial.println("PATH: " + fbdo.dataPath());
         Serial.println("TYPE: " + fbdo.dataType());
       }
       else {
         Serial.println("FAILED");
         Serial.println("REASON: " + fbdo.errorReason());
       }

    */
    if (Firebase.RTDB.getInt(&fbdo, "/user/mode")) {
      if (fbdo.dataType() == "int") {
        intValue = fbdo.intData();
        if (intValue == 2) {
          if (Firebase.RTDB.getInt(&fbdo, "/user/auto/userdata/temp")) {
            if (fbdo.dataType() == "int") {
              intValue = fbdo.intData();
              Serial.println(intValue);
              if (intValue < 50) {
                Serial.println(intValue);
                Serial.println("motor on");
                //here motor code
                digitalWrite(motor1Pin1, HIGH);
                digitalWrite(motor1Pin2, LOW);
                ledcWrite(pwmChannel, 100);   //세기조절 가능 습도가 높으면 점점 세기를 높이는 알고리즘 적용필요
              }
              else {
                Serial.println("Motor stopped");
                digitalWrite(motor1Pin1, LOW);
                digitalWrite(motor1Pin2, LOW);
              }
            }
          }
          if (Firebase.RTDB.getInt(&fbdo, "/user/auto/userdata/soil_humi")) {
            if (fbdo.dataType() == "int") {
              intValue = fbdo.intData();
              Serial.println(intValue);
              if (intValue < 300) {                              //센서값받아보고 비교필요
                Serial.println(intValue);
                Serial.println("pump on");
                digitalWrite(Relaypin, HIGH);
                delay(5000);
                digitalWrite(Relaypin, LOW);
                delay(5000);
                Serial.println("pump off");
              }
              else if (intValue > 700) {
                Serial.println("pump stopped");
                digitalWrite(Relaypin, LOW);
              }
            }
          }
          if (Firebase.RTDB.getInt(&fbdo, "/user/auto/userdata/light")) {         //차후 센서값보면서 수정필요
            if (fbdo.dataType() == "int") {
              intValue = fbdo.intData();
              Serial.println(intValue);
              if (intValue < 500) {
                Serial.println(intValue);
                Serial.println("led on");
                digitalWrite(green, HIGH);
                digitalWrite(blue, HIGH);
                digitalWrite(red, HIGH);
                delay(1000);
              }
              else {
                Serial.println("led off");
                digitalWrite(green, LOW);
                digitalWrite(blue, LOW);
                digitalWrite(red, LOW);
                delay(1000);
              }
            }
          }
        }
      }
    }

    if (Firebase.RTDB.getInt(&fbdo, "/user/mode")) {
      if (fbdo.dataType() == "int") {
        intValue = fbdo.intData();
        if (intValue == 1) {
          if (Firebase.RTDB.getInt(&fbdo, "/user/manual/device/fan")) {
            if (fbdo.dataType() == "int") {
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
              else {
                Serial.println("Motor stopped");
                digitalWrite(motor1Pin1, LOW);
                digitalWrite(motor1Pin2, LOW);
              }
            }
          }
          else
          {
            Serial.println(fbdo.errorReason());
          }
        }
      }
      if (fbdo.dataType() == "int") {
        intValue = fbdo.intData();
        if (intValue == 1) {
          if (Firebase.RTDB.getInt(&fbdo, "/user/manual/device/pump")) {
            if (fbdo.dataType() == "int") {
              intValue = fbdo.intData();
              Serial.println(intValue);
              if (intValue == 0) {
                Serial.println(intValue);
                Serial.println("pump on");
                digitalWrite(Relaypin, HIGH);
                delay(5000);
                digitalWrite(Relaypin, LOW);
                delay(5000);
                Serial.println("pump off");
              }
              else {
                Serial.println("pump stopped");
                digitalWrite(Relaypin, LOW);
              }
            }
          }
          else
          {
            Serial.println(fbdo.errorReason());
          }
        }
      }
      if (fbdo.dataType() == "int") {
        intValue = fbdo.intData();
        if (intValue == 1) {
          if (Firebase.RTDB.getInt(&fbdo, "/user/manual/device/led")) {
            if (fbdo.dataType() == "int") {
              intValue = fbdo.intData();
              Serial.println(intValue);
              if (intValue == 0) {
                Serial.println(intValue);
                Serial.println("led on");
                digitalWrite(green, HIGH);
                digitalWrite(blue, HIGH);
                digitalWrite(red, HIGH);
                delay(1000);
              }
              else {
                Serial.println("led off");
                digitalWrite(green, LOW);
                digitalWrite(blue, LOW);
                digitalWrite(red, LOW);
                delay(1000);
              }
            }
          }
          else
          {
            Serial.println(fbdo.errorReason());
          }
        }
      }
    }//manual control part end
  }
}
