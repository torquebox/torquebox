/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.torquebox.rack.core;

import org.jboss.beans.metadata.api.annotations.Create;
import org.jruby.Ruby;
import org.torquebox.interp.spi.RubyRuntimeFactory;
import org.torquebox.rack.spi.RackApplication;
import org.torquebox.rack.spi.RackApplicationFactory;

public class RubyRackApplicationFactory implements RackApplicationFactory {
	
	private RubyRuntimeFactory runtimeFactory;
	private String rackUpScript;

	public RubyRackApplicationFactory() {
		
	}
	
	public void setRubyRuntimeFactory(RubyRuntimeFactory runtimeFactory) {
		this.runtimeFactory = runtimeFactory;
	}
	
	public RubyRuntimeFactory getRubyRuntimeFactory() {
		return this.runtimeFactory;
	}
	
	public void setRackUpScript(String rackUpScript) {
		this.rackUpScript = rackUpScript;
	}
	
	public String getRackUpScript() {
		return this.rackUpScript;
	}
	
	@Create(ignored=true)
	public RackApplication create() throws Exception {
		Ruby ruby = getRubyRuntimeFactory().create();
		
		RubyRackApplication rackApp = new RubyRackApplication( ruby, rackUpScript );
		
		return rackApp;
	}

}
