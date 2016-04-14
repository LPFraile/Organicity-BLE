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

BleScanExperiment
--
Instractable 1: Organicity with Arduino Blend 
--

This example inlcude THO2 Humidity&Temperature sensor,multichannel gas sensor and Dust sensor. Notice that need to be used the included it library for the THO2. Has been Include it method to Power on and off the sensor in order to can connect on the same I2C bus the multichannel gas sensor. Also you will need download the Multichannel gas sensor library from git@github.com:Seeed-Studio/Mutichannel_Gas_Sensor.git. Load the orgBleEnvironmentalRead.iso example on your arduino Blend and connect the elements together.

- Elements:
 - (1) Blend V1.0 - a single board integrated with Arduino and BLE http://www.seeedstudio.com/depot/Blend-V10-a-single-board-integrated-with-Arduino-and-BLE-p-1918.html
 - (1) Base Shield V2 http://www.seeedstudio.com/depot/Base-Shield-V2-p-1378.html
 - (1) Grove - Dust sensor http://www.seeedstudio.com/depot/Grove-Dust-Sensor-p-1050.html
 - (1) Grove - Multichannel Gas Sensor http://www.seeedstudio.com/depot/Grove-Multichannel-Gas-Sensor-p-2502.html
 - (1) Grove - Temperature&Humidity Sensor (High-Accuracy & Mini) http://www.seeedstudio.com/depot/Grove-TemperatureHumidity-Sensor-HighAccuracy-Mini-p-1921.html
 - (1) Li-po Rider http://www.seeedstudio.com/depot/Lipo-Rider-p-710.html
 - (1) Polymer Lithium Ion Battery - 2200mAh 3.7V http://www.seeedstudio.com/depot/Polymer-Lithium-Ion-Battery-2200mAh-37V-p-1709.html?cPath=1_3

- Connect the elements together
 1. Choose on the board Your REQN and RDYN pin that will be using on BLE communicate. Notice that RDYN must be choose on a pin that can be configure as interruption on the specific arduino board. On this case is selected the 7(Int 4) in order to can use later the Int2(pin 0) and Int3(pin1) on the dust and the pin3 and pin4 for I2C communication with the digital sensors.  
 2. Plug in the Base Shield
 3. Plug in the Dust sensor on the Base Shield: With Arduino blend we are able to read also the second output of the dust sensor by connected each output as external interrupt. The digital pins usable for interrupts on Arduino blend are 0,1,3,4 and 7, the same that on Arduino Leonardo. The pins 3 and 4 is using on Arduino blend for I2C communication with the digitals sensor then the dust sensor outputs will be connected to the digital pins 0 and 1. By connected the grove seed cable from the Dust Sensor to the UART pins on the Base Shield will be read the output 1 of the senor (Low pulse occupancy for particles bigger that 1μm) on the pin 0 (Int 2) . In order to access the second output of the sensor (Low pulse occupancy for particles bigger that 2.5μm) add a extra cable to the dust sensor connector on the P2 (output2 placed between Vcc pin and GND) and connected to the pin 1 of Arduino Blend (Int3).
 4. Plug in the Temperature&Humidity TH02 digital sensor:We target to read data from 2 different digital sensor (working as slaves) that will be connected on the same I2C bus. The TH02 it should be powered down when the master controller is communicating with the other slave devices as is being described on the data-sheet. The TH02 can be powered down by setting the CS signal to logic high. The CS pin can be access from the sensor board by adding a pinhead connector on the INT hold. We will be able to control the power on and off of the device from the Arduino program by connect a cable from the CS pin to a digital pin on the shield. On this case let connect the CS to the pin 4 and of course the I2C connector to one of the specific pins on the shield.
 5. Plug in the Multichannel Gas sensor: Just connect the cable on a free I2C pins connector of the shield.
 6. Plug in the battery on the BAT pin in the Li-Po reader shield and the shield to the Arduino Blend with a micro USB and open the interrupt of the Li-Po shield to ON to power the device.

--
Instractable 2: Organicity with Arduino Edison and BLE shield
--
The same example can be load on Arduino Edision.You just will need also "Bluetooth 4.0 Low Energy-BLE Shield v2.1" in order to include BLE comunication on your arduino Edison. Load the orgBleEnvironmentalRead.iso example on your arduino Edison and connect the elements together.

- Elements:
 - (1) Bluetooth 4.0 Low Energy-BLE Shield v2.1" http://www.seeedstudio.com/depot/Bluetooth-40-Low-EnergyBLE-Shield-v21-p-1995.html?cPath=19_21
 - (1) Base Shield V2 http://www.seeedstudio.com/depot/Base-Shield-V2-p-1378.html
 - (1) Grove - Dust sensor http://www.seeedstudio.com/depot/Grove-Dust-Sensor-p-1050.html
 - (1) Grove - Multichannel Gas Sensor http://www.seeedstudio.com/depot/Grove-Multichannel-Gas-Sensor-p-2502.html
 - (1) Grove - Temperature&Humidity Sensor (High-Accuracy & Mini) http://www.seeedstudio.com/depot/Grove-TemperatureHumidity-Sensor-HighAccuracy-Mini-p-1921.html

- Connect the elements together
 1. Choose on the BLE-shiedl v2.1 your REQN and RDYN pin that will be using on BLE communicate and plug in on your arduino Edison. Notice that RDYN must be choose on a pin that can be configure as interruption on the specific arduino board. On this case is selected the 7 as RDYN, pin 0 and pin1 for the dust and the pin3 and pin4 for I2C communication with the digital sensors in order to reuse the same code than before.  
 2. Plug in the Base Shield V2 on the BLE-shiedl v2.1
 3. Plug in the Dust sensor on the Base Shield V2 as was explain on the same steap of the before Instructable. 
 4. Plug in the Temperature&Humidity TH02 digital sensor as was explain on the same steap of the before Instructable.
