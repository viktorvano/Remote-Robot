/*
 * myLibrary.c
 *
 *  Created on: Dec 20, 2020
 *      Author: vikto
 */


#include "myLibrary.h"

uint16_t distance = 0, triggerTime = 0, sensor = 0, d[5];
GPIO_TypeDef *triggerPorts[5] = {Trigger0_GPIO_Port, Trigger1_GPIO_Port, Trigger2_GPIO_Port, Trigger3_GPIO_Port, Trigger4_GPIO_Port};
uint16_t triggerPins[5] = {Trigger0_Pin, Trigger1_Pin, Trigger2_Pin, Trigger3_Pin, Trigger4_Pin};
GPIO_TypeDef *echoPorts[5] = {Echo0_GPIO_Port, Echo1_GPIO_Port, Echo2_GPIO_Port, Echo3_GPIO_Port, Echo4_GPIO_Port};
uint16_t echoPins[5] = {Echo0_Pin, Echo1_Pin, Echo2_Pin, Echo3_Pin, Echo4_Pin};
float batteryVoltage = 8.4f;
uint32_t ADC_Value = 0;
uint8_t buffer[2000];
uint16_t buffer_index = 0, timeout = 0, messageHandlerFlag = 0, netTimeout = 0;
uint8_t oneSecondFlag = 0;
float percent = 100;
uint8_t speed = 0;
uint32_t safeCounter = 0;

void HAL_ADC_ConvCpltCallback(ADC_HandleTypeDef* hadc)
{
	ADC_Value = HAL_ADC_GetValue(&hadc1);
	float currentVoltage = ((float)ADC_Value/4095.0f) * 3.3f * 3.0f * bulgarianVoltageConstant;
	batteryVoltage = 0.99f*batteryVoltage + 0.01f*currentVoltage;
	if((int)percent <= 0)
	{
		halt();
	}
}

void SysTickEnable()
{
	__disable_irq();
	SysTick->CTRL |= (SysTick_CTRL_CLKSOURCE_Msk | SysTick_CTRL_ENABLE_Msk);
	__enable_irq();
}

void SysTickDisable()
{
	__disable_irq();
	SysTick->CTRL &= ~(SysTick_CTRL_CLKSOURCE_Msk | SysTick_CTRL_ENABLE_Msk);
	__enable_irq();
}

uint16_t measureDistance(GPIO_TypeDef *triggerPort, uint16_t triggerPin, GPIO_TypeDef *echoPort, uint16_t echoPin)
{

	__HAL_UART_DISABLE_IT(&huart1, UART_IT_RXNE);
	SysTickDisable();
	//HAL_TIM_Base_Stop_IT(&htim2);//1s
	HAL_TIM_Base_Stop_IT(&htim3);//20ms
	HAL_TIM_Base_Start_IT(&htim4);//58us
	HAL_GPIO_WritePin(triggerPort, triggerPin, GPIO_PIN_SET);
	triggerTime = 0;//reset the variable
	asm ("nop");//to avoid program freezing
	while(triggerTime < TriggerDuration);
	HAL_GPIO_WritePin(triggerPort, triggerPin, GPIO_PIN_RESET);
	while(!HAL_GPIO_ReadPin(echoPort, echoPin) && oneSecondFlag == 0);//oneSecondFlag to avoid program freezing
	distance = 0;//reset the variable
	while(HAL_GPIO_ReadPin(echoPort, echoPin) && oneSecondFlag == 0);
	HAL_TIM_Base_Stop_IT(&htim4);//58us
	HAL_TIM_Base_Start_IT(&htim3);//20ms
	//HAL_TIM_Base_Start_IT(&htim2);//1s
	SysTickEnable();
	__HAL_UART_ENABLE_IT(&huart1, UART_IT_RXNE);
	return distance;
}

void ESP_RESET()
{
	HAL_GPIO_WritePin(ESP_ENABLE_GPIO_Port, ESP_ENABLE_Pin, GPIO_PIN_RESET);
	HAL_GPIO_WritePin(ESP_Reset_GPIO_Port, ESP_Reset_Pin, GPIO_PIN_RESET);
	HAL_Delay(30);
	HAL_GPIO_WritePin(ESP_ENABLE_GPIO_Port, ESP_ENABLE_Pin, GPIO_PIN_SET);
	HAL_GPIO_WritePin(ESP_Reset_GPIO_Port, ESP_Reset_Pin, GPIO_PIN_SET);
}

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

