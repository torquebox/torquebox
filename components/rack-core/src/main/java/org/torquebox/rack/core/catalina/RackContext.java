package org.torquebox.rack.core.catalina;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.valves.RequestDumperValve;
import org.jboss.aop.microcontainer.aspects.jmx.JMX;
import org.jboss.logging.Logger;

@JMX(exposedInterface=RackContextMBean.class, registerDirectly=true)
public class RackContext extends StandardContext implements RackContextMBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger( RackContext.class );

	
	@Override
	public void init() throws Exception {
		log.info( "init()" );
		super.init();
		
		//SessionRepairValve sessionRepairValve = new SessionRepairValve();
		//sessionRepairValve.setContainer( this );
		//this.addValve( sessionRepairValve );
		
		RequestDumperValve valve = new RequestDumperValve();
		valve.setContainer( this );
		this.addValve( valve );
		
	}
	
	
	
	
	
	

}
