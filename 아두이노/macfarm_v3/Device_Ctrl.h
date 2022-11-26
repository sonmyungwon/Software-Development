//Device_Ctrl.h
#ifndef Device_Ctrl_h
#define Device_Ctrl_h
#include <Arduino.h>


class Device_Ctrl
{
  public:
     void fanOn(int dir1Pin, int dir2Pin, int speedPin);
     void fanOff(int dir1Pin, int dir2Pin);
     void pumpOn(int pumpRelayPin);
     void pumpOff(int pumpRelayPin);
     void ledOn(int ledRelayPin);
     void ledOff(int ledRelayPin);
};

#endif
