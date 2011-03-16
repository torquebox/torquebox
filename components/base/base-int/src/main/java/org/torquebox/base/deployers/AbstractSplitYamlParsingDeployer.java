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
import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.vfs.VirtualFile;
import org.torquebox.base.metadata.TorqueBoxMetaData;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * Abstract deployer base-class supporting <code>torquebox.yml</code> sectional
 * parsing.
 * 
 * <p>
 * For a given subsystem 'foo', a torquebox.yml section named 'foo:' can
 * configure it or optionally (deprecated) a file named foo.yml.
 * </p>
 * 
 * @author Bob McWhirter
 */
public abstract class AbstractSplitYamlParsingDeployer extends AbstractParsingDeployer {

    /** Name of the section within torquebox.yml. */
    private String sectionName;

    /** Opotional fine-name for NAME.yml parsing separate from torquebox.yml. */
    private String fileName;

    /** Does this deployer support a standalone *.yml descriptor? */
    private boolean supportsStandalone = true;
    
    public AbstractSplitYamlParsingDeployer() {
        addInput( TorqueBoxMetaData.class );
    }

    public String getSectionName() {
        return this.sectionName;
    }

    public void setSupportsStandalone(boolean supports) {
        this.supportsStandalone = supports;
    }

    public boolean isSupportsStandalone() {
        return this.supportsStandalone;
    }
    
    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getFileName() {
        if (this.fileName != null) {
            return this.fileName;
        }

        return getSectionName() + ".yml";
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if (!(unit instanceof VFSDeploymentUnit)) {
            throw new DeploymentException( "Only supports VFS deployments" );
        }

        try {
            deploy( (VFSDeploymentUnit) unit );
        } catch (Exception e) {
            throw new DeploymentException( e );
        }
    }

    @SuppressWarnings("unchecked")
    public void deploy(VFSDeploymentUnit unit) throws DeploymentException {
        TorqueBoxMetaData globalMetaData = unit.getAttachment( TorqueBoxMetaData.class );

        log.debug( "Global torquebox.yml: " + globalMetaData );

        Object data = null;

        if (globalMetaData != null) {
            data = globalMetaData.getSection( getSectionName() );
            log.debug( "Global data section for " + getSectionName() + ": " + data );
        }

        if (data == null && isSupportsStandalone()) {
            VirtualFile metaDataFile = getMetaDataFile( unit, getFileName() );

            if ((metaDataFile != null) && metaDataFile.exists()) {
                if ( ! metaDataFile.equals( unit.getRoot() ) ) {
                    log.warn( "Usage of " + getFileName() + " is deprecated.  Please use torquebox.yml." );
                }
                InputStream in = null;
                try {
                    in = metaDataFile.openStream();
                    Yaml yaml = new Yaml();
                    data = (Map<String, ?>) yaml.load( in );
                } catch (YAMLException e) {
                    log.warn( "Error parsing: " + metaDataFile + ": " + e.getMessage() );
                    data = null;
                } catch (IOException e) {
                    throw new DeploymentException( e );
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            throw new DeploymentException( e );
                        }
                    }
                }
            }
        }

        if (data == null) {
            return;
        }

        try {
            parse( unit, data );
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException( e );
        }
    }

    protected String getOneOf(Map<String, String> map, String... keys) {
        for (String each : keys) {
            for (String key : map.keySet()) {
                if (each.equalsIgnoreCase( key )) {
                    return map.get( key );
                }
            }
        }
        return null;
    }

    public abstract void parse(VFSDeploymentUnit unit, Object data) throws Exception;

}
