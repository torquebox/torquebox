/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.core.component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.msc.inject.InjectionException;
import org.jboss.msc.inject.Injector;
import org.jruby.Ruby;
import org.torquebox.core.injection.ConvertableRubyInjection;
import org.torquebox.core.util.RuntimeHelper;

public class InjectionRegistry {

    public InjectionRegistry() {

    }

    public Injector<Object> getInjector(final String key) {
        return new Injector<Object>() {
            @Override
            public void inject(Object value) throws InjectionException {
                //System.err.println( "INJECT: " + key + " = " + value );
                InjectionRegistry.this.injections.put( key, value );
            }

            @Override
            public void uninject() {
                InjectionRegistry.this.injections.remove( key );
            }

        };
    }

    public void merge(Ruby ruby) throws Exception {
        synchronized (ruby) {
            RuntimeHelper.invokeClassMethod( ruby, TORQUEBOX_REGISTRY_CLASS_NAME, "merge!", new Object[] { getConvertedRegistry( ruby ) } );
        }
    }

    protected Map<String, Object> getConvertedRegistry(Ruby ruby) throws Exception {
        Map<String, Object> convertedRegistry = new HashMap<String, Object>();

        for (String key : this.injections.keySet()) {
            convertedRegistry.put( key, convert( ruby, this.injections.get( key ) ) );
        }

        return convertedRegistry;
    }

    public Object getUnconverted(String key) {
        return this.injections.get( key );
    }

    protected Object convert(Ruby ruby, Object object) throws Exception {
        if (object instanceof ConvertableRubyInjection) {
            return ((ConvertableRubyInjection) object).convert( ruby );
        }
        return object;
    }

    public static final String TORQUEBOX_REGISTRY_CLASS_NAME = "TorqueBox::Registry";

    private Map<String, Object> injections = new ConcurrentHashMap<String, Object>();
}