void ESP_Clear_Buffer()
{
	memset(buffer, 0, 2000);
	buffer_index = 0;
}

void calculateBattery()
{
	percent = ((batteryVoltage-6.0f) / 2.35f)*100.0f;
	if(percent > 100.0f)
		percent = 100.0f;
}

void updateScreen()
{
	char string[100];

	ssd1306_Fill(Black);
	ssd1306_SetCursor(0, 0);
	sprintf(string, "%d%% %.3fV", (int)percent, batteryVoltage);
	ssd1306_WriteString(string, Font_11x18, White);

	ssd1306_SetCursor(0, 20);
	sprintf(string, "%d", d[4]);
	ssd1306_WriteString(string, Font_11x18, White);

	ssd1306_SetCursor(50, 20);
	sprintf(string, "%d", d[2]);
	ssd1306_WriteString(string, Font_11x18, White);

	ssd1306_SetCursor(100, 20);
	sprintf(string, "%d", d[0]);
	ssd1306_WriteString(string, Font_11x18, White);

	ssd1306_SetCursor(20, 40);
	sprintf(string, "%d", d[3]);
	ssd1306_WriteString(string, Font_11x18, White);
	ssd1306_UpdateScreen();

	ssd1306_SetCursor(80, 40);
	sprintf(string, "%d", d[1]);
	ssd1306_WriteString(string, Font_11x18, White);
	ssd1306_UpdateScreen();
}

void measureDistances()
{
	for(uint8_t i=0; i<5 && oneSecondFlag == 0; i++)//if oneSecondFlag sets to 1 measurement stops
	{
		sensor = i;
		d[i] = measureDistance(triggerPorts[i], triggerPins[i], echoPorts[i], echoPins[i]);
	}
}

uint8_t string_compare(char array1[], char array2[], uint16_t length)
{
	 uint16_t comVAR=0, i;
	 for(i=0;i<length;i++)
	   	{
	   		  if(array1[i]==array2[i])
	   	  		  comVAR++;
	   	  	  else comVAR=0;
	   	}
	 if (comVAR==length)
		 	return 1;
	 else 	return 0;
}

int string_contains(char bufferArray[], char searchedString[], uint16_t length)
{
	uint8_t result=0;
	for(uint16_t i=0; i<length; i++)
	{
		result = string_compare(&bufferArray[i], &searchedString[0], strlen(searchedString));
		if(result == 1)
			return i;
	}
	return -1;
}

