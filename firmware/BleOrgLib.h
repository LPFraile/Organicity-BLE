/*"name": "BleOrgLib.h",
Author: "LPFraile <lidiapf0@gmail.com>",
License: "BSD",
Version: "0.0.1",
Description: "Connect your Blend devices with Organicity"
File: source file
*/

#ifndef BleOrgLib_h
#define BleOrgLib_h
#include "Arduino.h"
#include <SPI.h>
#include <boards.h>
#include <RBL_nRF8001.h>

void sensorSetup();
void sensorLoop();
void sensorReset();
extern String variables_name[15];
extern float variables_value[15];
extern int num_var;
extern char* device_name;
//extern String device_name;
class BleOrgLib
{
  public:
    BleOrgLib();
    void orgSetup();
    void orgRun();
    void orgFunctions(); 
 
  private:
    char* dname;
    String d_name;
    int numvar;
    int index;
    int lenghtname;
    void SendVariables();
    void SendfFloatVariable(String name,float floatVar);
    void ble_write_string(byte *bytes, uint8_t len);
  
};

#endif
