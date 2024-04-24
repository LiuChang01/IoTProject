package iot.unipi.it.SmartAgriculture.model;
import java.sql.Timestamp;
import java.util.Calendar;
public class HumidityModel {
	private int node;//node id
	private float humidity;//was integer but i  manage it like float 21.0
	private Timestamp timestamp;

	public HumidityModel(int node,float humidity,Timestamp timestamp) {
		this.node=node;
		this.humidity=humidity;
		this.timestamp=timestamp;
	}
	//getters
	public int getNode() {
		return node;
	}
	public float getHumidity() {
		return humidity;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}
	//setters
	public void setNode(int node) {
		this.node=node;
	}

	public void setHumidity(float humidity) {
		this.humidity=humidity;
	}
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp=timestamp;
	}
	//check if the sample is valid(measured in the last 30 seconds) used for mean 
	public boolean isValid() {
		Calendar calendar=Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, -30);//minus 30 secs
		Timestamp second30Ago=new Timestamp(calendar.getTime().getTime());
		return timestamp.after(second30Ago);
	}
	@Override
	public String toString() {
		return "HumidityModel{"+"node="+node+", humidity="+humidity+", timestamp="+timestamp.toString()+"}";
	}
}
