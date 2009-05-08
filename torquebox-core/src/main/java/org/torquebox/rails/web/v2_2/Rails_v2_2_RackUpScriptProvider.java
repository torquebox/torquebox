package org.torquebox.rails.web.v2_2;

import org.torquebox.rails.web.deployers.AbstractRailsRackUpScriptProvider;

public class Rails_v2_2_RackUpScriptProvider extends AbstractRailsRackUpScriptProvider {

	public Rails_v2_2_RackUpScriptProvider() {
		super(2, 0, 0 );
	}

	public String getRackUpScript(String context) {
		if ( context.endsWith( "/" ) ) {
			context = context.substring( 0, context.length() - 1 );
		}
		
		String script = 
			"require %q(org/torquebox/rails/web/v2_2/rails_rack_dispatcher)\n" +
			"::Rack::Builder.new {\n" + 
			"  run JBoss::Rails::Rack::Dispatcher.new(%q("+ context + "))\n" +
			"}.to_app\n";

		return script;
	}

}
