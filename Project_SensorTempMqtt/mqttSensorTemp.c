#include "contiki.h"
#include "net/routing/routing.h"
#include "mqtt.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-icmp6.h"
#include "net/ipv6/sicslowpan.h"
#include "sys/etimer.h"
#include "sys/ctimer.h"
#include "lib/sensors.h"
#include "dev/button-hal.h"
#include "dev/leds.h"
#include "os/sys/log.h"
#include "mqtt-client.h"
#include <sys/node-id.h>
#include <time.h>
#include <string.h>
#include <strings.h>
#define LOG_MODULE "mqtt-sensor-temperature"
#ifdef MQTT_CLIENT_CONF_LOG_LEVEL
#define LOG_LEVEL MQTT_CLIENT_CONF_LOG_LEVEL
#else
#define LOG_LEVEL LOG_LEVEL_DBG
#endif
#define MQTT_CLIENT_BROKER_IP_ADDR "fd00::1"
static const char *broker_ip=MQTT_CLIENT_BROKER_IP_ADDR;

#define DEFAULT_BROKER_PORT 1883
#define DEFAULT_PUBLISH_INTERVAL (30*CLOCK_SECOND)
#define PUBLISH_INTERVAL (5*CLOCK_SECOND)


static uint8_t state;
#define STATE_INIT 0
#define STATE_NET_OK 1
#define STATE_CONNECTING 2
#define STATE_CONNECTED 3
#define STATE_SUBSCRIBED 4
#define STATE_DISCONNECTED 5
PROCESS_NAME(mqtt_sensor_temperature);
AUTOSTART_PROCESSES(&mqtt_sensor_temperature);
#define MAX_TCP_SEGMENT_SIZE 32
#define CONFIG_IP_ADDR_STR_LEN 64
#define BUFFER_SIZE 64
static char sensor_id[BUFFER_SIZE];
static char pub_topic[BUFFER_SIZE];
static char sub_topic[BUFFER_SIZE];
#define STATE_MACHINE_PERIOD (CLOCK_SECOND>>1)
static struct etimer periodic_timer;

#define APP_BUFFER_SIZE 512
static char app_buffer[APP_BUFFER_SIZE];
static struct mqtt_message *msg_ptr=0;
static struct mqtt_connection conn;
PROCESS(mqtt_sensor_temperature,"MQTT Sensor for temperature");
static bool increasing_temperature=false;
static bool descreasing_temperature=false;
static int temperature=50;
static int variation=0;

static void pub_handler(const char *topic,uint16_t topic_len,const uint8_t *chunk,uint16_t chunk_len){
  printf("Pub Handler: topic='%s'(len=%u),chunk_len=%u\n",topic,topic_len,chunk_len);
  if(strcmp(topic,"waterspurt")==0){
    LOG_INFO("temperature command\n");
    if(strcmp((const char*)chunk,"on")==0){
      LOG_INFO("switch on temp act\n");
      increasing_temperature=false;
      descreasing_temperature=true;
    }else if(strcmp((const char *)chunk,"off")==0){
      LOG_INFO("switch off temp act\n");
      increasing_temperature=true;
      descreasing_temperature=false;
    }
  }else{
    LOG_INFO("topic no valid");
  }
}
static void mqtt_event(struct mqtt_connection *m,mqtt_event_t event,void*data){
  switch(event){
    case MQTT_EVENT_CONNECTED:{
      printf("Application has an MQTT connection\n");
      state=STATE_CONNECTED;
      break;
    }
    case MQTT_EVENT_DISCONNECTED:{
      printf("MQTT Disconnected. reason %u\n",*((mqtt_event_t*)data));
      state=STATE_DISCONNECTED;
      process_poll(&mqtt_sensor_temperature);
      break;
    }
    case MQTT_EVENT_PUBLISH:{
      LOG_INFO("published on topic\n");
      msg_ptr=data;
      pub_handler(msg_ptr->topic,strlen(msg_ptr->topic),msg_ptr->payload_chunk,msg_ptr->payload_length);
      break;
    }
    case MQTT_EVENT_SUBACK:{
#if MQTT_311
      mqtt_suback_event_t *suback_event=(mqtt_suback_event_t*)data;
      if(suback_event->success){
        printf("Application is subscribed to topic successfully\n");
      }else{
        printf("Application failed to subscribe to topic  (ret code%x)\n".suback_event->return_code);
      }
#else
      printf("Application is subscribed to topic successfully\n");
#endif
    break;
    }
    case MQTT_EVENT_UNSUBACK:{
      printf("Application is unsubscribed to topic sucessfully\n");
      break;
    }
    case MQTT_EVENT_PUBACK:{
      printf("Publish complete.\n");
      break;
    }
    default:{
      printf("Application got a unhadled MQTT event %i\n",event);
      break;
    }
  }
}

