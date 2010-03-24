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
package org.torquebox.ruby.enterprise.crypto.deployers;

import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.jruby.util.ByteList;
import org.jvyamlb.YAML;
import org.torquebox.ruby.enterprise.crypto.metadata.CryptoMetaData;
import org.torquebox.ruby.enterprise.crypto.metadata.CryptoStoreMetaData;

public class CryptoYamlParsingDeployer extends AbstractVFSParsingDeployer<CryptoMetaData>{

	private String storeBasePath;

	public CryptoYamlParsingDeployer() {
		super(CryptoMetaData.class);
		setName( "crypto.yml" );
	}
	
	public void setStoreBasePath(String storeBasePath) {
		if ( storeBasePath.endsWith( "/") ) {
			storeBasePath = storeBasePath.substring( 0, storeBasePath.length() - 1 );
		}
		this.storeBasePath = storeBasePath;
	}
	
	public String getStoreBasePath() {
		return this.storeBasePath;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected CryptoMetaData parse(VFSDeploymentUnit unit, VirtualFile file, CryptoMetaData root) throws Exception {
		Map<ByteList,Map<ByteList,ByteList>> crypto = (Map<ByteList, Map<ByteList,ByteList>>) YAML.load( file.openStream() );
		
		CryptoMetaData metaData = new CryptoMetaData();
		
		for ( ByteList name : crypto.keySet() ) {
			CryptoStoreMetaData storeMetaData = new CryptoStoreMetaData();
			Map<ByteList, ByteList> store = crypto.get( name );
			storeMetaData.setName( name.toString() );
			String storePath = store.get( ByteList.create( "store" ) ).toString();
			if ( ! storePath.toString().startsWith( "/" )  ) {
				if ( this.storeBasePath == null || this.storeBasePath.equals( "" ) ) {
					throw new DeploymentException( "Relative store specified (" + storePath + ") but storeBasePath not set on CryptoYamlParsingDeployer" );
				}
				storePath = this.storeBasePath + "/" + storePath;
				
			}
			storeMetaData.setStore( storePath );
			storeMetaData.setPassword( store.get( ByteList.create( "password" ) ).toString() );
			metaData.addCryptoStoreMetaData( storeMetaData );
		}
		
		return metaData;
	}

}
