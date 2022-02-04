# Remote-Robot
 Remote robot using STM32, ESP8266 and Android  
 Video Tutorial: https://www.youtube.com/watch?v=vzHLPQ25O1Q  
   
## Photos

![alt text](https://github.com/viktorvano/Remote-Robot/blob/main/Documents/car.jpg?raw=true)  
  
## Java Desktop App  
- Use WASD to move around  
- QWERTY digits (1,2,3...,9,0) to set the speed (power) from 10% (1) to 100% (0)
- R to toggle Driving Assintance
- L to toggle LED Lights
![alt text](https://github.com/viktorvano/Remote-Robot/blob/main/Documents/app_screenshot.png?raw=true)  
     
   
 ## Wiring Diagram
![alt text](https://github.com/viktorvano/Remote-Robot/blob/main/Documents/schematics.png?raw=true)  
  
STM32 Pinout:  
![alt text](https://github.com/viktorvano/Remote-Robot/blob/main/Documents/STM32F411CEU6.png?raw=true)  
  
  
## Code Snippets
  
[STM32 C code] You have to change your WiFi SSID credentials in "myLibrary.h":  
```C
#define WiFi_Credentials	"AT+CWJAP=\"WiFiSSID\",\"WiFiPASSWORD\"\r\n"
```  
[STM32 C code] Calculating the battery percentage:
- You have to list all voltege regulators and components that are connected directly to the battery
- Find out their lowest voltage needed to run (or regulate) properly
- The highest low voltage will be a 0% of the battery for you (in this case minVoltage = 6.0V)
- The 2 cell Lithium battery has voltage range 6.0V to 8.4V (voltageRange = 2.4V)
- Estimate the voltage drop due to current load (voltageDrop = 0.05V)
- Formula: percentage = ((batteryVoltage - minVoltage) / (voltageRange - voltageDrop))*100.0f;
```C
void calculateBattery()
{
	percent = ((batteryVoltage-6.0f) / 2.35f)*100.0f;
	if(percent > 100.0f)
		percent = 100.0f;
}
```
  
[Java Desktop App] Also you have to change the IP addresses in "IP_STM32.txt" and "AndroidIP.txt" files,   
which is automatically generated if does not exist and the JAR app is launched.
  
## DDNS  
  
Use a DDNS service such as No-IP to create a hostname
![alt text](https://github.com/viktorvano/STM32-IoT-Humdity-Sensor/blob/main/documents/noip.png?raw=true)   
  
Add DDNS credentials to your router  
![alt text](https://github.com/viktorvano/STM32-IoT-Humdity-Sensor/blob/main/documents/DDNS.png?raw=true)   
  
Reserve IP address of your IoT device (make it static, so it won't change over time)  
![alt text](https://github.com/viktorvano/STM32-IoT-Humdity-Sensor/blob/main/documents/static-device-IP.png?raw=true)   
  
Setup a port forwarding rules for STM32 (ESP8266) and Android too.  
![alt text](https://github.com/viktorvano/STM32-IoT-Humdity-Sensor/blob/main/documents/port-forwarding.png?raw=true)   
  
Now you can access the robot from anywhere.  
  
##### Ultrasonic Sensors repository
https://github.com/viktorvano/STM32F103C8T6_HC-SR04
