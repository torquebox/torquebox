package org.torquebox.rack.core.catalina;

import org.apache.catalina.Valve;

public interface RackContextMBean {

	void addValve(Valve valve);
}
