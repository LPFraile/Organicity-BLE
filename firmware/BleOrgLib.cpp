/*"name": "BleOrgLib.cpp",
Author: "LPFraile <lidiapf0@gmail.com>",
License: "BSD",
Version: "0.0.1",
Description: "Connect your Blend device with Organicity"
File: source file
*/

#include "BleOrgLib.h"
unsigned char buf[16]={0};
static byte buf_len=0;
int LED = 13;

BleOrgLib::BleOrgLib()
{
    index=0;

}

void BleOrgLib::orgFunctions(){
   sensorSetup();
   sensorLoop();  
   sensorReset();
}

void BleOrgLib::orgSetup()
{
Serial.println("power on library!");	
 
 pinMode(LED, OUTPUT);
 digitalWrite(LED, HIGH);
 ble_set_name(device_name); 		
 sensorSetup();
 
  index=0;
  // Init. and start BLE library.
  ble_begin();
}

void BleOrgLib::orgRun()
{ 
  
  if (ble_connected() )
  { 
	ble_write('G');
    	bool follow=true;  
    	double  timer=millis();
      
    	while(ble_connected()&&follow)
	{   
       		uint8_t temp;
       		Serial.println("ble conected");
       		double timer2=millis();
       		bool sending=true; 
      		while (ble_available()&&sending)
      		{
        		digitalWrite(LED, LOW);  
        		temp = ble_read();
        		if('V' == temp)
        		{
          			temp = ble_read();
          			if('n' == temp){
            
           				Serial.println("ReceiveVn");
          				Serial.print("Index");
              				Serial.println(index);
               				SendVariables();
            			}   
        		 } 
        		if ((millis()-timer2)>30000)
         		{
				Serial.println("stack sending sensor value disconnect for reset the conexion");
				delay (5000); 
				ble_disconnect(); 
				delay(150);
				sending=false;
				follow=false;  
				digitalWrite(LED, HIGH); 
        		 }
       		}
        	ble_do_events();
        	delay(150);
	       if ((millis()-timer)>30000)
	       {
			  Serial.println("stack waiting for a message disconnect for reset the conexion ");
			  delay (10000);
			  ble_disconnect(); 
			  delay(150);
			  follow=false;   
	       }
   	 }   
  }
  else //very importan read only when is not blouthout conection to not stop the protocol comuniation
  {
   	sensorLoop();
   }
ble_do_events();	
}

void BleOrgLib::SendVariables()
{
  
    noInterrupts();

    delay(100);  //jitters elimination
    
    if (index<num_var)
    {
	Serial.print("Value:");
	Serial.print(variables_name[index]);
      	Serial.println(variables_value[index]);
      	SendfFloatVariable(variables_name[index],variables_value[index]);
        index++;
    }
    else if(index==num_var)
    {
      index=0;
      delay(150);
      digitalWrite(LED, HIGH);
      delay(150);
      ble_disconnect();
      sensorReset();	
    }
   
    interrupts();
}
void BleOrgLib::ble_write_string(byte *bytes, uint8_t len)
{
  if (buf_len + len > 20)
  {
    for (int j = 0; j < 15000; j++)
      ble_do_events();
    
    buf_len = 0;
  } 
  for (int j = 0; j < len; j++)
  {
    ble_write(bytes[j]);
    buf_len++;
  }   
  if (buf_len == 20)
  {
    for (int j = 0; j < 15000; j++)
      ble_do_events();
    
    buf_len = 0;
  }  
}

void BleOrgLib::SendfFloatVariable(String name,float floatVar)
{

  buf[0]='V';
  byte * b = (byte *) &floatVar;
  int i=0;
  int n= name.length();
  buf[1]=n;
  Serial.print("name:");
  Serial.println(name);

  for (i=0;i<n;i++)
  {
    buf[i+2]=name[i];
  }

  buf[n+2] =b[3]; 
  buf[n+3]= b[2];
  buf[n+4]= b[1];
  buf[n+5]= b[0];

  Serial.print("Index on Send function");
  Serial.println(index);

  ble_write_string(buf,n+6);
  buf_len = 0;

}

