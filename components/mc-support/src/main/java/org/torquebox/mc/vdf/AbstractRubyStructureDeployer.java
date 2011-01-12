package org.torquebox.mc.vdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.deployers.spi.structure.ClassPathEntry;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaDataFactory;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSArchiveStructureDeployer;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;
import org.jboss.vfs.VisitorAttributes;
import org.jboss.vfs.util.SuffixMatchFilter;

public abstract class AbstractRubyStructureDeployer extends AbstractVFSArchiveStructureDeployer {

    public static final VirtualFileFilter JAR_FILTER = new SuffixMatchFilter(".jar", VisitorAttributes.DEFAULT);

    public void addDirectoryOfJarsToClasspath(StructureContext structureContext, ContextInfo context, String dirPath) throws IOException {
        log.info("Add dir to CLASSPATH: " + dirPath);

        VirtualFile dir = structureContext.getFile().getChild(dirPath);

        if (dir.exists() && dir.isDirectory()) {
            List<VirtualFile> children = getClassPathEntries( dir );

            for (VirtualFile jar : children) {
                log.info("..." + jar);
                addClassPath(structureContext, jar, true, true, context);
            }
        }
    }

    public static List<VirtualFile> getClassPathEntries(VirtualFile dir) throws IOException {
        return dir.getChildrenRecursively(JAR_FILTER);
    }
    
    public static List<ClassPathEntry> getClassPathEntries(VirtualFile dir, VirtualFile relativeTo) throws IOException {
        List<ClassPathEntry> entries = new ArrayList<ClassPathEntry>();
        
        List<VirtualFile> files = getClassPathEntries(dir);
        
        for (VirtualFile file : files ) {
            entries.add( StructureMetaDataFactory.createClassPathEntry( getRelativePath( relativeTo, file ) ) );
        }
        
        return entries;
    }

}
