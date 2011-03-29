package org.torquebox.jobs.core;




public interface ScheduledJobMBean {
    
    /** Start this job. */
    void start() throws Exception;
    
    /** Stop this job. */
    void stop() throws Exception;
    
    /** Retrieve the ruby class name. */
    String getRubyClassName();
    
    /** Retrieve the cronspec */
    String getCronExpression();
    
    /** Set the cronspec */
    void setCronExpression(String cronspec);
    
    /** Is this job currently started? */
    boolean isStarted();
    
    /** Is this job currently stopped? */
    boolean isStopped();
    
    /** Get current status. */
    String getStatus();

}
