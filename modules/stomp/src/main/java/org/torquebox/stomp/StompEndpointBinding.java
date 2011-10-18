package org.torquebox.stomp;


public class StompEndpointBinding {
    
    public StompEndpointBinding(String host, int port, String context) {
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public String getEndpointURL() {
        return "ws://" + this.host + ":" + this.port + this.context;
    }
    
    public String toString() {
        return getEndpointURL();
    }
    
    private String host;
    private int port;
    private String context;

}
