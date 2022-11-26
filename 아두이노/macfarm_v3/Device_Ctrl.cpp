// file: NewClass.cpp
#include "Device_Ctrl.h"


void Device_Ctrl::fanOn(int dir1Pin, int dir2Pin, int speedPin)
{
  Serial.println("motor on");
  digitalWrite(dir1Pin, HIGH);         //모터가 시계 방향으로 회전
  digitalWrite(dir2Pin, LOW);
  analogWrite(speedPin, 255); // 세기조절 가능
}

void Device_Ctrl::fanOff(int dir1Pin, int dir2Pin)
{
  Serial.println("Motor stopped");
  digitalWrite(dir1Pin, LOW);
  digitalWrite(dir2Pin, LOW);
}

void Device_Ctrl::pumpOn(int pumpRelayPin)
{
  Serial.println("pump on");
  digitalWrite(pumpRelayPin, HIGH);
}

void Device_Ctrl::pumpOff(int pumpRelayPin)
{
  Serial.println("pump off");
  digitalWrite(pumpRelayPin, LOW);
}

void Device_Ctrl::ledOn(int ledRelayPin)
{
  Serial.println("led on");
  digitalWrite(ledRelayPin, HIGH);
}

void Device_Ctrl::ledOff(int ledRelayPin)
{
  Serial.println("led off");
  digitalWrite(ledRelayPin, LOW);
}
