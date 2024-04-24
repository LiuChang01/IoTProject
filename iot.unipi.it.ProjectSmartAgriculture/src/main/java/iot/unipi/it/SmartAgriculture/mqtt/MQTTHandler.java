package iot.unipi.it.SmartAgriculture.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import com.google.gson.Gson;
import iot.unipi.it.SmartAgriculture.log.*;
import iot.unipi.it.SmartAgriculture.model.*;
import iot.unipi.it.SmartAgriculture.mqtt.sensors.humidity.*;
import iot.unipi.it.SmartAgriculture.coap.CoapRegistrationHandler;
import iot.unipi.it.SmartAgriculture.mqtt.sensors.temperature.*;

import java.net.SocketException;

public class MQTTHandler implements MqttCallback  {
	private final String BROKER="tcp://127.0.0.1:1883";
	private final String CLIENT_ID="SmartAgricultureCollector";
	private MqttClient mqttClient=null;
	private CoapRegistrationHandler coaphandler;
	private Gson parser;
	private HumidityCollector humiditycollector;
	private TemperatureCollector temperaturecollector;
	private Logger logger;
	private final int SECONDS_TO_WAIT_BEFORE_RECONNECT=5;
	private final int MAX_RECCONECTION=10;
	public MQTTHandler() throws SocketException {
		parser=new Gson();
		logger=Logger.getInstance();
		humiditycollector=new HumidityCollector();
		temperaturecollector=new TemperatureCollector();
		coaphandler=new CoapRegistrationHandler();
		do {
			try {
				mqttClient=new MqttClient(BROKER,CLIENT_ID);
				System.out.println("connecting to the broker:"+BROKER);
				mqttClient.setCallback(this);
				connectToBroker();
			}catch(MqttException me) {
				System.out.println("could not connect, retry..");
			}
		}while(!mqttClient.isConnected());
	}
	//function used to try to connect to broker
	private void connectToBroker()throws MqttException{
		mqttClient.connect();
		mqttClient.subscribe(humiditycollector.HUMIDITY_TOPIC);
		System.out.println("subscribed to the topic"+humiditycollector.HUMIDITY_TOPIC);
		mqttClient.subscribe(temperaturecollector.TEMPERATURE_TOPIC);
		System.out.println("subscribed to the topic"+temperaturecollector.TEMPERATURE_TOPIC);
	}
	//function to publish a message
	public void publishMessage(final String topic,final String message){
		try{
			mqttClient.publish(topic,new MqttMessage(message.getBytes()));
		}catch (MqttException e){
			e.printStackTrace();
		}
	}
	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		System.out.println("connection with broker lost");
		int iter=0;
		do {
			iter++;
			if(iter>MAX_RECCONECTION){
				System.err.println("impossible connect to broker");
				System.exit(-1);
			}
			try{
				Thread.sleep(SECONDS_TO_WAIT_BEFORE_RECONNECT*1000*iter);
				System.out.println("new attempt to connect with broker");
				connectToBroker();
			}catch (MqttException| InterruptedException e){
				e.printStackTrace();
			}
		}while(!this.mqttClient.isConnected());
		System.out.println("connection with broker done");
	}
	@Override
	public void messageArrived(String topic, MqttMessage mqttmessage) throws Exception {
		// TODO Auto-generated method stub
		String payload=new String(mqttmessage.getPayload());
		float avgh= humiditycollector.getAverage();
		float avgt=temperaturecollector.getAverage();
		if(topic.equals(humiditycollector.HUMIDITY_TOPIC)){
			HumidityModel humiditysample=parser.fromJson(payload, HumidityModel.class);
			humiditycollector.addHumiditySample(humiditysample);
			if(avgh< humiditycollector.getThreshold()){
				publishMessage("waterspurt","on");
				coaphandler.SwitchONOFF(true);
				System.out.println("Water spurt activated");
			}
		}else if(topic.equals(temperaturecollector.TEMPERATURE_TOPIC)){
			TemperatureModel temperaturesample=parser.fromJson(payload, TemperatureModel.class);
			temperaturecollector.addTemperatureSample(temperaturesample);
			if(avgt> temperaturecollector.getThreshold()){
				publishMessage("waterspurt","on");
				coaphandler.SwitchONOFF(true);
				System.out.println("Water spurt activated");
			}
		}

	}
	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		logger.logInfo("message correctly delivered");

	}
	public HumidityCollector getHumidityCollector(){
		return humiditycollector;
	}
	public TemperatureCollector getTemperatureCollector(){
		return temperaturecollector;
	}


}
