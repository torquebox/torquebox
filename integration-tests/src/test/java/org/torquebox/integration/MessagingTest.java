package org.torquebox.integration;

import static org.junit.Assert.assertEquals;

import java.rmi.RMISecurityManager;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jruby.Ruby;
import org.jruby.RubyString;
import org.junit.Before;
import org.junit.Test;


@Run(RunModeType.AS_CLIENT)
public class MessagingTest extends AbstractIntegrationTest {
    
    @Before
    public void setUpSecurityManager() {
        if(System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
    }

	@Deployment
	public static JavaArchive createDeployment() {
		return createDeployment( "rails/2.3.8/messaging-rails.yml" );
	}

	@Test
	public void testQueueMessageProcessor() throws Exception {
        driver.get( "http://localhost:8080/messaging-rails/message/queue?text=ham%20biscuit" );
        Ruby runtime = IntegrationTestRubyFactory.createRuby();
        
        RubyString result = (RubyString) runtime.evalScriptlet( slurpResource( "org/torquebox/integration/messaging_test.rb" ) );
        
        assertEquals( "result=ham biscuit", result.toString() );
	}

}
