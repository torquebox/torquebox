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
package org.torquebox.ruby.enterprise.endpoints.cxf.util;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.cxf.common.logging.AbstractDelegatingLogger;
import org.jboss.logging.Logger;

public class JBossLoggerBridge extends AbstractDelegatingLogger {

	private Logger logger;

	public JBossLoggerBridge(String name, String resourceBundleName) {
		super(name, resourceBundleName);
		this.logger = Logger.getLogger( name );
	}

	@SuppressWarnings("deprecation")
	@Override
	public Level getLevel() {
		if ( this.logger.isTraceEnabled() ) {
			return Level.FINEST;
		}
		
		if ( this.logger.isDebugEnabled() ) {
			return Level.FINE;
		}
		
		if ( this.logger.isInfoEnabled() ) {
			return Level.INFO;
		}
		
		return Level.ALL;
	}

	@Override
	protected void internalLogFormatted(String message, LogRecord logRecord) {
		Level level = logRecord.getLevel();
		
		if ( level == Level.FINEST || level == Level.FINER ) {
			logger.trace( message );
			return;
		}
		
		if ( level == Level.FINE ) {
			logger.debug( message );
			return;
		}
		
		if ( level == Level.WARNING ) {
			logger.warn( message );
			return;
		}
		
		if ( level == Level.SEVERE ) {
			logger.error( message );
			return;
		}
		
		logger.info( message );
	}

}
