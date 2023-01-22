/*
 * myLibrary.h
 *
 *  Created on: Dec 20, 2020
 *      Author: vikto
 */

#ifndef INC_MYLIBRARY_H_
#define INC_MYLIBRARY_H_

#define bulgarianVoltageConstant 1.0019f

#include "main.h"
#include <stdio.h>
#include <string.h>
#include "ssd1306.h"
#include "ssd1306_tests.h"
#include <stdlib.h>

extern ADC_HandleTypeDef hadc1;
extern SPI_HandleTypeDef hspi2;
extern TIM_HandleTypeDef htim2;
extern TIM_HandleTypeDef htim3;
extern TIM_HandleTypeDef htim4;
extern UART_HandleTypeDef huart1;

//Change your WiFi credentials
#define WiFi_Credentials	"AT+CWJAP=\"WiFiSSID\",\"WiFiPASSWORD\"\r\n"

#define TriggerDuration 2
#define Steering_PWM_Channel	TIM_CHANNEL_1
#define Motor_PWM_Channel		TIM_CHANNEL_2

extern uint16_t distance, triggerTime, sensor, d[5];
extern GPIO_TypeDef *triggerPorts[5];
extern uint16_t triggerPins[5];
extern GPIO_TypeDef *echoPorts[5];
extern uint16_t echoPins[5];
extern float batteryVoltage;
extern uint32_t ADC_Value;
extern uint8_t buffer[2000];
extern uint16_t buffer_index, timeout, messageHandlerFlag, netTimeout;
extern uint8_t oneSecondFlag;
extern float percent;
extern uint8_t speed;
extern uint32_t safeCounter;

void HAL_ADC_ConvCpltCallback(ADC_HandleTypeDef* hadc);
void SysTickEnable();
void SysTickDisable();
uint16_t measureDistance(GPIO_TypeDef *triggerPort, uint16_t triggerPin, GPIO_TypeDef *echoPort, uint16_t echoPin);

void ESP_RESET();
void ESP_Server_Init();
void ESP_Clear_Buffer();
void calculateBattery();
void updateScreen();
void measureDistances();
uint8_t string_compare(char array1[], char array2[], uint16_t length);
int string_contains(char bufferArray[], char searchedString[], uint16_t length);
void messageHandler();
void sendData();
void setSpeed(int position);
void halt();
void stopMotors();
void drivingAssistance();
void lights(GPIO_PinState status);
uint32_t HAL_GetTick(void);

#endif /* INC_MYLIBRARY_H_ */
