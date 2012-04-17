/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.core.runtime;

import java.io.IOException;

import org.jruby.Ruby;
import org.jruby.runtime.load.LoadService;
import org.jruby.runtime.load.LoadServiceResource;

public class NonLeakingLoadService extends LoadService {

    public NonLeakingLoadService(Ruby runtime) {
        super( runtime );
    }

    @Override
    protected LoadServiceResource tryResourceFromJarURL(SearchState state, String baseName, SuffixType suffixType) {
        return wrapLeakingResource( super.tryResourceFromJarURL( state, baseName, suffixType ) );
    }
    
    @Override
    protected LoadServiceResource tryResourceFromJarURLWithLoadPath(String namePlusSuffix, String loadPathEntry) {
        return wrapLeakingResource( super.tryResourceFromJarURLWithLoadPath( namePlusSuffix, loadPathEntry ) );
    }
    
    @Override
    protected LoadServiceResource getClassPathResource(ClassLoader classLoader, String name) {
        return wrapLeakingResource( super.getClassPathResource( classLoader, name ) );
    };

    static LoadServiceResource wrapLeakingResource(LoadServiceResource resource) {
        if (resource == null) {
            return null;
        }
        try {
            return new NonLeakingLoadServiceResource( resource.getURL(), resource.getName(), resource.isAbsolute() );
        } catch (IOException e) {
            return resource;
        }
    }
}
