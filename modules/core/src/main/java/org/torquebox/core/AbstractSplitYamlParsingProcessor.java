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

package org.torquebox.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;

import org.torquebox.core.util.DeprecationUtil;

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
public abstract class AbstractSplitYamlParsingProcessor extends AbstractParsingProcessor {

    /** Name of the section within torquebox.yml. */
    private String sectionName;

    /** Optional file-name for NAME.yml parsing separate from torquebox.yml. */
    private String fileName;

    /** Does this deployer support a standalone *.yml descriptor? */
    private boolean supportsStandalone = true;

    /** Does this deploy support a *-<name>.yml format? */
    private boolean supportsSuffix = false;

    public AbstractSplitYamlParsingProcessor() {
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

    public void setSupportsSuffix(boolean supports) {
        this.supportsSuffix = supports;
    }

    public boolean isSupportsSuffix() {
        return this.supportsSuffix;
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

    @SuppressWarnings("unchecked")
    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        ResourceRoot resourceRoot = unit.getAttachment( Attachments.DEPLOYMENT_ROOT );
        VirtualFile root = resourceRoot.getRoot();
        TorqueBoxMetaData globalMetaData = unit.getAttachment( TorqueBoxMetaData.ATTACHMENT_KEY );

        Object data = null;

        if (globalMetaData != null) {
            data = globalMetaData.getSection( getSectionName() );
        }

        if (data == null && isSupportsStandalone()) {
            VirtualFile metaDataFile = getMetaDataFile( root, getFileName() );

            if ((metaDataFile == null || !metaDataFile.exists()) && this.supportsSuffix) {
                List<VirtualFile> matches = getMetaDataFileBySuffix( root, "-" + getFileName() );
                if (!matches.isEmpty()) {
                    if (matches.size() > 1) {
                        log.warn( "Multiple matches: " + matches );
                    }
                    metaDataFile = matches.get( 0 );
                }
            }

            if ((metaDataFile != null) && metaDataFile.exists()) {
                if (!metaDataFile.equals( root )) {
                    DeprecationUtil.log( log, "Usage of " + getFileName() + " is deprecated.  Please use torquebox.yml." );
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
                    throw new DeploymentUnitProcessingException( e );
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            throw new DeploymentUnitProcessingException( e );
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
        } catch (DeploymentUnitProcessingException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DeploymentUnitProcessingException( e );
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

    protected abstract void parse(DeploymentUnit unit, Object data) throws Exception;

    private static final Logger log = Logger.getLogger( "org.torquebox.core" );

}
