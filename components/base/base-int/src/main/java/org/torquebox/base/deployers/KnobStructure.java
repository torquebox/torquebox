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
import java.util.ArrayList;
import java.util.List;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.structure.ClassPathEntry;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.spi.structure.StructureMetaDataFactory;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSArchiveStructureDeployer;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;
import org.jboss.vfs.VisitorAttributes;
import org.jboss.vfs.util.SuffixMatchFilter;
import org.jboss.vfs.util.automount.Automounter;

/**
 * <pre>
 * Stage: structure
 *    In: 
 *   Out: classpath entries and metadata locations
 * </pre>
 * 
 * StructureDeployer to identify Ruby-on-Rails applications.
 * 
 * @author Bob McWhirter
 */
public class KnobStructure extends AbstractVFSArchiveStructureDeployer {

    /**
     * Construct.
     */
    public KnobStructure() {
        setRelativeOrder( -1000 );
    }

    public boolean doDetermineStructure(StructureContext structureContext) throws DeploymentException {
        VirtualFile root = structureContext.getFile();

        try {
            if (isKnob( root ) || RubyApplicationRecognizer.isRubyApplication( root )) {
                StructureMetaData structureMetaData = structureContext.getMetaData();
                ContextInfo context = createBaseContextInfo( root, structureMetaData );
                structureMetaData.addContext( context );
                return true;
            }
        } catch (IOException e) {
            throw new DeploymentException( e );
        }

        return false;
    }

    public static boolean isKnob(VirtualFile root) {
        String name = root.getName();

        boolean result = (name.endsWith( ".knob" ) || name.endsWith( ".rails" ) || name.endsWith( ".rack" ));

        return result;

    }

    public ContextInfo createBaseContextInfo(VirtualFile rackRoot, StructureMetaData structureMetaData) throws IOException {
        List<String> metaDataPaths = new ArrayList<String>();
        metaDataPaths.add( "" );
        metaDataPaths.add( "config" );
        metaDataPaths.add( "META-INF" );
        metaDataPaths.add( "WEB-INF" );

        List<VirtualFile> allJars = new ArrayList<VirtualFile>();
        allJars.addAll( getJarFiles( rackRoot.getChild( "lib" ) ) );
        allJars.addAll( getJarFiles( rackRoot.getChild( "vendor/jars" ) ) );
        allJars.addAll( getJarFiles( rackRoot.getChild( "vendor/plugins" ) ) );

        mountFiles( allJars, rackRoot );

        List<ClassPathEntry> classPathEntries = getClassPathEntries( allJars, rackRoot );

        ContextInfo context = StructureMetaDataFactory.createContextInfo( "", metaDataPaths, classPathEntries );
        return context;
    }

    public List<VirtualFile> getJarFiles(VirtualFile dir) throws IOException {
        return dir.getChildrenRecursively( JAR_FILTER );
    }

    public List<ClassPathEntry> getClassPathEntries(List<VirtualFile> files, VirtualFile relativeTo) {
        List<ClassPathEntry> entries = new ArrayList<ClassPathEntry>();
        for (VirtualFile file : files) {
            entries.add( StructureMetaDataFactory.createClassPathEntry( getRelativePath( relativeTo, file ) ) );
        }

        return entries;
    }

    public void mountFiles(List<VirtualFile> files, VirtualFile owner) {
        for (VirtualFile file : files) {
            try {
                Automounter.mount( owner, file );
            } catch (IOException e) {
                log.warn( "Exception mounting  file " + file.getPathName(), e );
            }
        }
    }

    @Override
    protected boolean hasValidName(VirtualFile file) {
        return isKnob( file );
    }

    @Override
    protected boolean hasValidSuffix(String name) {
        return true;
    }

    public static final VirtualFileFilter JAR_FILTER = new SuffixMatchFilter( ".jar", VisitorAttributes.DEFAULT );

}
