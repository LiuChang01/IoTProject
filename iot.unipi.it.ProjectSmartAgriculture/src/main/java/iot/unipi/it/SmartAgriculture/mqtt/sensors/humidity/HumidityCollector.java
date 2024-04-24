package iot.unipi.it.SmartAgriculture.mqtt.sensors.humidity;
import iot.unipi.it.SmartAgriculture.model.*;
import iot.unipi.it.SmartAgriculture.persistence.*;
import java.sql.Timestamp;
import java.util.*;
public class HumidityCollector {
    public final String HUMIDITY_TOPIC="humidity";
    public final String ACT_TOPIC="waterspurt";
    private Map<Integer,HumidityModel> lastHumiditySamples;
    private float threshold=25;
    public HumidityCollector() {
        lastHumiditySamples=new HashMap<Integer, HumidityModel>();

    }
    //function adds a new humidity sample give temp sample
    public void addHumiditySample(HumidityModel humiditySample) {
        humiditySample.setTimestamp(new Timestamp(System.currentTimeMillis()));
        lastHumiditySamples.put(humiditySample.getNode(), humiditySample);
        DBDriver.getInstance().insertHumiditySample(humiditySample);
        //remove old samples from the lastTemperatuerSample map
        lastHumiditySamples.entrySet().removeIf(entry->!entry.getValue().isValid());
    }
    //computes the average
    public float getAverage(){
        int Num=lastHumiditySamples.size();
        float sum=lastHumiditySamples.values().stream().map(HumidityModel::getHumidity).reduce((float)0,Float::sum);
        return sum/Num;
    }
    public void setThreshold(float threshold){
        this.threshold=threshold;
    }
    public float getThreshold(){
        return threshold;
    }
}
