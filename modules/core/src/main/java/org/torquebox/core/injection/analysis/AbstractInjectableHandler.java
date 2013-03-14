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

package org.torquebox.core.injection.analysis;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

public abstract class AbstractInjectableHandler implements InjectableHandler, Service<InjectableHandler> {

    public AbstractInjectableHandler(String type) {
        this.type = type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return this.type;
    }
    
    public int getRecognitionPriority() {
        return this.recognitionPriority;
    }
    
    public void setRecognitionPriority(int priority) {
        this.recognitionPriority = priority;
    }
    
    @Override
    public AbstractInjectableHandler getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
    }

    @Override
    public void stop(StopContext context) {
        
    }

    protected String getString(Object injection) {
        return RubyInjectionUtils.getString( injection );
    }

    protected String getJavaClassName(Object injection) {
        return RubyInjectionUtils.getJavaClassName( injection );
    }
    
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger( this.getClass() );
    private String type;
    private int recognitionPriority = 0;

}
