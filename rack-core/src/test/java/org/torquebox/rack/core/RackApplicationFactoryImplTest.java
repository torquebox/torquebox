package org.torquebox.rack.core;

import org.jruby.Ruby;
import org.junit.Test;
import org.torquebox.rack.spi.RackApplication;
import org.torquebox.test.ruby.AbstractRubyTestCase;

import static org.junit.Assert.*;

public class RackApplicationFactoryImplTest extends AbstractRubyTestCase {

	
	@Test
	public void testSingleRackup_OneRuntime() throws Exception {
		String rackUpScript = "lambda(){|env|puts env}";
		RackApplicationFactoryImpl appFactory = new RackApplicationFactoryImpl( rackUpScript );
		
		Ruby ruby = createRuby();
		RackApplication rackApp1 = appFactory.createRackApplication( ruby );
		assertNotNull( rackApp1 );
		RackApplication rackApp2 = appFactory.createRackApplication( ruby );
		assertNotNull( rackApp2 );
		assertSame( rackApp1, rackApp2 );
	}
	
	@Test
	public void testSingleRackup_TwoRuntimes() throws Exception {
		String rackUpScript = "lambda(){|env|puts env}";
		RackApplicationFactoryImpl appFactory = new RackApplicationFactoryImpl( rackUpScript );
		
		Ruby ruby1 = createRuby();
		RackApplication rackApp1a = appFactory.createRackApplication( ruby1 );
		assertNotNull( rackApp1a );
		RackApplication rackApp1b = appFactory.createRackApplication( ruby1 );
		assertNotNull( rackApp1b );
		assertSame( rackApp1a, rackApp1b );
		
		Ruby ruby2 = createRuby();
		RackApplication rackApp2a = appFactory.createRackApplication( ruby2 );
		assertNotNull( rackApp2a );
		RackApplication rackApp2b = appFactory.createRackApplication( ruby2 );
		assertNotNull( rackApp2b );
		assertSame( rackApp2a, rackApp2b );
		
		assertFalse( rackApp1a == rackApp2a );
	}
}
