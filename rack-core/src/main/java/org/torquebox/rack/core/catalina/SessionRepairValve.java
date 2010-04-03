package org.torquebox.rack.core.catalina;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.tomcat.util.buf.MessageBytes;
import org.jboss.logging.Logger;

public class SessionRepairValve extends ValveBase {

	private static final Logger log = Logger.getLogger(SessionRepairValve.class);

	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		String requestUri = request.getRequestURI();
		
		Pattern pattern = Pattern.compile( "^(.*);JSESSIONID=([^#]+)(.*)$");
		
		Matcher matcher = pattern.matcher( requestUri );
		
		if ( matcher.matches() ) { 
			log.info( "REGEXP MATCH=" + requestUri );
			
			String uriPrefix = matcher.group(1);
			String sessionId = matcher.group(2);
			String uriSuffix = matcher.group(3);
			
			log.info( "prefix=" + uriPrefix );
			log.info( "sessionId=" + sessionId );
			log.info( "suffix=" + uriSuffix );
			
			String repairedUri = uriPrefix + uriSuffix;
			log.info( "repaired=[" + repairedUri + "]" );
			MessageBytes uriMb = request.getCoyoteRequest().requestURI();
			uriMb.recycle();
			uriMb.setString( repairedUri );
			if ( request.getRequestedSessionId() == null ) {
				log.info( "setting requested sessionId to [" + sessionId + "]" );
				request.setRequestedSessionId( sessionId );
			}
		} else {
			log.info( "no match=" + requestUri );
		}
		
		getNext().invoke(request, response);
	}
}