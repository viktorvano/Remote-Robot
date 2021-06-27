# Remote-Robot
 Remote robot using STM32 and Android  
   
## Photos

![alt text](https://github.com/viktorvano/Remote-Robot/blob/main/Documents/IMG_20210620_173554_845.jpg?raw=true)  
  
Use WASD to move around, QWERTY digits (1,2,3...,9,0) to set the speed (power) from 10% (1) to 100% (0) and R to toggle Driving Assintance.
![alt text](https://github.com/viktorvano/Remote-Robot/blob/main/Documents/app_screenshot.png?raw=true)  
     
   
 ## Wiring Diagram
![alt text](https://github.com/viktorvano/Remote-Robot/blob/main/Documents/schematics.png?raw=true)  
  
![alt text](https://github.com/viktorvano/Remote-Robot/blob/main/Documents/STM32F411CEU6.png?raw=true)  
  
  
## Code Snippets
  
You have to change your WiFi SSID credentials:  
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
  
Also you have to change stringSTM32IP:  
```Java
public class Variables {
    public static String stringSTM32IP = "192.168.2.90";
    public static int stm32StatusUpdatePeriod = 1000;
    public static double distanceProgressRange = 200.0;
}
```
