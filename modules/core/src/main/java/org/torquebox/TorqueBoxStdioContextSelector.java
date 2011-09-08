package org.torquebox;

import org.jboss.stdio.StdioContext;
import org.jboss.stdio.StdioContextSelector;
import org.torquebox.core.runtime.RuntimeContext;

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
        if (RuntimeContext.getCurrentRuntime() != null &&
                RuntimeContext.getCurrentRuntime().getInstanceConfig().isDebug()) {
            return debugContext;
        }
        return defaultContext;
    }


    private final StdioContext defaultContext;
    private final StdioContext debugContext;

}
