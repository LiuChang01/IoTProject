package iot.unipi.it.SmartAgriculture.persistence;
import iot.unipi.it.SmartAgriculture.model.*;
import java.sql.*;
public class DBDriver {
	private static DBDriver instance=null;
	private static String databaseIp;
	private static  int databasePort;
	private static String databaseUsername;
	private static String databasePassword;
	private static String databaseName;
	public static DBDriver getInstance() {
		if(instance==null) {
			instance=new DBDriver();
		}
		return instance;
	}
	private DBDriver() {
		//set db parameters
		databaseIp="localhost";
		databasePassword="PASSWORD";
		databaseName="SmartAgricultureDB";
		databasePort=3306;
		databaseUsername="root";
	}
	//conn to DB
	private Connection getConnection()throws SQLException{
		return DriverManager.getConnection("jdbc:mysql://"+databaseIp+":"+databasePort+"/"+databaseName+"?zeroDateTimeBehaviour=CONVERT_TO_NULL&serverTimezone=CET",databaseUsername,databasePassword);
		
	}
	public void insertHumiditySample(HumidityModel humiditysample) {
		try {
			Connection connection=getConnection();
			PreparedStatement statement=connection.prepareStatement("INSERT INTO humidity (node,percentuage) VALUES(?,?)");
			statement.setInt(1, humiditysample.getNode());
			statement.setFloat(2, humiditysample.getHumidity());
			statement.executeUpdate();
		}catch(final SQLException e) {
			e.printStackTrace();
		}
	}
	public void insertTemperatureSample(TemperatureModel temperaturesample) {
		try {
			Connection connection=getConnection();
			PreparedStatement statement=connection.prepareStatement("INSERT INTO temperature (node,degrees) VALUES(?,?)");
			statement.setInt(1, temperaturesample.getNode());
			statement.setFloat(2, temperaturesample.getTemperature());
			statement.executeUpdate();
		}catch(final SQLException e) {
			e.printStackTrace();
		}
	}
	public void insertAct(String ip){
		try {
			Connection connection=getConnection();
			PreparedStatement statement=connection.prepareStatement("INSERT INTO actuators (ipAddr) VALUES(?)");
			statement.setString(1, ip);
			statement.executeUpdate();
		}catch(final SQLException e) {
			e.printStackTrace();
		}
	}
	
}
