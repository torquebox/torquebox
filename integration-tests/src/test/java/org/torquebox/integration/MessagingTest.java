package org.torquebox.integration;

import static org.junit.Assert.*;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.jruby.Ruby;
import org.jruby.RubyString;
import org.torquebox.test.ruby.TestRubyFactory;


@Run(RunModeType.AS_CLIENT)
public class MessagingTest extends AbstractIntegrationTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return createDeployment( "rails/2.3.8/messaging-rails.yml" );
	}

	@Test
	public void testQueueMessageProcessor() throws Exception {
        driver.get( "http://localhost:8080/messaging-rails/message/queue?text=ham%20biscuit" );
        Ruby runtime = IntegrationTestRubyFactory.createRuby();
        
        RubyString result = (RubyString) runtime.evalScriptlet( slurpResource( "org/torquebox/integration/messaging_test.rb" ) );
        
        //RubyString result = (RubyString) runtime.evalScriptlet("require 'rubygems'\nrequire 'org.torquebox.torquebox-messaging-client'\nTorqueBox::Messaging::Queue.new('/queues/results').receive(:timeout => 2000)");
        
        assertEquals( "result=ham biscuit", result.toString() );
	}

}
