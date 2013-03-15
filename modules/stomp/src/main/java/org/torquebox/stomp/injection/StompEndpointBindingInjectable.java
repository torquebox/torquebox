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

package org.torquebox.stomp.injection;

import org.jboss.as.server.deployment.AttachmentList;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.torquebox.core.injection.SimpleNamedInjectable;
import org.torquebox.stomp.RubyStompletMetaData;
import org.torquebox.stomp.as.StompServices;

public class StompEndpointBindingInjectable extends SimpleNamedInjectable {

    public static final StompEndpointBindingInjectable INSTANCE = new StompEndpointBindingInjectable( false );
    public static final StompEndpointBindingInjectable SECURE_INSTANCE = new StompEndpointBindingInjectable( true );

    public StompEndpointBindingInjectable(boolean secure) {
        super(
                (secure ? "stomp-endpoint-secure" : "stomp-endpoint"),
                (secure ? "stomp-endpoint-secure" : "stomp-endpoint"),
                false,
                true );
        this.secure = secure;
    }

    @Override
    public ServiceName getServiceName(ServiceTarget serviceTarget, DeploymentUnit unit) throws Exception {
        AttachmentList<RubyStompletMetaData> stomplets = unit.getAttachment( RubyStompletMetaData.ATTACHMENTS_KEY );

        if (stomplets == null || stomplets.isEmpty()) {
            return null;
        }

        return StompServices.endpointBinding( unit, secure );
    }

    private boolean secure;

}
