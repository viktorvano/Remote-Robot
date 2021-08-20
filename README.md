# Remote-Robot
 Remote robot using STM32, ESP8266 and Android  
 Video Tutorial: https://www.youtube.com/watch?v=vzHLPQ25O1Q  
   
## Photos

![alt text](https://github.com/viktorvano/Remote-Robot/blob/main/Documents/IMG_20210620_173554_845.jpg?raw=true)  
  
## Java Desktop App  
- Use WASD to move around  
- QWERTY digits (1,2,3...,9,0) to set the speed (power) from 10% (1) to 100% (0)
- R to toggle Driving Assintance
![alt text](https://github.com/viktorvano/Remote-Robot/blob/main/Documents/app_screenshot.png?raw=true)  
     
   
 ## Wiring Diagram
![alt text](https://github.com/viktorvano/Remote-Robot/blob/main/Documents/schematics.png?raw=true)  
  
STM32 Pinout:  
![alt text](https://github.com/viktorvano/Remote-Robot/blob/main/Documents/STM32F411CEU6.png?raw=true)  
  
  
## Code Snippets
  
[STM32 C code] You have to change your WiFi SSID credentials:  
```C
void ESP_Server_Init()
{
	ESP_RESET();
	HAL_Delay(2000);
	ESP_Clear_Buffer();

	HAL_UART_Transmit(&huart1, (uint8_t*)"AT+RST\r\n", strlen("AT+RST\r\n"), 100);
	HAL_Delay(1500);
	ESP_Clear_Buffer();

	HAL_UART_Transmit(&huart1, (uint8_t*)"AT+CWMODE=1\r\n", strlen("AT+CWMODE=1\r\n"), 100);
	HAL_Delay(2000);
	ESP_Clear_Buffer();

	HAL_UART_Transmit(&huart1, (uint8_t*)"AT+CWDHCP=1,1\r\n", strlen("AT+CWDHCP=1,1\r\n"), 100);
	HAL_Delay(2000);
	ESP_Clear_Buffer();

	HAL_UART_Transmit(&huart1, (uint8_t*)"AT+CIPMUX=1\r\n", strlen("AT+CIPMUX=1\r\n"), 100);
	HAL_Delay(2000);
	ESP_Clear_Buffer();

	HAL_UART_Transmit(&huart1, (uint8_t*)"AT+CIPSERVER=1,80\r\n", strlen("AT+CIPSERVER=1,80\r\n"), 100);
	HAL_Delay(2000);
	ESP_Clear_Buffer();

 //Change your WiFi SSID credentials below
	HAL_UART_Transmit(&huart1, (uint8_t*)"AT+CWJAP=\"WiFiSSID\",\"WiFiPASSWORD\"\r\n", strlen("AT+CWJAP=\"WiFiSSID\",\"WiFiPASSWORD\"\r\n"), 100);
}

//and also here:
void messageHandler()
{
	...else if(string_contains((char*)buffer, "+CWJAP:", buffer_index) != -1
			&& (string_contains((char*)buffer, "FAIL", buffer_index) != -1
			|| string_contains((char*)buffer, "DISCONNECT", buffer_index) != -1))
	{
		//Change your WiFi SSID credentials below
		HAL_UART_Transmit(&huart1, (uint8_t*)"AT+CWJAP=\"WiFiSSID\",\"WiFiPASSWORD\"\r\n", strlen("AT+CWJAP=\"WiFiSSID\",\"WiFiPASSWORD\"\r\n"), 100);
	}
	ESP_Clear_Buffer();
	__HAL_UART_ENABLE_IT(&huart1, UART_IT_RXNE);
}

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
  
[Java Desktop App] Also you have to change the IP address in "IP_STM32.txt" file,   
which is automatically generated if does not exist and the JAR app is launched.  
The default IP address in the file is defined here stringSTM32IP = "192.168.2.91"; in the Variables class.  
If the file exist, the IP address is loaded from the file after the app is launched.  
```Java
public class Variables {
    public static String stringSTM32IP = "192.168.2.91";
    public static int stm32StatusUpdatePeriod = 1000;
    public static double distanceProgressRange = 200.0;
}
```
  
##### Ultrasonic Sensors repository
https://github.com/viktorvano/STM32F103C8T6_HC-SR04
