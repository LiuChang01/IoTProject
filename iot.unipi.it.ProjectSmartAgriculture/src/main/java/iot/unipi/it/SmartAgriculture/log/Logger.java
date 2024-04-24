package iot.unipi.it.SmartAgriculture.log;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.logging.*;
public class Logger {
	private static Logger instance;
	private static java.util.logging.Logger logger;
	public static Logger getInstance() {
		if(instance==null) {
			instance=new Logger();
		}
		return instance;
	}
	private Logger() {
		logger=java.util.logging.Logger.getLogger(Logger.class.getName());
		try {
			FileHandler filehandler=new FileHandler("./info.log");
			logger.addHandler(filehandler);
			filehandler.setFormatter(new Formatter() {
				@Override
				public String format(LogRecord logrecord) {
					return logrecord.getMessage()+"\n";
				}
			});
			logger.setUseParentHandlers(false);
			
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	public void log(String topic,String message) {
		Timestamp timestamp=new Timestamp(System.currentTimeMillis());
		logger.info("["+topic+"-"+timestamp+"] "+message);
	}
	public void logTemperature(String message) {
		log("TEMPERATURE",message);
		
	}
	public void logHumidity(String message) {
		log("HUMIDITY",message);
		
	}
	public void logInfo(String message) {
		log("INFO",message);
		
	}
}