void messageHandler()
{
	__HAL_UART_DISABLE_IT(&huart1, UART_IT_RXNE);
	int position = 0;
	if((position = string_contains((char*)buffer, "GET", buffer_index)) != -1)
	{
		sendData();
	}else if((position = string_contains((char*)buffer, "F-", buffer_index)) != -1)
	{
		setSpeed(position);
		HAL_GPIO_WritePin(Motor_A_GPIO_Port, Motor_A_Pin, 1);
		HAL_GPIO_WritePin(Motor_B_GPIO_Port, Motor_B_Pin, 0);
		HAL_GPIO_WritePin(Steering_A_GPIO_Port, Steering_A_Pin, 0);
		HAL_GPIO_WritePin(Steering_B_GPIO_Port, Steering_B_Pin, 0);
		if(buffer[position+2] == 'T')
			drivingAssistance();
	}else if((position = string_contains((char*)buffer, "B-", buffer_index)) != -1)
	{
		setSpeed(position);
		HAL_GPIO_WritePin(Motor_A_GPIO_Port, Motor_A_Pin, 0);
		HAL_GPIO_WritePin(Motor_B_GPIO_Port, Motor_B_Pin, 1);
		HAL_GPIO_WritePin(Steering_A_GPIO_Port, Steering_A_Pin, 0);
		HAL_GPIO_WritePin(Steering_B_GPIO_Port, Steering_B_Pin, 0);
		if(buffer[position+2] == 'T')
			drivingAssistance();
	}else if((position = string_contains((char*)buffer, "-R", buffer_index)) != -1)
	{
		setSpeed(position);
		HAL_GPIO_WritePin(Motor_A_GPIO_Port, Motor_A_Pin, 0);
		HAL_GPIO_WritePin(Motor_B_GPIO_Port, Motor_B_Pin, 0);
		HAL_GPIO_WritePin(Steering_A_GPIO_Port, Steering_A_Pin, 1);
		HAL_GPIO_WritePin(Steering_B_GPIO_Port, Steering_B_Pin, 0);
		if(buffer[position+2] == 'T')
			drivingAssistance();
	}else if((position = string_contains((char*)buffer, "-L", buffer_index)) != -1)
	{
		setSpeed(position);
		HAL_GPIO_WritePin(Motor_A_GPIO_Port, Motor_A_Pin, 0);
		HAL_GPIO_WritePin(Motor_B_GPIO_Port, Motor_B_Pin, 0);
		HAL_GPIO_WritePin(Steering_A_GPIO_Port, Steering_A_Pin, 0);
		HAL_GPIO_WritePin(Steering_B_GPIO_Port, Steering_B_Pin, 1);
		if(buffer[position+2] == 'T')
			drivingAssistance();
	}else if((position = string_contains((char*)buffer, "FR", buffer_index)) != -1)
	{
		setSpeed(position);
		HAL_GPIO_WritePin(Motor_A_GPIO_Port, Motor_A_Pin, 1);
		HAL_GPIO_WritePin(Motor_B_GPIO_Port, Motor_B_Pin, 0);
		HAL_GPIO_WritePin(Steering_A_GPIO_Port, Steering_A_Pin, 1);
		HAL_GPIO_WritePin(Steering_B_GPIO_Port, Steering_B_Pin, 0);
		if(buffer[position+2] == 'T')
			drivingAssistance();
	}else if((position = string_contains((char*)buffer, "FL", buffer_index)) != -1)
	{
		setSpeed(position);
		HAL_GPIO_WritePin(Motor_A_GPIO_Port, Motor_A_Pin, 1);
		HAL_GPIO_WritePin(Motor_B_GPIO_Port, Motor_B_Pin, 0);
		HAL_GPIO_WritePin(Steering_A_GPIO_Port, Steering_A_Pin, 0);
		HAL_GPIO_WritePin(Steering_B_GPIO_Port, Steering_B_Pin, 1);
		if(buffer[position+2] == 'T')
			drivingAssistance();
	}else if((position = string_contains((char*)buffer, "BR", buffer_index)) != -1)
	{
		setSpeed(position);
		HAL_GPIO_WritePin(Motor_A_GPIO_Port, Motor_A_Pin, 0);
		HAL_GPIO_WritePin(Motor_B_GPIO_Port, Motor_B_Pin, 1);
		HAL_GPIO_WritePin(Steering_A_GPIO_Port, Steering_A_Pin, 1);
		HAL_GPIO_WritePin(Steering_B_GPIO_Port, Steering_B_Pin, 0);
		if(buffer[position+2] == 'T')
			drivingAssistance();
	}else if((position = string_contains((char*)buffer, "BL", buffer_index)) != -1)
	{
		setSpeed(position);
		HAL_GPIO_WritePin(Motor_A_GPIO_Port, Motor_A_Pin, 0);
		HAL_GPIO_WritePin(Motor_B_GPIO_Port, Motor_B_Pin, 1);
		HAL_GPIO_WritePin(Steering_A_GPIO_Port, Steering_A_Pin, 0);
		HAL_GPIO_WritePin(Steering_B_GPIO_Port, Steering_B_Pin, 1);
		if(buffer[position+2] == 'T')
			drivingAssistance();
	}else if((position = string_contains((char*)buffer, "--", buffer_index)) != -1)
	{
		setSpeed(position);
		HAL_GPIO_WritePin(Motor_A_GPIO_Port, Motor_A_Pin, 0);
		HAL_GPIO_WritePin(Motor_B_GPIO_Port, Motor_B_Pin, 0);
		HAL_GPIO_WritePin(Steering_A_GPIO_Port, Steering_A_Pin, 0);
		HAL_GPIO_WritePin(Steering_B_GPIO_Port, Steering_B_Pin, 0);
		if(buffer[position+2] == 'T')
			drivingAssistance();
	}else if(string_contains((char*)buffer, "+CWJAP:", buffer_index) != -1
			&& (string_contains((char*)buffer, "FAIL", buffer_index) != -1
			|| string_contains((char*)buffer, "DISCONNECT", buffer_index) != -1))
	{
		//Change your WiFi SSID credentials below
		HAL_UART_Transmit(&huart1, (uint8_t*)"AT+CWJAP=\"WiFiSSID\",\"WiFiPASSWORD\"\r\n", strlen("AT+CWJAP=\"WiFiSSID\",\"WiFiPASSWORD\"\r\n"), 100);
	}
	ESP_Clear_Buffer();
	__HAL_UART_ENABLE_IT(&huart1, UART_IT_RXNE);
}

