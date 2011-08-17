package org.torquebox.stomp;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.apache.catalina.Manager;
import org.projectodd.stilts.conduit.spi.StompSessionManager;
import org.projectodd.stilts.conduit.stomp.SimpleStompSessionManager;
import org.projectodd.stilts.stomp.StompException;
import org.projectodd.stilts.stomp.spi.StompSession;

public class HttpStompSessionManager extends SimpleStompSessionManager implements StompSessionManager {

    public HttpStompSessionManager(Manager webSessionManager) {
        this.webSessionManager = webSessionManager;
    }

    @Override
    public StompSession findSession(String sessionId) throws StompException {
        try {
            HttpSession webSession = (HttpSession) webSessionManager.findSession( sessionId );
            if (webSession != null) {
                return new HttpStompSession( webSession );
            }
            return super.findSession(sessionId);
        } catch (IOException e) {
            throw new StompException( e );
        }
    }

    private Manager webSessionManager;

}
