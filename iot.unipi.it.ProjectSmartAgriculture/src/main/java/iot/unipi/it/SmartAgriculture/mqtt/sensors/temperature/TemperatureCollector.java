package iot.unipi.it.SmartAgriculture.mqtt.sensors.temperature;
import iot.unipi.it.SmartAgriculture.model.*;
import iot.unipi.it.SmartAgriculture.persistence.*;
import java.sql.Timestamp;
import java.util.*;
public class TemperatureCollector {
	public final String TEMPERATURE_TOPIC="temperature";
	public final String ACT_TOPIC="waterspurt";
	private Map<Integer,TemperatureModel> lastTemperatureSamples;
	private float threshold=75;
	public TemperatureCollector() {
		lastTemperatureSamples=new HashMap<Integer, TemperatureModel>();

	}
	//function adds a new temperature sample give temp sample
	public void addTemperatureSample(TemperatureModel temperatureSample) {
		temperatureSample.setTimestamp(new Timestamp(System.currentTimeMillis()));
		lastTemperatureSamples.put(temperatureSample.getNode(), temperatureSample);
		DBDriver.getInstance().insertTemperatureSample(temperatureSample);
		//remove old samples from the lastTemperatuerSample map
		lastTemperatureSamples.entrySet().removeIf(entry->!entry.getValue().isValid());
	}
	//computes the average
	public float getAverage(){
		int Num=lastTemperatureSamples.size();
		float sum=lastTemperatureSamples.values().stream().map(TemperatureModel::getTemperature).reduce((float)0,Float::sum);
		return sum/Num;
	}
	public void setThreshold(float threshold){
		this.threshold=threshold;
	}
	public float getThreshold(){
		return threshold;
	}

}