static bool have_connectivity(void){
  if(uip_ds6_get_global(ADDR_PREFERRED)==NULL||uip_ds6_defrt_choose()==NULL){
    return false;
  }
  return true;
}

PROCESS_THREAD(mqtt_sensor_temperature,ev,data){
  PROCESS_BEGIN();
  char broker_address[CONFIG_IP_ADDR_STR_LEN];
  mqtt_status_t status;
  printf("MQTT sensor process starts\n");
  snprintf(sensor_id,BUFFER_SIZE,"%02x%02x%02x%02x%02x%02x",
          linkaddr_node_addr.u8[0],linkaddr_node_addr.u8[1],
          linkaddr_node_addr.u8[2],linkaddr_node_addr.u8[5],
          linkaddr_node_addr.u8[6],linkaddr_node_addr.u8[7]);
  //broker registrtion
  mqtt_register(&conn,&mqtt_sensor_temperature,sensor_id,mqtt_event,MAX_TCP_SEGMENT_SIZE);
  state=STATE_INIT;

  etimer_set(&periodic_timer,PUBLISH_INTERVAL);

  while(1){
    PROCESS_YIELD();
    if(ev==button_hal_press_event){
      LOG_INFO("button pressed");
      leds_on(LEDS_ALL);
      leds_off(LEDS_ALL);
      temperature=100;
    }
    if((ev==PROCESS_EVENT_TIMER&&data==&periodic_timer)||ev==PROCESS_EVENT_POLL){
      if(state==STATE_INIT){
        if(have_connectivity()==true){
          state=STATE_NET_OK;
        }
      }
        if(state==STATE_NET_OK){
          //connect to mqtt server 
          printf("Connecting..\n");
          memcpy(broker_address,broker_ip,strlen(broker_ip));
          mqtt_connect(&conn,broker_address,DEFAULT_BROKER_PORT,(DEFAULT_PUBLISH_INTERVAL*3)/CLOCK_SECOND,MQTT_CLEAN_SESSION_ON);
          state=STATE_CONNECTING;
        }
        if(state==STATE_CONNECTED){
          strcpy(sub_topic,"waterspurt");
          status=mqtt_subscribe(&conn,NULL,sub_topic,MQTT_QOS_LEVEL_0);
          if(status==MQTT_STATUS_OUT_QUEUE_FULL){
            LOG_ERR("tried to sub to a topic but queue full\n");
            PROCESS_EXIT();
          }
          state=STATE_SUBSCRIBED;
          LOG_INFO("subscribed to a topic\n");
        }
        if(state==STATE_SUBSCRIBED){
            sprintf(pub_topic,"%s","temperature");
            if(increasing_temperature||descreasing_temperature){
              variation=rand()%3;//valuein 0-2
              temperature=(increasing_temperature)?(temperature+variation):(temperature-variation);
            }else{
              if((rand()%10)<6){
                variation=(rand()%5)-2;//value in -2 2
                temperature=temperature+variation;
              }
            }
            LOG_INFO("publish something with temp->%d\n",temperature);
           
            sprintf(app_buffer,"{\"Node\":%d,\"temperature\":%d}",node_id,temperature);
            mqtt_publish(&conn,NULL,pub_topic,(uint8_t*)app_buffer,strlen(app_buffer),MQTT_QOS_LEVEL_0,MQTT_RETAIN_OFF);            
          }else if(state==STATE_DISCONNECTED){
          LOG_ERR("Disconnected from MQTT broker,restart the process");
          state=STATE_INIT;
        }
        etimer_set(&periodic_timer,PUBLISH_INTERVAL);
      }
    
  }
  PROCESS_END();
}