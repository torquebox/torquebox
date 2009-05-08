package org.torquebox.rails.web.deployers;

public interface RailsRackUpScriptProvider extends RackUpScriptProvider, Comparable<RailsRackUpScriptProvider> {
	
	int getMajorVersion();
	int getMinorVersion();
	int getTinyVersion();

}
