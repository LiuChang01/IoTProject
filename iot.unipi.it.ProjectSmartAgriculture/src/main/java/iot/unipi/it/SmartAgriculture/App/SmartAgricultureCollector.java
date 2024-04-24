package iot.unipi.it.SmartAgriculture.App;
import iot.unipi.it.SmartAgriculture.mqtt.MQTTHandler;
import iot.unipi.it.SmartAgriculture.mqtt.sensors.humidity.HumidityCollector;
import iot.unipi.it.SmartAgriculture.mqtt.sensors.temperature.TemperatureCollector;
import iot.unipi.it.SmartAgriculture.coap.CoapRegistrationHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
public class SmartAgricultureCollector {

	public static void main(String[] args) throws SocketException {
		// TODO Auto-generated method stub
		MQTTHandler mqttNetworkHandler=new MQTTHandler();
		CoapRegistrationHandler coaphadler= new CoapRegistrationHandler();
		coaphadler.start();
		printAvailableCommands();
		BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(System.in));
		String command;
		String [] parts;
		while(true){
			System.out.print("->");
			try{
				command= bufferedReader.readLine();
				parts=command.split(" ");
				switch(parts[0]){
					case "!help": {
						help_function(parts);
						break;
					}
					case "!get_humidity":{
						get_humidity_function(mqttNetworkHandler.getHumidityCollector());
						break;
					}
					case "!get_temperature":{
						get_temperature_function(mqttNetworkHandler.getTemperatureCollector());
						break;
					}
					case "!spurt_the_water_on":{
						spurt_the_water_on_function(mqttNetworkHandler,coaphadler);
						break;
					}
					case "!spurt_the_water_off":{
						spurt_the_water_off_function(mqttNetworkHandler,coaphadler);
						break;
					}
					case "!exit":{
						System.out.println("exiting.. \nthanks for using the app");
						System.exit(0);
						break;
					}
					case "!set_humidity_th":{
						set_humidity_th_function(parts,mqttNetworkHandler.getHumidityCollector());
						break;
					}
					case "!set_temperature_th":{
						set_temperature_th_function(parts,mqttNetworkHandler.getTemperatureCollector());
						break;
					}
					default:{
						System.out.println("What command is this ???????");
						break;
					}
				}
				printAvailableCommands();
			}catch (IOException e){
				e.printStackTrace();
			}
		}

	}
	public static void set_humidity_th_function(String [] parts,HumidityCollector humidityCollector){
		if(parts.length!=2){
			System.out.println("not good use of the command");
			return;
		}
		float bound;
		bound=Float.parseFloat(parts[1]);
		humidityCollector.setThreshold(bound);
		System.out.println("threshold setted ->"+bound);
	}
	public static void set_temperature_th_function(String [] parts,TemperatureCollector temperatureCollector){
		if(parts.length!=2){
			System.out.println("not good use of the command");
			return;
		}
		float bound;
		bound=Float.parseFloat(parts[1]);
		temperatureCollector.setThreshold(bound);
		System.out.println("threshold setted ->"+bound);
	}
	public static void help_function(String [] parts){
		if(parts.length!=2){
			System.out.println("incorrect use of help command");
		}else{
			switch (parts[1]){
				case "!help":
				case "help":
					System.out.println("Command that let's you to get info about commands");
					break;
				case "!get_humidity":
				case "get_humidity":
					System.out.println("Command that let's you to get info about humidity");
					break;
				case "!get_temperature":
				case "get_temperature":
					System.out.println("Command that let's you to get info about temperature");
					break;
				case "!exit":
				case "exit":
					System.out.println("Exit the application");
					break;
				case "!spurt_the_water_off":
				case "spurt_the_water_off":
					System.out.println("command that let's you activate the Actuator");
					break;
				case "!spurt_the_water_on":
				case "spurt_the_water_on":
					System.out.println("command that let's you deactivate the Actuator");
					break;
				case "!set_temperature_th":
				case "set_temperature_th":{
					System.out.println("command that let's you set the threshold of temperature by passing the temperature threshold as argument ");
					break;
				}
				case "!set_humidity_th":
				case "set_humidity_th":{
					System.out.println("command that let's you set the threshold of humidity by passing the humidity threshold as argument ");
					break;
				}
				default:
					System.out.println("Command not used well");
					break;
			}
		}
	}
	public static void printAvailableCommands(){
		System.out.println("**************Smart Agriculture***************\n"+
							"The followings are commands that are available:\n"+
							"1)!help <command> ->show the detail of commands\n"+
							"2)!get_humidity ->get the last measurement of humidity\n"+
							"3)!get_temperature->get the measurement of temperature\n"+
							"4)!spurt_the_water_on ->activate the actuator for spurting the water\n"+
							"5)!spurt_the_water_off->deactivate the actuator for spurting the water\n"+
							"6)!set_humidity_th <threshold>->set the threshold of activation of humidity actuator\n"+
							"7)!set_temperature_th <threshold>->set the threshold of activation of temperature actuator\n"+
							"8)!exit ->exiting from the application\n");
	}
	public static void get_humidity_function(HumidityCollector humiditycollector){
		float humidity= humiditycollector.getAverage();
		System.out.println("The humidity level in the camp is ->"+humidity+"%\n");
	}
	public static void get_temperature_function(TemperatureCollector temperaturecollector){
		float temperature=temperaturecollector.getAverage();
		System.out.println("The temperature degrees in the camp is ->"+temperature+"C\n");
	}
	public static void spurt_the_water_on_function(MQTTHandler mqttHadler,CoapRegistrationHandler coaphandler){
		mqttHadler.publishMessage("waterspurt","on");
		coaphandler.SwitchONOFF(true);
		System.out.println("Water spurt activated");
	}
	public static void spurt_the_water_off_function(MQTTHandler mqttHandler,CoapRegistrationHandler coaphandler){
		mqttHandler.publishMessage("waterspurt","off");
		coaphandler.SwitchONOFF(false);
		System.out.println("Water spurt off");
	}
}
