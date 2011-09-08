package org.torquebox;

import org.jboss.stdio.StdioContext;
import org.jboss.stdio.StdioContextSelector;
import org.jruby.Ruby;
import org.torquebox.core.runtime.RuntimeContext;
import org.torquebox.core.runtime.TorqueBoxRubyInstanceConfig;

public class TorqueBoxStdioContextSelector implements StdioContextSelector {

    /**
     * Construct a new instance.
     *
     * @param defaultContext the context to use (must not be {@code null})
     * @param debugContext the context to use for the Ruby debugger (must not be {@code null})
     */
    public TorqueBoxStdioContextSelector(final StdioContext defaultContext, final StdioContext debugContext) {
        if (defaultContext == null) {
            throw new NullPointerException( "defaultContext is null" );
        }
        if (debugContext == null) {
            throw new NullPointerException( "debugContext is null" );
        }
        this.defaultContext = defaultContext;
        this.debugContext = debugContext;
    }

    /** {@inheritDoc} */
    public StdioContext getStdioContext() {
        Ruby runtime = RuntimeContext.getCurrentRuntime();
        if (runtime != null && runtime.getInstanceConfig() instanceof TorqueBoxRubyInstanceConfig) {
            TorqueBoxRubyInstanceConfig config = (TorqueBoxRubyInstanceConfig) runtime.getInstanceConfig();
            if (config.isInteractive()) {
                return debugContext;
            }
        }
        return defaultContext;
    }


    private final StdioContext defaultContext;
    private final StdioContext debugContext;

}
