package org.torquebox.rack.core;

import org.jruby.Ruby;
import org.junit.Test;
import org.torquebox.test.ruby.AbstractRubyTestCase;

public class RubyRackApplicationTest extends AbstractRubyTestCase {

	@Test
	public void testConstruct() throws Exception {
		Ruby ruby = createRuby();
		
		String rackup = "";
		//RubyRackApplication rackApp = new RubyRackApplication( ruby, rackup );
	}
}
