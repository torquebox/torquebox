package org.torquebox.rails.queues.deployers;

import org.jboss.virtual.VirtualFileFilter;
import org.jboss.virtual.VisitorAttributes;
import org.jboss.virtual.plugins.vfs.helpers.SuffixMatchFilter;
import org.torquebox.ruby.enterprise.queues.deployers.AbstractRubyQueuesScanningDeployer;

public class RailsQueuesScanningDeployer extends AbstractRubyQueuesScanningDeployer {

	private static final String APP_QUEUES = "app/queues/";
	private static final VirtualFileFilter QUEUE_FILTER = new SuffixMatchFilter("_queue.rb", VisitorAttributes.DEFAULT);
	
	public RailsQueuesScanningDeployer() {
		setPath( APP_QUEUES );
		setFilter( QUEUE_FILTER );
	}

}
