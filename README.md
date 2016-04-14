# Organicity-BLE
The BleOrgLib.h is a library that implement the specific communication protocol between the BLE device an the Organicity phone application. Allow it you to upload your sensor values on Organicity facility. Exist some libraries than implement a general protocol between BLE and android but are so big that Blend Arduino did not have any space rest for the digital sensor libraries. Just download this library from Github, copy past a template “organicity.ino” and place your code on the corresponding spaces. 

Code parts:
--

1. Firstly the next 4 variables must be declare:
  - An int num_var must be declare and initialized with the number of sensor values that you want to send to Organicity 
  ```cpp
  int num_var=...;  
  ```
  - An string array called variable_name with a maximum of 15 elements must be declare and initialized with the specific names for your sensors. Notice that you can report to Organicity a maximum of 15 sensor values.
  ```cpp
  String variables_name[15]={"SensorName1","SensorName2"};  
  ```
  - An array of 15 floats position must be declare. Later on your code you just must to include each sensor value on the position of the array that correspond with their name on variables_name array. Notice that the  values must be float.
   ```cpp
  float variables_value[15];  
  ```
  - You must named here you device. Notice that the name must start with “Blend” and have at most 10 characters. 
  ```cpp
  char* device_name="DeviceName";  
  ```
2. Include your libraries and necessary general variables.
3. On the body of the function sensorSetup() include the code that you want to be run on the Arduino setup().Initialization of sensors and routines. You need to declare also here the chosen pins for  REQN and RDYN. On the case of Arduino blend will be 6 and 7.

  ```cpp
  void sensorSetup()
{
	//////////////RUN ON ARDUINO Setup()////////
	/*
   		Include your code that will be initialize on the setup() 
  */
  ////////////////////////////////////////////
  // For BLE Shield and Blend:
  //   Default pins set to 9 and 8 for REQN and RDYN
  //   Set your REQN and RDYN here before ble_begin() if you need
  //   Need to set the REQN and RDYN pins //
  // For Blend Micro:
  //   Default pins set to 6 and 7 for REQN and RDYN
  //   So, no need to set for Blend Micro.
  ble_set_pins(6, 7);
}  
  ```
4. On the body of the function sensorLoop() include the code that you want to be run on the Arduino loop(). Read here your sensors and actualize their value on the variables_value[15] array. Notice that num_var sensor values must be update each time. 

  ```cpp
  void sensorLoop()
{
  /////////////////RUN ON ARDUINO Loop()///////////////////
	/*
   		Include your code that will be run on each loop  
   */
  //////////////////////////////////////////////////////// 
  variables_value[0]=SensorValue1;
  variables_value[1]=SensorValue2;
  ...
  //num_var sensor values must be update each time
  
}  
  ```
5. On the body of the function sensorReset() you can reset all the necessary variables. This function is calling just after the last sensor value is send it. If before of the next read of the sensors you need to initialized any variable, here is the space to make it.

  ```cpp
void sensorReset()
{
  ////////RUN JUST AFTER OF SEND THE LAST SENSOR VALUES////////
  /*	
	 If you need reset any variable of you program later of have been
	 send data to Organicity make it on this function
  */
  ////////////////////////////////////////////////////////////
}
```
OrgBleEnvironmentalRead device
--
This example inlcude THO2 Humidity&Temperature sensor,multichannel gas sensor and Dust sensor. Notice that need to be used the included it library for the THO2. Has been Include it method to Power on and off the sensor in order to can connect on the same I2C bus the multichannel gas sensor. 
