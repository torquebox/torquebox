package org.torquebox.stomp;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.catalina.Session;
import org.projectodd.stilts.stomp.spi.StompSession;

public class HttpStompSession implements StompSession {

    public HttpStompSession(HttpSession webSession) {
        this.webSession = webSession;
    }
    
    @Override
    public String getId() {
        return this.webSession.getId();
    }

    @Override
    public List<String> getAttributeNames() {
        List<String> names = new ArrayList<String>();
        
        Enumeration<String> webNames = this.webSession.getAttributeNames();
        
        while ( webNames.hasMoreElements() ) {
            names.add( webNames.nextElement() );
        }
        
        return names;
    }

    @Override
    public Object getAttribute(String name) {
        return this.webSession.getAttribute( name );
    }

    @Override
    public void setAttribute(String name, Object value) {
        this.webSession.setAttribute( name, value );
    }

    @Override
    public void removeAttribute(String name) {
        this.webSession.removeAttribute( name );
    }
    
    public void access() {
        ((Session)this.webSession).access();
    }
    
    public void endAccess() {
        ((Session)this.webSession).endAccess();
    }

    private HttpSession webSession;

}
