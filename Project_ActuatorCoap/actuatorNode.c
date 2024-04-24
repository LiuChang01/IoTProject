#include "contiki.h"
#include <stdio.h>
#include <stdlib.h>
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "sys/log.h"
#include <string.h>
#include "dev/leds.h"
#include "node-id.h"
#include "net/ipv6/simple-udp.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-ds6.h"
#include "net/ipv6/uip-debug.h"
#include "routing/routing.h"
#define LOG_MODULE "RPL_BR"
#define LOG_LEVEL LOG_LEVEL_INFO

#define SERVER_EP "coap://[fd00::1]:5683"

extern coap_resource_t res_actuator;
char *service_url="/registration";
static bool registered=false;
static struct etimer connectivity_timer,registration_timer;
static bool is_connected(){
	if(NETSTACK_ROUTING.node_is_reachable()){
		LOG_INFO("border router is reachable\n");
		return true;
	}else{
		LOG_INFO("router not reachable\n");
		return false;		
	}
}
void client_chunk_handler(coap_message_t *response){
  const uint8_t *chunk;
  int len;
  if(response==NULL){
    LOG_INFO("req timeout\n");
    etimer_set(&registration_timer,CLOCK_SECOND*5);
    return;
  }
  len =coap_get_payload(response,&chunk);
  if(strncmp((char*)chunk,"Success",len)==0){
    registered=true;
    LOG_INFO("device registred\n");
  }else{
    etimer_set(&registration_timer,CLOCK_SECOND*5);
  }

}
PROCESS(actuatorNode,"Actuator");
AUTOSTART_PROCESSES(&actuatorNode);
PROCESS_THREAD(actuatorNode,ev,data){
  PROCESS_BEGIN();
  static coap_endpoint_t server_ep;
  static coap_message_t request[1];
  PROCESS_PAUSE();
  LOG_INFO("Activation of resource\n");
  coap_activate_resource(&res_actuator,"WaterSpurt");
  LOG_INFO("Resource activated\n");
  etimer_set(&connectivity_timer,CLOCK_SECOND);
  PROCESS_WAIT_UNTIL(etimer_expired(&connectivity_timer));
  while(!is_connected()){
      etimer_reset(&connectivity_timer);
      PROCESS_WAIT_UNTIL(etimer_expired(&connectivity_timer));
  }
  while(!registered){
    LOG_INFO("send registration msg\n");
    coap_endpoint_parse(SERVER_EP,strlen(SERVER_EP),&server_ep);
    coap_init_message(request,COAP_TYPE_CON,COAP_POST,0);
    coap_set_header_uri_path(request,service_url);
    coap_set_payload(request,(uint8_t*)"registration",sizeof("registration")-1);
    COAP_BLOCKING_REQUEST(&server_ep,request,client_chunk_handler);
    PROCESS_WAIT_UNTIL(etimer_expired(&registration_timer));
  }
  
  PROCESS_END();
}
