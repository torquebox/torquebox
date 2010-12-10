package org.torquebox.integration.arquillian;

import static org.junit.Assert.assertEquals;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jruby.Ruby;
import org.junit.Test;
import org.torquebox.integration.IntegrationTestRubyFactory;


@Run(RunModeType.AS_CLIENT)
public class MessagingTest extends AbstractIntegrationTest {
    
	@Deployment
	public static JavaArchive createDeployment() {
		return createDeployment( "rails/2.x/messaging-rails.yml" );
	}

	@Test
	public void testQueueMessageProcessor() throws Exception {
        Ruby runtime = IntegrationTestRubyFactory.createRuby();
        runtime.evalScriptlet(" require 'org.torquebox.torquebox-messaging-client'\n" );
        
        driver.get( "http://localhost:8080/messaging-rails/message/queue?text=ham+biscuit" );
        
        Object result = runtime.evalScriptlet( slurpResource( "org/torquebox/integration/messaging_test.rb" ) );
        System.err.println(" result=" + result );
        
        assertEquals( "result=ham biscuit", result.toString() );
	}

}
