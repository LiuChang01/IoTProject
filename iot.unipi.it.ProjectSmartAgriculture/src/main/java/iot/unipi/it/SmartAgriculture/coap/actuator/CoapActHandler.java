package iot.unipi.it.SmartAgriculture.coap.actuator;
import iot.unipi.it.SmartAgriculture.coap.actuator.water.ActuatorCoap;
public class CoapActHandler {
    private ActuatorCoap waterSpurt=new ActuatorCoap();
    private static CoapActHandler instance=null;
    public static CoapActHandler getInstance(){
        if(instance==null)
            instance=new CoapActHandler();
        return instance;
    }
    public void registerAct(String ip){
        waterSpurt.registerAct(ip);
    }
    public void unregisterAct(String ip){
        waterSpurt.unregisterAct(ip);
    }
    public void switchONOFF(boolean on){
        waterSpurt.switchAct(on);
    }
}
