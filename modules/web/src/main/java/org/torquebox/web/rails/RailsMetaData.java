/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.web.rails;

import java.util.regex.Pattern;

import org.jboss.as.server.deployment.AttachmentKey;

public class RailsMetaData {
    
    public static final AttachmentKey<RailsMetaData> ATTACHMENT_KEY = AttachmentKey.create(RailsMetaData.class);

    public RailsMetaData() {
    }

    public void setVersionSpec(String versionSpec) {
        this.versionSpec = versionSpec;
    }

    public String getVersionSpec() {
        return this.versionSpec;
    }

    public boolean isRails2() {
        return getVersionSpec() != null && Pattern.matches( ".*2\\.[0-9]+\\.[0-9]+\\.*", getVersionSpec() );
    }

    public boolean isRails3() {
        return !isRails2();
    }

    public String toString() {
        return "[RailsApplicationMetaData:\n  version=" + versionSpec + "]";
    }

    private String versionSpec;
}
