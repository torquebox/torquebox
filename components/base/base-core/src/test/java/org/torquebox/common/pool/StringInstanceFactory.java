package org.torquebox.common.pool;

import org.torquebox.common.spi.InstanceFactory;

public class StringInstanceFactory implements InstanceFactory<String> {

    private int counter = 0;

    @Override
    public String create() throws Exception {
        return "Instance-" + (++counter);
    }

    @Override
    public void dispose(String instance) {
        // no-op
    }

}
