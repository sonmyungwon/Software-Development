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
#define DHTPIN 3        // GPIO23
#define DHTTYPE DHT11   // DHT 22  (AM2302), AM2321

#define Manual 1
#define Auto 2
#define ON 1
#define OFF 0

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

int Relaypin = 1;
int Relaypin2 = 10;

int soil_humi = 5;
int light = 0;


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

void loop() {
  
  int h = dht.readHumidity();
  int t = dht.readTemperature();
  int value = analogRead(light);
  int l = map(value, 0, 4095, 255, 0);
  int s0 = analogRead(soil_humi);
  int s = s0 / 4;

  check(h,t);

  printData(h,t,l,s);
  
  if (Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 3000 || sendDataPrevMillis == 0)) {
    sendDataPrevMillis = millis();
    dataBase("/user/auto/sensor/temp", t);
    dataBase("/user/auto/sensor/humi", h);
    dataBase("/user/auto/sensor/soilhumi", s);
    dataBase("/user/auto/sensor/light", l);
  }

  if (Firebase.RTDB.getInt(&fbdo, "/user/mode") && fbdo.dataType() == "int") {
      intmode = fbdo.intData();
      if (intmode == Manaul) {
        fanManual(intfan);
        pumpManual(intpump);
        ledManual(intled);
      }
      else if (intmode == Auto) {
        fanAuto(inttemp, t);
        pumpAuto(inthumi, s);
        ledAuto(intled, l);
      }
  }
  
}

void check(int h, int t){
  if (isnan(h) || isnan(t)) {
    Serial.println("Failed to read from DHT sensor!");
    return;
  }
}

void printData(int h, int t, int l, int s){
  Serial.print("humidity : ");
  Serial.println(h);
  Serial.print("temp : ");
  Serial.println(t);
  Serial.print("light : ");
  Serial.println(l);
  Serial.print("soil : ");
  Serial.println(s);
}

void dataBase(char pass[], int data){
  if (Firebase.RTDB.pushInt(&fbdo, pass[], data)) {
      Serial.println(data);
      Serial.println("PASSED");
      Serial.println("PATH: " + fbdo.dataPath());
      Serial.println("TYPE: " + fbdo.dataType());
  }
  else {
    Serial.println("FAILED");
    Serial.println("REASON: " + fbdo.errorReason());
  }
}

void fanManual(int intfan){
  if (Firebase.RTDB.getInt(&fbdo, "/user/manual/device/fan") && fbdo.dataType() == "int") {
    intfan = fbdo.intData();
    if (intfan == ON) {
      Serial.println("motor on");
      digitalWrite(Dir1Pin_A, HIGH);         //모터가 시계 방향으로 회전
      digitalWrite(Dir2Pin_A, LOW);
      analogWrite(SpeedPin_A, 255); // 세기조절 가능
    }
    else if (intfan == OFF){
      Serial.println("Motor stopped");
      digitalWrite(Dir1Pin_A, LOW);
      digitalWrite(Dir2Pin_A, LOW);
    }
  }
}

void pumpManual(int intpump){
  if (Firebase.RTDB.getInt(&fbdo, "/user/manual/device/pump") && fbdo.dataType() == "int") {
    intpump = fbdo.intData();
    if (intpump == ON) {
      Serial.println("pump on");
      digitalWrite(Relaypin, HIGH);
    }
    else if (intpump == OFF){
      Serial.println("pump stopped");
      digitalWrite(Relaypin, LOW);
    }
  }
}

void ledManual(int intled){
  if (Firebase.RTDB.getInt(&fbdo, "/user/manual/device/led") && fbdo.dataType() == "int") {
    intled = fbdo.intData();
    if (intled == ON) {
      Serial.println("led on");
      digitalWrite(Relaypin2, HIGH);
    }
    else if (intled == OFF){
      Serial.println("led off");
      digitalWrite(Relaypin2, LOW);
    }
  }
}

void fanAuto(int inttemp, int t){
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
}

void pumpAuto(int inthumi, int s){
  if (Firebase.RTDB.getInt(&fbdo, "/user/auto/userdata/soil_humi") && fbdo.dataType() == "int") {
    inthumi = fbdo.intData();
    Serial.println(intValue);
    if ( inthumi < s) {                             //센서값받아보고 비교필요
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
}

void ledAuto(int intled, int l){
  if (Firebase.RTDB.getInt(&fbdo, "/user/auto/userdata/light") && fbdo.dataType() == "int") {        //차후 센서값보면서 수정필요
    intlight = fbdo.intData();
    Serial.println(intValue);
    if (intlight < l) {
      Serial.println("led on");
      digitalWrite(Relaypin2, HIGH);
    }
    else {
      Serial.println("led off");
      digitalWrite(Relaypin2, LOW);
    }
  }
}
