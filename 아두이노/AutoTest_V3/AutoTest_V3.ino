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
#define WIFI_SSID "407_601-2"
#define WIFI_PASSWORD "bigsys601"

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
int intmode;
int intfan;
int intpump;
int intled;
int inttemp;
int inthumi;
int intlight;
float floatValue;
DHT dht(DHTPIN, DHTTYPE);

////////motor code add/////////////


int Dir1Pin_A = 25;      // 제어신호 1핀
int Dir2Pin_A = 26;      // 제어신호 2핀
int SpeedPin_A = 27;    // PWM제어를 위한 핀

int Relaypin = 2;
int Relaypin2 = 4;
int soil_humi;
int light;
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

  pinMode(Dir1Pin_A, OUTPUT);             // 제어 1번핀 출력모드 설정
  pinMode(Dir2Pin_A, OUTPUT);             // 제어 2번핀 출력모드 설정
  pinMode(SpeedPin_A, OUTPUT);            // PWM제어핀 출력모드 설정

  pinMode(Relaypin, OUTPUT);
  pinMode(Relaypin2, OUTPUT);
}
void push(&fbdo,String adress, int value) {
    if (Firebase.RTDB.pushInt(&fbdo, adress, value)) {
      Serial.println(value);
      Serial.println("PASSED");
      Serial.println("PATH: " + fbdo.dataPath());
      Serial.println("TYPE: " + fbdo.dataType());
    }
    else {
      Serial.println("FAILED");
      Serial.println("REASON: " + fbdo.errorReason());
    }
  }
  return;
}
void loop() {
  delay(2000);
  int h = dht.readHumidity();
  int t = dht.readTemperature();
  if (isnan(h) || isnan(t)) {
    Serial.println("Failed to read from DHT sensor!");
    return;
  }

  if (Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 3000 || sendDataPrevMillis == 0)) {
    sendDataPrevMillis = millis();
    if (Firebase.RTDB.pushInt(&fbdo, "/user/auto/sensor/temp", t)) {
      Serial.println(t);
      Serial.println("PASSED");
      Serial.println("PATH: " + fbdo.dataPath());
      Serial.println("TYPE: " + fbdo.dataType());
    }
    else {
      Serial.println("FAILED");
      Serial.println("REASON: " + fbdo.errorReason());
    }
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
    /*    lightness 센서값 push
        if (Firebase.RTDB.pushInt(&fbdo, "/user/auto/sensor/light", l)) {
          Serial.println("PASSED");
          Serial.println("PATH: " + fbdo.dataPath());
          Serial.println("TYPE: " + fbdo.dataType());
        }
        else {
          Serial.println("FAILED");
          Serial.println("REASON: " + fbdo.errorReason());
        }
        //soil_humi 센서값 push
        if (Firebase.RTDB.pushInt(&fbdo, "/user/auto/sensor/soil_humi", s)) {
          Serial.println("PASSED");
          Serial.println("PATH: " + fbdo.dataPath());
          Serial.println("TYPE: " + fbdo.dataType());
        }
        else {
          Serial.println("FAILED");
          Serial.println("REASON: " + fbdo.errorReason());
        }
    */
    // 자동제어

    if (Firebase.RTDB.getInt(&fbdo, "/user/mode")) {
      if (fbdo.dataType() == "int") {
        intmode = fbdo.intData();
        if (intmode == 2) {
          if (Firebase.RTDB.getInt(&fbdo, "/user/auto/userdata/temp") && fbdo.dataType() == "int") {
            inttemp = fbdo.intData();
            if (inttemp < t) {
              Serial.println(inttemp);
              Serial.println(t);
              Serial.println("motor on");
              digitalWrite(Dir1Pin_A, HIGH);         //모터가 시계 방향으로 회전
              digitalWrite(Dir2Pin_A, LOW);
              analogWrite(SpeedPin_A, 255);          //모터 속도를 최대로 설정
            }
            else {
              Serial.println("Motor stopped");
              digitalWrite(Dir1Pin_A, LOW);
              digitalWrite(Dir2Pin_A, LOW);
            }
          }
          if (Firebase.RTDB.getInt(&fbdo, "/user/auto/userdata/soil_humi") && fbdo.dataType() == "int") {
            inthumi = fbdo.intData();
            Serial.println(intValue);
            if ( inthumi < soil_humi) {                             //센서값받아보고 비교필요
              Serial.println(intValue);
              Serial.println("pump on");
              digitalWrite(Relaypin, HIGH);
              delay(5000);
              digitalWrite(Relaypin, LOW);
              delay(5000);
              Serial.println("pump off");
            }
            else if (inthumi < 450) {
              Serial.println("pump stopped");
              digitalWrite(Relaypin, LOW);
            }

          }
          if (Firebase.RTDB.getInt(&fbdo, "/user/auto/userdata/light") && fbdo.dataType() == "int") {        //차후 센서값보면서 수정필요
            intlight = fbdo.intData();
            Serial.println(intValue);
            if (intlight < 500) {
              Serial.println("led on");
              digitalWrite(Relaypin2, HIGH);
            }
            else {
              Serial.println("led off");
              digitalWrite(Relaypin2, LOW);
            }

          }
        }
      }
    }
    // 수동제어
    if (Firebase.RTDB.getInt(&fbdo, "/user/mode") && fbdo.dataType() == "int") {
      intmode = fbdo.intData();
      if (intmode == 1) {
        if (Firebase.RTDB.getInt(&fbdo, "/user/manual/device/fan") && fbdo.dataType() == "int") {
          intfan = fbdo.intData();
          if (intfan == 1) {
            Serial.println("motor on");
            digitalWrite(Dir1Pin_A, HIGH);         //모터가 시계 방향으로 회전
            digitalWrite(Dir2Pin_A, LOW);
            analogWrite(SpeedPin_A, 255); // 세기조절 가능
          }
          else {
            Serial.println("Motor stopped");
            digitalWrite(Dir1Pin_A, LOW);
            digitalWrite(Dir2Pin_A, LOW);
          }
        }
        if (Firebase.RTDB.getInt(&fbdo, "/user/manual/device/pump") && fbdo.dataType() == "int") {
          intpump = fbdo.intData();
          Serial.println(intValue);
          if (intpump == 1) {
            Serial.println(intpump);
            Serial.println("pump on");
            digitalWrite(Relaypin, HIGH);
          }
          else {
            Serial.println("pump stopped");
            digitalWrite(Relaypin, LOW);
          }
        }
        if (Firebase.RTDB.getInt(&fbdo, "/user/manual/device/led") && fbdo.dataType() == "int") {
          intled = fbdo.intData();
          if (intled == 1) {
            Serial.println("led on");
            digitalWrite(Relaypin2, HIGH);
          }
          else {
            Serial.println("led off");
            digitalWrite(Relaypin2, LOW);
          }
        }
        else {
          Serial.println(fbdo.errorReason());
        }
      }
    }//manual control part end
  }
}
