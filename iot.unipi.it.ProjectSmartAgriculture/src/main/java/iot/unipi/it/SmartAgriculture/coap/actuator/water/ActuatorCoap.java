package iot.unipi.it.SmartAgriculture.coap.actuator.water;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import java.util.*;
public class ActuatorCoap {
    private List<CoapClient> clientActuatorSwitchList=new ArrayList<>();
    public void registerAct(String ip){
        for (CoapClient coapClient : clientActuatorSwitchList) {
            if (coapClient.getURI().equals(ip)) {
                return;
            }
        }
        System.out.println("registration of the WaterSpurter with ip->"+ip);
        CoapClient newActuator=new CoapClient("coap://["+ip+"]/WaterSpurt");
        clientActuatorSwitchList.add(newActuator);
    }
    public void unregisterAct(String ip){
        for(int i=0;i< clientActuatorSwitchList.size();i++){
            if(clientActuatorSwitchList.get(i).getURI().equals(ip)){
                clientActuatorSwitchList.remove(i);
            }
        }
    }
    public void switchAct(boolean on){
        if(clientActuatorSwitchList==null){
            return;
        }
        String msg="mode="+(on?"on":"off");
        for(CoapClient client:clientActuatorSwitchList){
            client.put(new CoapHandler() {
                @Override
                public void onLoad(CoapResponse response) {
                    if(response!=null){
                        if(!response.isSuccess()){
                            System.out.println("error switch the act");
                        }
                    }
                }
                @Override
                public void onError() {
                    System.out.println("error on->"+client.getURI());
                }
            },msg,MediaTypeRegistry.TEXT_PLAIN);
        }
    }
}
