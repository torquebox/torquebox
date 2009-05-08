/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.torquebox.ruby.core.runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jruby.Ruby;
import org.torquebox.ruby.core.runtime.spi.RubyRuntimeFactory;

public class DefaultRubyRuntimePool extends AbstractRubyRuntimePool {

	private List<Ruby> instances = new ArrayList<Ruby>();
	private Set<Ruby> availableInstances = new HashSet<Ruby>();
	private int minInstances = 0;
	private int maxInstances = -1;
	private int timeout = 30;

	public DefaultRubyRuntimePool(RubyRuntimeFactory factory) {
		super(factory);
	}

	public void setMinInstances(int minInstances) {
		this.minInstances = minInstances;
	}

	public void setMaxInstances(int maxInstances) {
		this.maxInstances = maxInstances;
	}
	
	/** Time-out to fetch an instance.
	 * 
	 * @param timeout Time-out length, in seconds.
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public synchronized void start() throws Exception {
		prepopulateRuntimes();
	}

	private void prepopulateRuntimes() throws Exception {
		for (int i = 0; i < this.minInstances; ++i) {
			Ruby ruby = factory.createRubyRuntime();
			this.instances.add( ruby );
			this.availableInstances.add( ruby );
		}
	}

	public void stop() {
		this.instances.clear();
	}

	public synchronized Ruby borrowRuntime() throws Exception {
		if (availableInstances.isEmpty()) {
			if (this.maxInstances < 0 || this.instances.size() < this.maxInstances) {
				Ruby runtime = factory.createRubyRuntime();
				this.instances.add(runtime);
				return runtime;
			}
			if (this.instances.size() >= this.maxInstances) {
				while (availableInstances.isEmpty()) {
					wait( this.timeout * 1000 );
				}
			}
		}

		Iterator<Ruby> iterator = availableInstances.iterator();
		Ruby ruby = iterator.next();
		iterator.remove();
		return ruby;
	}

	public synchronized void returnRuntime(Ruby ruby) {
		this.availableInstances.add( ruby );
		notifyAll();
	}

}
