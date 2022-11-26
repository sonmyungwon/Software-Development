// file: NewClass.cpp
#include "Device_Ctrl.h"

const int dir1Pin = 27;      // 제어신호 1핀
const int dir2Pin = 26;      // 제어신호 2핀
const int speedPin = 14;    // PWM제어를 위한 핀
const int pumpRelayPin = 12;  // pump 릴레이 핀
const int ledRelayPin = 13;   // led 릴레이 핀 


void Device_Ctrl::fanOn()
{
  Serial.println("motor on");
  digitalWrite(dir1Pin, HIGH);         //모터가 시계 방향으로 회전
  digitalWrite(dir2Pin, LOW);
  analogWrite(speedPin, 255); // 세기조절 가능
}

void Device_Ctrl::fanOn()
{
  Serial.println("Motor stopped");
  digitalWrite(dir1Pin, LOW);
  digitalWrite(dir2Pin, LOW);
}

void Device_Ctrl::pumpOn()
{
  Serial.println("pump on");
  digitalWrite(pumpRelaypin, HIGH);
}

void Device_Ctrl::pumpOff()
{
  Serial.println("pump off");
  digitalWrite(pumpRelaypin, LOW);
}

void Device_Ctrl::ledOn()
{
  Serial.println("led on");
  digitalWrite(ledRelaypin, HIGH);
}

void Device_Ctrl::ledOff()
{
  Serial.println("led on");
  digitalWrite(ledRelaypin, HIGH);
}
