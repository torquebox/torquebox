/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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

package org.torquebox.base.deployers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.base.metadata.TorqueBoxMetaData;
import org.yaml.snakeyaml.Yaml;

public class TorqueBoxYamlParsingDeployer extends AbstractParsingDeployer {

    public TorqueBoxYamlParsingDeployer() {
        addOutput( TorqueBoxMetaData.class );
        setRelativeOrder( -5000 );
    }

    @Override
    protected void deploy(VFSDeploymentUnit unit) throws DeploymentException {
        VirtualFile file = getMetaDataFile( unit, "torquebox.yml" );

        if (file == null) {
            return;
        }
        try {
            TorqueBoxMetaData metaData = parse( file );
            unit.addAttachment( TorqueBoxMetaData.class, metaData );
        } catch (Exception e) {
            throw new DeploymentException( e );
        }
    }

    @SuppressWarnings("unchecked")
    static TorqueBoxMetaData parse(VirtualFile file) throws IOException {

        Yaml yaml = new Yaml();
        InputStream in = null;
        try {
            in = file.openStream();
            Map<String, Object> data = (Map<String, Object>) yaml.load( in );
            if (data == null) {
                data = new HashMap<String, Object>();
            }
            return new TorqueBoxMetaData( data );
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

}
