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

        log.debug( "Determine structure for: " + root );

        try {
            if (isKnob( root ) || RubyApplicationRecognizer.isRubyApplication( root )) {
                log.debug( "is knob" );
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

        return (name.endsWith( ".knob" ) || name.endsWith( ".rails" ) || name.endsWith( ".rack" ));
    }

    public ContextInfo createBaseContextInfo(VirtualFile rackRoot, StructureMetaData structureMetaData) throws IOException {
        List<String> metaDataPaths = new ArrayList<String>();
        metaDataPaths.add( "" );
        metaDataPaths.add( "config" );
        metaDataPaths.add( "META-INF" );
        metaDataPaths.add( "WEB-INF" );

        List<ClassPathEntry> classPaths = getClassPathEntries( rackRoot.getChild( "lib" ), rackRoot );
        classPaths.addAll( getClassPathEntries( rackRoot.getChild( "vendor/jars" ), rackRoot ) );
        classPaths.addAll( getClassPathEntries( rackRoot.getChild( "vendor/plugins" ), rackRoot ) );

        ContextInfo context = StructureMetaDataFactory.createContextInfo( "", metaDataPaths, classPaths );
        return context;
    }

    public void addDirectoryOfJarsToClasspath(StructureContext structureContext, ContextInfo context, String dirPath) throws IOException {
        log.info( "Add dir to CLASSPATH: " + dirPath );

        VirtualFile dir = structureContext.getFile().getChild( dirPath );

        if (dir.exists() && dir.isDirectory()) {
            List<VirtualFile> children = getClassPathEntries( dir );

            for (VirtualFile jar : children) {
                log.info( "..." + jar );
                addClassPath( structureContext, jar, true, true, context );
            }
        }
    }

    public List<VirtualFile> getClassPathEntries(VirtualFile dir) throws IOException {
        return dir.getChildrenRecursively( JAR_FILTER );
    }

    public List<ClassPathEntry> getClassPathEntries(VirtualFile dir, VirtualFile relativeTo) throws IOException {
        List<ClassPathEntry> entries = new ArrayList<ClassPathEntry>();

        List<VirtualFile> files = getClassPathEntries( dir );

        for (VirtualFile file : files) {
            entries.add( StructureMetaDataFactory.createClassPathEntry( getRelativePath( relativeTo, file ) ) );
        }

        return entries;
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
