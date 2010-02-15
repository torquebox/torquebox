package org.torquebox.ruby.enterprise.web.rack.deployers;

import org.apache.catalina.Valve;

public interface RackContextMBean {

	void addValve(Valve valve);
}
