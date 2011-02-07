package org.torquebox.jobs.core;

import org.jboss.logging.Logger;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.torquebox.interp.spi.RubyRuntimePool;

public class RubyJob implements Job, StatefulJob {

    private static final Logger log = Logger.getLogger( RubyJob.class );
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};

    private RubyRuntimePool runtimePool;
    private IRubyObject component;

    public RubyJob(RubyRuntimePool runtimePool, IRubyObject component) {
        this.runtimePool = runtimePool;
        this.component = component;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.debug(  "Executing job: " + this );
        try {
            Object jobResult = JavaEmbedUtils.invokeMethod( component.getRuntime(), component, "run", EMPTY_OBJECT_ARRAY, Object.class );
            context.setResult( jobResult );
        } finally {
            this.runtimePool.returnRuntime( component.getRuntime() );
        }
    }

}
