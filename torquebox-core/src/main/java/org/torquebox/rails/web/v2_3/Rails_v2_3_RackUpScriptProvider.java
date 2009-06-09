package org.torquebox.rails.web.v2_3;

import org.torquebox.rails.web.deployers.AbstractRailsRackUpScriptProvider;

public class Rails_v2_3_RackUpScriptProvider extends AbstractRailsRackUpScriptProvider {

	public Rails_v2_3_RackUpScriptProvider() {
		super(2, 3, 0);
	}

	public String getRackUpScript(String context) {
		if ( context.endsWith( "/" ) ) {
			context = context.substring( 0, context.length() - 1 );
		}
		
		String script = 
			"RELATIVE_URL_ROOT=%q(" + context + ")\n" +
			"require %q(org/torquebox/rails/web/v2_3/rails_rack_dispatcher)\n" +
			"::Rack::Builder.new {\n" + 
			"  run ActionController::Dispatcher.new\n" +
			"}.to_app\n";

		return script;
	}

}
