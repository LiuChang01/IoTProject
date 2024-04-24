#include "contiki.h"
#include <stdio.h>
#include <stdlib.h>
#include "coap-engine.h"
#include "sys/log.h"
#include <string.h>
#include "os/dev/leds.h"
#define LOG_MODULE "ACTUATOR"
#define LOG_LEVEL LOG_LEVEL_INFO

static bool actuating=false;

static void res_put_handler(coap_message_t *request,coap_message_t *response,uint8_t *buffer,uint16_t preferred_size,int32_t *offset);
RESOURCE(res_actuator,
        "title=Actuator for spurt the water on the camp put mode=on|off",
        NULL,
        NULL,
        res_put_handler,
        NULL);
static void res_put_handler(coap_message_t *request,coap_message_t *response,uint8_t *buffer,uint16_t preferred_size,int32_t *offset){
    if(request!=NULL){
        LOG_INFO("put request received\n");
    }
    LOG_INFO("Inside the put handler\n");
    size_t len=0;
    const char *text=NULL;
    char mode[4];
    static int mode_success=1;
	memset(mode, 0, 4);
    len =coap_get_post_variable(request,"mode",&text);
    if(len > 0 && len < 4) {
		memcpy(mode, text, len);
		if(strncmp(mode, "on", len) == 0) {
			actuating = true;
			leds_on(LEDS_ALL);
			LOG_INFO("WaterSpurt ON\n");
		} else if(strncmp(mode, "off", len) == 0) {
			actuating = false;
			leds_off(LEDS_ALL);
			LOG_INFO("WaterSpurt OFF\n");
		} else {
			mode_success = 0;
		}
	} else {
		mode_success = 0;
	}
    if(!mode_success){
        coap_set_status_code(response,BAD_REQUEST_4_00);
    }
    
}

