package org.torquebox.messaging.core;

import java.util.Map;

/**
 * Common interface for any web socket media handlers.
 * @author mdobozy
 * @see WebSocketsServer
 *
 */
public interface WebSocketsMediaUpstreamHandler {

	/**
	 * Provides configuration information for a given media handler.
	 * @param params
	 */
	public void configure(Map<String, Object> params);

}
