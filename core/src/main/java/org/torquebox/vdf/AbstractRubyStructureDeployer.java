package org.torquebox.vdf;

import java.io.IOException;
import java.util.List;

import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.vfs.plugins.structure.AbstractVFSArchiveStructureDeployer;
import org.jboss.deployers.vfs.spi.structure.StructureContext;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;
import org.jboss.vfs.VisitorAttributes;
import org.jboss.vfs.util.SuffixMatchFilter;

public abstract class AbstractRubyStructureDeployer extends AbstractVFSArchiveStructureDeployer {

	public static final VirtualFileFilter JAR_FILTER = new SuffixMatchFilter(".jar", VisitorAttributes.DEFAULT);

	public void addDirectoryOfJarsToClasspath(StructureContext structureContext, ContextInfo context, String dirPath) throws IOException {

		VirtualFile dir = VFS.getChild(dirPath);

		if (dir.exists() && dir.isDirectory()) {
			List<VirtualFile> children = dir.getChildrenRecursively(JAR_FILTER);

			for (VirtualFile jar : children) {
				addClassPath(structureContext, jar, true, true, context);
			}
		}
	}

}
