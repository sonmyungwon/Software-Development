#include "DHT.h"
#include <Arduino.h>
#if defined(ESP32)
#elif defined(ESP8266)
#include <ESP8266WiFi.h>
#endif
#include <Firebase_ESP_Client.h>
#include "time.h"
#include <Wire.h>
#include <BH1750.h>

//Provide the token generation process info..
#include "addons/TokenHelper.h"
//Provide the RTDB payload printing info and other helper functions.
#include "addons/RTDBHelper.h"

// Insert your network credentials
const String  WIFI_SSID = "YOURWIFI";
const String WIFI_PASSWORD = "YOURPASSWORD";

// Insert Firebase project API Key
const String API_KEY = "YOURAPI";

// Insert RTDB URLefine the RTDB URL */
const String DATABASE_URL = "YOURURL";

//Define Firebase Data object
const int DHTPIN = 15        // GPIO23

unsigned long sendDataPrevMillis = 0;
bool signupOK = false;
int intmode;      //설정 모드
int fan_signal;     //사용자가 명령한 fan on/off 신호
int pump_signal;      //사용자가 명령한 pump on/off 신호
int led_signal;       //사용자가 명령한 led on/off 신호
int temp_user_setting;    //사용자가 설정한 온도 값
int shumi_user_setting;   //사용자가 설정한 토양습도 값
int light_user_setting;   //사용자가 설정한 조도 값
bool real_push = false;     //실제 데이터베이스 push 여부 확인
String date;              //날짜 변수
const char* ntpServer = "pool.ntp.org";
uint8_t timeZone = 9;
uint8_t summerTime = 0; // 3600
struct tm timeinfo;
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;
BH1750 lightMeter;

DHT dht(DHTPIN, DHT11);


int dir1Pin = 27;      // 제어신호 1핀
int dir2Pin = 26;      // 제어신호 2핀
int speedPin = 14;    // PWM제어를 위한 핀
int pumpRelayPin = 12;  // pump 릴레이 핀
int ledRelayPin = 13;   // led 릴레이 핀 
int soilhumiPin = 32;   // 토양습도센서 핀

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

  pinMode(dir1Pin, OUTPUT);     // 제어 1번핀 출력모드 설정
  pinMode(dir2Pin, OUTPUT);     // 제어 2번핀 출력모드 설정
  pinMode(speedPin, OUTPUT);    // PWM제어핀 출력모드 설정

  pinMode(pumpRelaypin, OUTPUT);  // pump 릴레이핀 출력모드 설정
  pinMode(ledRelaypin, OUTPUT);   // led 릴레이핀 출력모드 설정

  Wire.begin();

  lightMeter.begin();

}

void loop() {

  int humi = dht.readHumidity();  // 읽은 습도 데이터 값
  int temp = dht.readTemperature();   // 읽은 온도 데이터 값
  int light = lightMeter.readLightLevel();   // 읽은 빛 데이터 값
  int s0 = analogRead(soilhumiPin);
  int soilhumi = s0 / 4;   // 읽은 토양습도 데이터 값

  check(humi, temp);

  printData(humi, temp, light, sdilHumi);

  // 센서 데이터 값 3초 마다 데이터베이스로 옮김
  if (Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 3000 || sendDataPrevMillis == 0)) {
    sendDataPrevMillis = millis();
    date = getDate();
    pushData("/user/sensor/" + date + "/temp", temp);
    pushData("/user/sensor/" + date + "/humi", humi);
    pushData("/user/sensor/" + date + "/soilhumi", soilhumi);
    pushData("/user/sensor/" + date + "/light", light);
  }

  // 데이터베이스에서 mode를 받음
  if (Firebase.RTDB.getInt(&fbdo, "/user/mode") && fbdo.dataType() == "int") {
    intmode = fbdo.intData();
    if (intmode == 1) {
      retrieveFan(fan_signal);
      retrievePump(pump_signal, soilhumi);
      retrieveLed(led_signal);
    }
    else if (intmode==2){
      compareFan(temp_user_setting, temp);
      comparePump(shumi_user_setting, soilhumi);
      compareLed(led_signal, light);
    }
    else{
      Serial.println("mode error");
    }
  }

}

// 온습도 센서 오류 확인
void check(int humi, int temp) {
  if (isnan(humi) || isnan(temp)) {
    Serial.println("Failed to read from DHT  sensor!");
    return;
  }
}

// 센서 데이터 값 확인
void printData(int humi, int temp, int light, int soilhumi) {
  Serial.print("humidity : ");
  Serial.println(humi);
  Serial.print("temp : ");
  Serial.println(temp);
  Serial.print("light : ");
  Serial.println(light);
  Serial.print("soil : ");
  Serial.println(soilhumi);
}

// 데이터 값을 데이터베이스로 옮기는 함수
void pushData(String path, int sensordata) {
  getLocalTime(&timeinfo);
  while (real_push == false) {
    if (String(timeinfo.tm_min) == "0" || String(timeinfo.tm_min) == "30" ) {
      if (Firebase.RTDB.pushInt(&fbdo, path, sensordata)) {
        Serial.println(sensordata);
        Serial.println(path + ": PASSED");
        real_push = true; //loop 시작하자마자 false로 고치는거 필요할듯
      }
      else {
        Serial.println("FAILED");
        Serial.println("REASON: " + fbdo.errorReason());
      }
    }
  }
  real_push == false;
}

