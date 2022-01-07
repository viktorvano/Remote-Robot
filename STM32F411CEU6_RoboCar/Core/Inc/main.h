/* USER CODE BEGIN Header */
/**
  ******************************************************************************
  * @file           : main.h
  * @brief          : Header for main.c file.
  *                   This file contains the common defines of the application.
  ******************************************************************************
  * @attention
  *
  * <h2><center>&copy; Copyright (c) 2021 STMicroelectronics.
  * All rights reserved.</center></h2>
  *
  * This software component is licensed by ST under BSD 3-Clause license,
  * the "License"; You may not use this file except in compliance with the
  * License. You may obtain a copy of the License at:
  *                        opensource.org/licenses/BSD-3-Clause
  *
  ******************************************************************************
  */
/* USER CODE END Header */

/* Define to prevent recursive inclusion -------------------------------------*/
#ifndef __MAIN_H
#define __MAIN_H

#ifdef __cplusplus
extern "C" {
#endif

/* Includes ------------------------------------------------------------------*/
#include "stm32f4xx_hal.h"

/* Private includes ----------------------------------------------------------*/
/* USER CODE BEGIN Includes */

/* USER CODE END Includes */

/* Exported types ------------------------------------------------------------*/
/* USER CODE BEGIN ET */

/* USER CODE END ET */

/* Exported constants --------------------------------------------------------*/
/* USER CODE BEGIN EC */

/* USER CODE END EC */

/* Exported macro ------------------------------------------------------------*/
/* USER CODE BEGIN EM */

/* USER CODE END EM */

void HAL_TIM_MspPostInit(TIM_HandleTypeDef *htim);

/* Exported functions prototypes ---------------------------------------------*/
void Error_Handler(void);

/* USER CODE BEGIN EFP */

/* USER CODE END EFP */

/* Private defines -----------------------------------------------------------*/
#define LED_Pin GPIO_PIN_13
#define LED_GPIO_Port GPIOC
#define Trigger0_Pin GPIO_PIN_0
#define Trigger0_GPIO_Port GPIOA
#define Echo0_Pin GPIO_PIN_1
#define Echo0_GPIO_Port GPIOA
#define Trigger1_Pin GPIO_PIN_2
#define Trigger1_GPIO_Port GPIOA
#define Echo1_Pin GPIO_PIN_3
#define Echo1_GPIO_Port GPIOA
#define Trigger2_Pin GPIO_PIN_4
#define Trigger2_GPIO_Port GPIOA
#define Echo2_Pin GPIO_PIN_5
#define Echo2_GPIO_Port GPIOA
#define Trigger3_Pin GPIO_PIN_6
#define Trigger3_GPIO_Port GPIOA
#define Echo3_Pin GPIO_PIN_7
#define Echo3_GPIO_Port GPIOA
#define Trigger4_Pin GPIO_PIN_2
#define Trigger4_GPIO_Port GPIOB
#define Echo4_Pin GPIO_PIN_10
#define Echo4_GPIO_Port GPIOB
#define OLED_DC_Pin GPIO_PIN_12
#define OLED_DC_GPIO_Port GPIOB
#define OLED_CS_Pin GPIO_PIN_14
#define OLED_CS_GPIO_Port GPIOB
#define OLED_RESET_Pin GPIO_PIN_8
#define OLED_RESET_GPIO_Port GPIOA
#define ESP_Reset_Pin GPIO_PIN_11
#define ESP_Reset_GPIO_Port GPIOA
#define ESP_ENABLE_Pin GPIO_PIN_12
#define ESP_ENABLE_GPIO_Port GPIOA
#define Steering_B_Pin GPIO_PIN_15
#define Steering_B_GPIO_Port GPIOA
#define Steering_A_Pin GPIO_PIN_3
#define Steering_A_GPIO_Port GPIOB
#define Steering_PWM_Pin GPIO_PIN_4
#define Steering_PWM_GPIO_Port GPIOB
#define Motor_PWM_Pin GPIO_PIN_5
#define Motor_PWM_GPIO_Port GPIOB
#define Motor_B_Pin GPIO_PIN_6
#define Motor_B_GPIO_Port GPIOB
#define Motor_A_Pin GPIO_PIN_7
#define Motor_A_GPIO_Port GPIOB
#define LIGHTS_Pin GPIO_PIN_9
#define LIGHTS_GPIO_Port GPIOB
/* USER CODE BEGIN Private defines */

/* USER CODE END Private defines */

#ifdef __cplusplus
}
#endif

#endif /* __MAIN_H */
