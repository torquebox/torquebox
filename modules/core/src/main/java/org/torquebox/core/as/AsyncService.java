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

package org.torquebox.core.as;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;

/**
 * AsyncService is a simple wrapper class for any
 * org.jboss.msc.service.Service implementation that needs to start
 * asynchronously and execute blocking tasks inside its start method.
 * Just extend AsyncService<T> and execute the blocking start code
 * inside the startAsync method. Any exceptions thrown will automatically
 * be wrapped as org.jboss.msc.service.StartException instances.
 * 
 * @author bbrowning
 *
 */
public abstract class AsyncService<T> implements Service<T> {

    @Override
    public void start(final StartContext context) throws StartException {
        context.asynchronous();
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    startAsync(context);
                    context.complete();
                } catch (Exception e) {
                    context.failed( new StartException( e ) );
                }
            }
        });
    }

    public abstract void startAsync(final StartContext context) throws Exception;

    private static ExecutorService threadPool = Executors.newCachedThreadPool();
}
