package iot.unipi.it.SmartAgriculture.coap;
import iot.unipi.it.SmartAgriculture.coap.actuator.CoapActHandler;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

import iot.unipi.it.SmartAgriculture.persistence.*;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class CoapRegistrationHandler extends CoapServer {
    private final static CoapActHandler coapActHandler=CoapActHandler.getInstance();
    public CoapRegistrationHandler()throws SocketException{
        this.add(new CoapRegistrationResource());
    }
    public void SwitchONOFF(boolean on){
        coapActHandler.switchONOFF(on);
    }

    class CoapRegistrationResource extends CoapResource{
        public CoapRegistrationResource(){
            super("registration");
        }

        @Override
        public void handlePOST(CoapExchange exchange){
            String act=exchange.getRequestText();
            String ip=exchange.getSourceAddress().getHostAddress();
            coapActHandler.registerAct(ip);
            DBDriver.getInstance().insertAct(ip);
            exchange.respond(CoAP.ResponseCode.CREATED,"Success".getBytes(StandardCharsets.UTF_8));
        }
        @Override
        public void handleDELETE(CoapExchange exchange){
            String [] request=exchange.getRequestText().split("-");
            String ip=request[0];
            coapActHandler.unregisterAct(ip);
            exchange.respond(CoAP.ResponseCode.DELETED,"Cancellation Completed!".getBytes(StandardCharsets.UTF_8));
        }
    }
}


