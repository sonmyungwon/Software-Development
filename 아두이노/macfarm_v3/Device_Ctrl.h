//Device_Ctrl.h
#ifndef Device_Ctrl_h
#define Device_Ctrl_h
#include <Arduino.h>


class Device_Ctrl
{
  public:
     void fanOn();
     void fanOff();
     void pumpOn();
     void pumpOff();
     void ledOn();
     void ledOff();
};

#endif
