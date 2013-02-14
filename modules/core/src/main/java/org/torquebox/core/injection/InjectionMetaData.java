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

package org.torquebox.core.injection;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.DeploymentUnit;

import java.util.Arrays;
import java.util.List;

public class InjectionMetaData {

    // The default list with injection-enabled directories
    private static final String[] DEFAULT_INJECTION_PATHS = {"app", "lib", "models", "helpers"};

    private boolean enabled = true;

    private List<String> paths = Arrays.asList(DEFAULT_INJECTION_PATHS);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public static final AttachmentKey<InjectionMetaData> ATTACHMENT_KEY = AttachmentKey.create(InjectionMetaData.class);

    public static boolean injectionIsEnabled(DeploymentUnit unit) {
        InjectionMetaData injectionMetaData = unit.getAttachment(ATTACHMENT_KEY);
        return (injectionMetaData == null || injectionMetaData.isEnabled());
    }

    public static List<String> injectionPaths(DeploymentUnit unit) {
        InjectionMetaData injectionMetaData = unit.getAttachment(ATTACHMENT_KEY);

        if (injectionMetaData == null)
            return Arrays.asList(DEFAULT_INJECTION_PATHS);
        else
            return injectionMetaData.getPaths();
    }
}