// 장치가 실제 작동 여부 신호를 데이터베이스로 전달
void realCheck(String path, int num) {
  Firebase.RTDB.setInt(&fbdo, path, num)
}

// fan on 함수
void fanOn(){
  Serial.println("motor on");
  digitalWrite(dir1Pin, HIGH);         //모터가 시계 방향으로 회전
  digitalWrite(dir2Pin, LOW);
  analogWrite(speedPin, 255); // 세기조절 가능
}

// fan off 함수
void fanOff(){
  Serial.println("Motor stopped");
  digitalWrite(dir1Pin, LOW);
  digitalWrite(dir2Pin, LOW);
}

// pump on 함수
void pumpOn(){
  Serial.println("pump on");
  digitalWrite(pumpRelaypin, HIGH);
}

// pump off 함수
void pumpOff(){
  Serial.println("pump off");
  digitalWrite(pumpRelaypin, LOW);
}

// led on 함수
void ledOn(){
  Serial.println("led on");
  digitalWrite(ledRelaypin, HIGH);
}

// led off 함수
void ledOff(){
  Serial.println("led on");
  digitalWrite(ledRelaypin, HIGH);
}

// fan 수동제어 함수
void retrieveFan(int fan_signal) {
  if (Firebase.RTDB.getInt(&fbdo, "/user/device/fan") && fbdo.dataType() == "int") {
    fan_signal = fbdo.intData();
    if (fan_signal == 1) {
      fanOn();
      realCheck("/user/device/real_fan_on",1);
    }
    else {
      fanOff();
      realCheck("/user/device/real_fan_on",0);
    }
  }
}

// pump 수동제어 함수
void retrievePump(int pump_signal, int soilhumi) {
  //Firebase로부터 사용자의 onoff 신호를 받는 부분
  if (Firebase.RTDB.getInt(&fbdo, "/user/device/pump") && fbdo.dataType() == "int") {
    pump_signal = fbdo.intData();
    if (soilhumi > 500) {//물 넘치지 않은 상태
      if (pump_signal == 1) {
        pumpOn();
        realCheck("/user/device/real_pump_on",1);
      }
      else {
        pumpOff();
        realCheck("/user/device/real_pump_on",0);
      }
    }
    else {//물넘침 감지
      Serial.println("over water");
      pumpOff();
      realCheck("/user/device/real_pump_on",0);
      if (Firebase.RTDB.setInt(&fbdo, "/user/exception", 1)) {

        Serial.println("EXCEPTION OCCUR");
        Serial.println("PATH: " + fbdo.dataPath());
        Serial.println("TYPE: " + fbdo.dataType());
      }
    }
  }
}

// led 수동제어 함수
void retrieveLed(int led_signal) {
  if (Firebase.RTDB.getInt(&fbdo, "/user/device/led") && fbdo.dataType() == "int") {
    led_signal = fbdo.intData();
    if (led_signal == 1) {
      ledOn();
      realCheck("/user/device/real_led_on",1);
    }
    else {
      ledOff();
      realCheck("/user/device/real_led_on",0);
    }
  }
}

// fan 자동제어 함수
void compareFan(int temp_user_setting, int temp) {
  if (Firebase.RTDB.getInt(&fbdo, "/user/userdata/temp") && fbdo.dataType() == "int") {
    temp_user_setting = fbdo.intData();
    if (temp_user_setting < temp) {
      Serial.println(temp_user_setting);
      Serial.println(temp);
      fanOn();
    }
    else {
      fanOff();
    }
  }
}

// pump 자동제어 함수
void comparePump(int shumi_user_setting, int soilhumi) {
  if (Firebase.RTDB.getInt(&fbdo, "/user/userdata/soil_humi") && fbdo.dataType() == "int") {
    shumi_user_setting = fbdo.intData();
    Serial.println(shumi_user_setting);
    if ( shumi_user_setting < soilhumi) {                             //센서값받아보고 비교필요
      Serial.println(shumi_user_setting);
      pumpOn();
      delay(2500);
      pumpOff();
      delay(5000);
    }
    else if (shumi_user_setting > soilhumi) {
      pumpOff();
    }
  }
}

// led 자동제어 함수
void compareLed(int light_user_setting, int light) {
  if (Firebase.RTDB.getInt(&fbdo, "/user/userdata/light") && fbdo.dataType() == "int") {        //차후 센서값보면서 수정필요
    light_user_setting = fbdo.intData();
    Serial.println(light_user_setting);
    if (light_user_setting > light && isDay()) {
      ledOn();
    }
    else {
      ledOff();
    }
  }
}

// 시간 가져오는 함수
void printLocalTime() {
  struct tm timeinfo;
  if (!getLocalTime(&timeinfo)) {
    Serial.println("Failed to obtain time");
    return;
  }
  Serial.println(&timeinfo, "%A, %B %d %Y %H:%M:%S");
}

// 날짜 구하는 함수
String getDate() {
  getLocalTime(&timeinfo);
  String path;
  path = String(timeinfo.tm_year + 1900 - 2000) + String(timeinfo.tm_mon + 1) + String(timeinfo.tm_mday);  //2022년 11월 16일 -> 221116
  return path;
}

//현재 낮인지 판단하는 함수
bool isDay() {
  getLocalTime(&timeinfo);
  if (timeinfo.tm_hour < 18 || timeinfo.tm_hour > 6) {
    return true;
  }
  
  else {
    return false;
  }
}
