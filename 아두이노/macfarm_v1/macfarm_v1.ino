#include "DHT.h"
#include <Arduino.h>
#if defined(ESP32)
#elif defined(ESP8266)
#include <ESP8266WiFi.h>
#endif
#include <Firebase_ESP_Client.h>
#include "time.h"

//Provide the token generation process info.
#include "addons/TokenHelper.h"
//Provide the RTDB payload printing info and other helper functions.
#include "addons/RTDBHelper.h"

// Insert your network credentials
#define WIFI_SSID "son"
#define WIFI_PASSWORD "123456789"
const char* ntpServer = "pool.ntp.org";
uint8_t timeZone = 9;
uint8_t summerTime = 0; // 3600
struct tm timeinfo;
String date;

// Insert Firebase project API Key
#define API_KEY "AIzaSyDqFzxgzics8NUugrOKBHB1lemosv32QUM"

// Insert RTDB URLefine the RTDB URL */
#define DATABASE_URL "https://smartfarmactivity-default-rtdb.firebaseio.com/"

//Define Firebase Data object
FirebaseData fbdo;

FirebaseAuth auth;
FirebaseConfig config;
#define DHTPIN 13        // GPIO23



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
bool real_push = false;
DHT dht(DHTPIN, DHTTYPE);

////////motor code add/////////////


int Dir1Pin_A = 25;      // 제어신호 1핀
int Dir2Pin_A = 26;      // 제어신호 2핀
int SpeedPin_A = 13;    // PWM제어를 위한 핀

int Relaypin = 33;//pump
int Relaypin2 = 32;//led

int soil_humi = 27;
int light = 14;


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
  configTime(3600 * timeZone, 3600 * summerTime, ntpServer); //init and get the time
  printLocalTime();
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

  check(h, t);

  print_data(h, t, l, s);

  if (Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 3000 || sendDataPrevMillis == 0)) {
    sendDataPrevMillis = millis();
    date = get_push_path();
    database("/user/auto/sensor/" + date + "/temp", t);
    database("/user/auto/sensor/" + date + "/humi", h);
    database("/user/auto/sensor/" + date + "/soilhumi", s);
    database("/user/auto/sensor/" + date + "/light", l);
  }

  if (Firebase.RTDB.getInt(&fbdo, "/user/mode") && fbdo.dataType() == "int") {
    intmode = fbdo.intData();
    if (intmode == 1) {
      fan_manual(intfan);
      pump_manual(intpump, s);
      led_manual(intled);
    }
    else {
      fan_auto(inttemp, t);
      pump_auto(inthumi, s);
      led_auto(intled, l);
    }
  }

}

void check(int h, int t) {
  if (isnan(h) || isnan(t)) {
    Serial.println("Failed to read from DHT  sensor!");
    return;
  }
}

void print_data(int h, int t, int l, int s) {
  Serial.print("humidity : ");
  Serial.println(h);
  Serial.print("temp : ");
  Serial.println(t);
  Serial.print("light : ");
  Serial.println(l);
  Serial.print("soil : ");
  Serial.println(s);
}



void database(String path, int sensordata) {
  getLocalTime(&timeinfo);

  if ((String(timeinfo.tm_min) == "1" || String(timeinfo.tm_min) == "31" ) && real_push == false) {
    if (Firebase.RTDB.pushInt(&fbdo, path, sensordata)) {
      Serial.println(sensordata);
      Serial.println("PASSED");
      Serial.println("PATH: " + fbdo.dataPath());
      Serial.println("TYPE: " + fbdo.dataType());
      real_push = true; //loop 시작하자마자 false로 고치는거 필요할듯
    }
    else {
      Serial.println("FAILED");
      Serial.println("REASON: " + fbdo.errorReason());
    }
  }
  else if ((String(timeinfo.tm_min) == "3" || String(timeinfo.tm_min) == "33" ) && real_push == true) {
    real_push = false;
  }
}
void fan_manual(int intfan) {
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
}

void pump_manual(int intpump, int s) {
  if (Firebase.RTDB.getInt(&fbdo, "/user/manual/device/pump") && fbdo.dataType() == "int") {
    intpump = fbdo.intData();
    if (s > 800) {//물 넘치지 않은 상태
      if (intpump == 1) {
        Serial.println("pump on");
        digitalWrite(Relaypin, HIGH);
      }
      else {
        Serial.println("pump stopped");
        digitalWrite(Relaypin, LOW);
      }
    }
    else {//물넘침 감지
      Serial.println("over water");
      digitalWrite(Relaypin, LOW);

      if (Firebase.RTDB.setInt(&fbdo, "/user/manual/exception", 1)) {

        Serial.println("EXCEPTION OCCUR");
        Serial.println("PATH: " + fbdo.dataPath());
        Serial.println("TYPE: " + fbdo.dataType());

      }

    }
  }
}

void led_manual(int intled) {
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
}

void fan_auto(int inttemp, int t) {
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

void pump_auto(int inthumi, int s) {
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

void led_auto(int intled, int l) {
  if (Firebase.RTDB.getInt(&fbdo, "/user/auto/userdata/light") && fbdo.dataType() == "int") {        //차후 센서값보면서 수정필요
    intlight = fbdo.intData();
    Serial.println(intValue);
    if (intlight < l && isday()) {
      Serial.println("led on");
      digitalWrite(Relaypin2, HIGH);
    }
    else {
      Serial.println("led off");
      digitalWrite(Relaypin2, LOW);
    }
  }
}

void printLocalTime() {
  struct tm timeinfo;
  if (!getLocalTime(&timeinfo)) {
    Serial.println("Failed to obtain time");
    return;
  }
  Serial.println(&timeinfo, "%A, %B %d %Y %H:%M:%S");

}
String get_push_path() {
  getLocalTime(&timeinfo);
  String path;
  path = String(timeinfo.tm_year + 1900 - 2000) + String(timeinfo.tm_mon + 1) + String(timeinfo.tm_mday);  //2022년 11월 16일 -> 221116
  return path;
}

bool isday() {//현재 낮인지 판단하는 함수
  getLocalTime(&timeinfo);
  if (timeinfo.tm_hour < 18 || timeinfo.tm_hour > 6) { 
    return true;
  }
  else {
    return false;
  }
}


/*
  bool real_push false; 선언해두고



  void database(char pass[], int data){
   getLocalTime(&timeinfo);

  if((String(timeinfo.min) =="1" || String(timeinfo.min) == "31" )&& real_push = false){
   if (Firebase.RTDB.pushInt(&fbdo, pass[], data)) {
      Serial.println(data);
      Serial.println("PASSED");
      Serial.println("PATH: " + fbdo.dataPath());
      Serial.println("TYPE: " + fbdo.dataType());
      real_push = true; //loop 시작하자마자 false로 고치는거 필요할듯
  }
  else {
    Serial.println("FAILED");
    Serial.println("REASON: " + fbdo.errorReason());
  }
  }
  }

*/
