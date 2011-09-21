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

package org.projectodd.polyglot.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFilter;

/**
 * Deployment processor base-class that assists in locating files by name, or
 * suffix.
 * 
 * @author Toby Crawley
 */
public abstract class FileLocatingProcessor implements DeploymentUnitProcessor {

    /**
     * Search a variety of locations for a specific file, relative to the root.
     * 
     * @param root The root from which to base the search.
     * @param fileName The searched-for file name.
     * @param locations Locations, relative to the root, to check for the file.
     * @return The matching file, if found, otherwise <code>null</code>.
     */
    protected VirtualFile getFile(final VirtualFile root, final String fileName, String[] locations) {
        if (root.getName().equals( fileName )) {
            return root;
        }
        for (int i = 0; i < locations.length; i++) {
            final VirtualFile file = root.getChild( locations[i] + fileName );
            if (file.exists()) {
                return file;
            }
        }

        return null;
    }

    /**
     * Search a variety of locations for files matching a suffix.
     * 
     * <p>
     * The suffix match applies <b>only</b> to the actual filename. Given this,
     * suffixes should <b>not</b> include slashes and expect to match paths.
     * </p>
     * 
     * @param root The root from which to base the search.
     * @param suffix The searched-for file suffix.
     * @param locations Locations, relative to the root, to check for the file.
     * @return The matching files, if found, otherwise and empty list.
     */
    protected List<VirtualFile> getFilesBySuffix(final VirtualFile root, final String suffix, String[] locations) {
        List<VirtualFile> files = new ArrayList<VirtualFile>();

        for (int i = 0; i < locations.length; ++i) {
            final VirtualFile file = root.getChild( locations[i] );
            try {
                List<VirtualFile> matches = file.getChildren( new VirtualFileFilter() {
                    @Override
                    public boolean accepts(VirtualFile file) {
                        return file.getName().endsWith( suffix );
                    }
                } );
                files.addAll( matches );
            } catch (IOException e) {
                // TODO
                // ignore?
            }

        }

        return files;
    }

    /**
     * Determine if any one (or more) of several candidate paths exist under the
     * specified root.
     * 
     * @param root The root to search.
     * @param paths The candidate paths to test.
     * @return <code>true</code> if the root contains any of the paths,
     *         otherwise <code>false</code>.
     */
    public static boolean hasAnyOf(VirtualFile root, String... paths) {
        for (String path : paths) {
            if (root.getChild( path ).exists()) {
                return true;
            }
        }
        return false;
    }

}
