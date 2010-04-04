package org.torquebox.rack.core;

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Test;
import org.torquebox.test.ruby.AbstractRubyTestCase;

public class RubyRackApplicationTest extends AbstractRubyTestCase {

	@Test
	public void testConstruct() throws Exception {
		Ruby ruby = createRuby();
		
		String rackup = "run Proc.new {|env| [200, {'Content-Type' => 'text/html'}, env.inspect]}";
		RubyRackApplication rackApp = new RubyRackApplication( ruby, rackup );
		IRubyObject rubyApp = rackApp.getRubyApplication();
		assertNotNil( rubyApp );
	}
	

}
