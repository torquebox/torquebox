package org.torquebox.jobs.metadata;

import java.util.HashMap;
import java.util.Map;

public class RubyJobsMetaData {
	
	private Map<String,RubyJobMetaData> jobs = new HashMap<String,RubyJobMetaData>();
	
	public RubyJobsMetaData() {
		
	}
	
	public void addJob(RubyJobMetaData job) {
		this.jobs.put( job.getName(), job );
	}
	
	public RubyJobMetaData getJobByName(String name) {
		return this.jobs.get( name );
	}

}
