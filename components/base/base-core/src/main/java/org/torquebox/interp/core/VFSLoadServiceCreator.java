/* Copyright 2010 Red Hat, Inc. */

package org.torquebox.interp.core;

import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig.LoadServiceCreator;
import org.jruby.runtime.load.LoadService;

/**
 * Factory for {@link VFSLoadService}.
 * 
 * @see VFSLoadService
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class VFSLoadServiceCreator implements LoadServiceCreator {

    @Override
    public LoadService create(Ruby ruby) {
        return new VFSLoadService( ruby );
    }

}
