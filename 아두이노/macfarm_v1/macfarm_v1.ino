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

//Provide the token generation process info.
#include "addons/TokenHelper.h"
//Provide the RTDB payload printing info and other helper functions.
#include "addons/RTDBHelper.h"

// Insert your network credentials
#define WIFI_SSID "YOURWIFI"
#define WIFI_PASSWORD "YOURPASSWORD"

// Insert Firebase project API Key
#define API_KEY "YOURAPI"

// Insert RTDB URLefine the RTDB URL */
#define DATABASE_URL "YOURURL"

//Define Firebase Data object
#define DHTPIN 15        // GPIO23
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
const char* ntpServer = "pool.ntp.org";
uint8_t timeZone = 9;
uint8_t summerTime = 0; // 3600
struct tm timeinfo;
String date;
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;
BH1750 lightMeter;

DHT dht(DHTPIN, DHTTYPE);

////////motor code add/////////////


int Dir1Pin_A = 27;      // 제어신호 1핀
int Dir2Pin_A = 26;      // 제어신호 2핀
int SpeedPin_A = 14;    // PWM제어를 위한 핀

int pumpRelaypin = 12;//pump
int ledRelaypin = 13;//led

int soil_humi = 32;



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

  pinMode(pumpRelaypin, OUTPUT);
  pinMode(ledRelaypin, OUTPUT);

  Wire.begin();

  lightMeter.begin();

}

void loop() {

  int humi = dht.readHumidity();
  int temp = dht.readTemperature();
  int light = lightMeter.readLightLevel();
  int s0 = analogRead(soil_humi);
  int soilhumi = s0 / 4;

  check(humi, temp);

  print_data(humi, temp, light, sdilHumi);

  if (Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 3000 || sendDataPrevMillis == 0)) {
    sendDataPrevMillis = millis();
    date = get_push_path();
    database("/user/sensor/" + date + "/temp", temp);
    database("/user/sensor/" + date + "/humi", humi);
    database("/user/sensor/" + date + "/soilhumi", soilhumi);
    database("/user/sensor/" + date + "/light", light);
  }

  if (Firebase.RTDB.getInt(&fbdo, "/user/mode") && fbdo.dataType() == "int") {
    intmode = fbdo.intData();
    if (intmode == 1) {
      fan_manual(intfan);
      pump_manual(intpump, soilhumi);
      led_manual(intled);
    }
    else if (intmode==2){
      fan_auto(inttemp, temp);
      pump_auto(inthumi, soilhumi);
      led_auto(intled, light);
    }
    else{
      Serial.println("mode error");
    }
  }

}

void check(int humi, int temp) {
  if (isnan(humi) || isnan(temp)) {
    Serial.println("Failed to read from DHT  sensor!");
    return;
  }
}

void print_data(int humi, int temp, int light, int soilhumi) {
  Serial.print("humidity : ");
  Serial.println(humi);
  Serial.print("temp : ");
  Serial.println(temp);
  Serial.print("light : ");
  Serial.println(light);
  Serial.print("soil : ");
  Serial.println(soilhumi);
}



void database(String path, int sensordata) {
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
void fan_manual(int intfan) {
  if (Firebase.RTDB.getInt(&fbdo, "/user/device/fan") && fbdo.dataType() == "int") {
    intfan = fbdo.intData();
    if (intfan == 1) {
      Serial.println("motor on");
      digitalWrite(Dir1Pin_A, HIGH);         //모터가 시계 방향으로 회전
      digitalWrite(Dir2Pin_A, LOW);
      analogWrite(SpeedPin_A, 255); // 세기조절 가능
      realCheck("/user/device/real_fan_on",1);
    }
    else {
      Serial.println("Motor stopped");
      digitalWrite(Dir1Pin_A, LOW);
      digitalWrite(Dir2Pin_A, LOW);
      realCheck("/user/device/real_fan_on",0);
    }
  }
}

void pump_manual(int intpump, int soilhumi) {
  if (Firebase.RTDB.getInt(&fbdo, "/user/device/pump") && fbdo.dataType() == "int") {
    intpump = fbdo.intData();
    if (soilhumi > 500) {//물 넘치지 않은 상태
      if (intpump == 1) {
        Serial.println("pump on");
        digitalWrite(pumpRelaypin, HIGH);
         realOn("realpump");
      }
      else {
        Serial.println("pump stopped");
        digitalWrite(pumpRelaypin, LOW);
        realCheck("/user/device/real_pump_on",0);
      }
    }
    else {//물넘침 감지
      Serial.println("over water");
      digitalWrite(pumpRelaypin, LOW);
      realCheck("/user/device/real_pump_on",0);
      if (Firebase.RTDB.setInt(&fbdo, "/user/exception", 1)) {

        Serial.println("EXCEPTION OCCUR");
        Serial.println("PATH: " + fbdo.dataPath());
        Serial.println("TYPE: " + fbdo.dataType());
      }
    }
  }
}

void realCheck(String path, int num) {
  Firebase.RTDB.setInt(&fbdo, path, num)
}

void led_manual(int intled) {
  if (Firebase.RTDB.getInt(&fbdo, "/user/device/led") && fbdo.dataType() == "int") {
    intled = fbdo.intData();
    if (intled == 1) {
      Serial.println("led on");
      digitalWrite(ledRelaypin, HIGH);
      realCheck("/user/device/real_led_on",1);
    }
    else {
      Serial.println("led off");
      digitalWrite(ledRelaypin, LOW);
      realCheck("/user/device/real_led_on",0);
    }
  }
}

void fan_auto(int inttemp, int temp) {
  if (Firebase.RTDB.getInt(&fbdo, "/user/userdata/temp") && fbdo.dataType() == "int") {
    inttemp = fbdo.intData();
    if (inttemp < temp) {
      Serial.println(inttemp);
      Serial.println(temp);
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

void pump_auto(int inthumi, int soilhumi) {
  if (Firebase.RTDB.getInt(&fbdo, "/user/userdata/soil_humi") && fbdo.dataType() == "int") {
    inthumi = fbdo.intData();
    Serial.println(inthumi);
    if ( inthumi < soilhumi) {                             //센서값받아보고 비교필요
      Serial.println(inthumi);
      Serial.println("pump on");
      digitalWrite(pumpRelaypin, HIGH);
      delay(2500);
      digitalWrite(pumpRelaypin, LOW);
      delay(5000);
      Serial.println("pump off");
    }
    else if (inthumi > soilhumi) {
      Serial.println("pump stopped");
      digitalWrite(pumpRelaypin, LOW);
    }
  }
}

void compareLedauto(int intled, int light) {
  if (Firebase.RTDB.getInt(&fbdo, "/user/userdata/light") && fbdo.dataType() == "int") {        //차후 센서값보면서 수정필요
    intlight = fbdo.intData();
    Serial.println(intlight);
    if (intlight > light && isday()) {
      Serial.println("led on");
      digitalWrite(ledRelaypin, HIGH);
    }
    else {
      Serial.println("led off");
      digitalWrite(ledRelaypin, LOW);
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