void sendData()
{
	char outputString[100], cipsend[50], response[300];
	memset(outputString, 0, 100);
	memset(cipsend, 0, 50);
	memset(response, 0, 300);

	sprintf(outputString, "Battery: %i%%\nDistances: %i,%i,%i,%i,%i\n", (int)percent, d[0], d[1], d[2], d[3], d[4]);
	sprintf(response, "HTTP/1.1 200 OK\r\nContent-Length: %i\r\nContent-Type: text/plain\r\n\r\n%s", strlen(outputString), outputString);
	sprintf(cipsend, "AT+CIPSEND=0,%i\r\n", strlen(response));

	HAL_UART_Transmit(&huart1, (uint8_t*)cipsend, strlen(cipsend), 100);
	HAL_Delay(50);
	HAL_UART_Transmit(&huart1, (uint8_t*)response, strlen(response), 100);
	HAL_Delay(50);
	HAL_UART_Transmit(&huart1, (uint8_t*)"AT+CIPCLOSE=0\r\n", strlen("AT+CIPCLOSE=0\r\n"), 100);
}

void setSpeed(int position)
{
	speed = atoi((char*)&buffer[position + 3]);
	__HAL_TIM_SET_COMPARE(&htim3, Motor_PWM_Channel, speed);
}

void halt()
{
	HAL_ADC_Stop_IT(&hadc1);
	HAL_TIM_Base_Stop_IT(&htim3);//20ms
	HAL_TIM_Base_Stop_IT(&htim4);//58us
	__HAL_UART_DISABLE_IT(&huart1, UART_IT_RXNE);
	stopMotors();
	while(1);
}

void stopMotors()
{
	__HAL_TIM_SET_COMPARE(&htim3, Steering_PWM_Channel, 255);
	__HAL_TIM_SET_COMPARE(&htim3, Motor_PWM_Channel, 255);
	HAL_GPIO_WritePin(Motor_A_GPIO_Port, Motor_A_Pin, 0);
	HAL_GPIO_WritePin(Motor_B_GPIO_Port, Motor_B_Pin, 0);
	HAL_GPIO_WritePin(Steering_A_GPIO_Port, Steering_A_Pin, 0);
	HAL_GPIO_WritePin(Steering_B_GPIO_Port, Steering_B_Pin, 0);
}

void drivingAssistance()
{
	float factor = speed/255.0f;
	if(d[1] < (100.0f * factor)
	|| d[2] < (100.0f * factor)
	|| d[3] < (100.0f * factor))
	{
		HAL_GPIO_WritePin(Motor_A_GPIO_Port, Motor_A_Pin, 0);
		HAL_GPIO_WritePin(Motor_B_GPIO_Port, Motor_B_Pin, 1);
	}
}

uint32_t HAL_GetTick(void)
{
	if(timeout == 1)
		safeCounter++;
	if(safeCounter > 100000)
	{
		safeCounter = 0;
		/*uwTick += 50;
		stopMotors();
		SysTickDisable();
		HAL_TIM_Base_Stop_IT(&htim4);//58us
		HAL_TIM_Base_Start_IT(&htim3);//20ms
		SysTickEnable();
		__HAL_UART_ENABLE_IT(&huart1, UART_IT_RXNE);*/
		NVIC_SystemReset();
		return uwTick;
	}
  return uwTick;
}
