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
package org.torquebox.rack.deployers;

import java.util.List;
import java.util.Map;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.deployers.AbstractSplitYamlParsingDeployer;
import org.torquebox.rack.metadata.RackApplicationMetaData;

/**
 * <pre>
 * Stage: PARSE
 *    In: web.yml
 *   Out: RackApplicationMetaData
 * </pre>
 * 
 * Internal deployment descriptor for setting vhosts, web context, and static
 * content dir
 * 
 */
public class WebYamlParsingDeployer extends AbstractSplitYamlParsingDeployer {

    public WebYamlParsingDeployer() {
        setSectionName("web");
        addInput(RackApplicationMetaData.class);
        addOutput(RackApplicationMetaData.class);
    }

    @SuppressWarnings("unchecked")
    public void parse(VFSDeploymentUnit unit, Object dataObj) throws Exception {

        log.debug("Deploying web configuration: " + unit);
        RackApplicationMetaData rackMetaData = unit.getAttachment(RackApplicationMetaData.class);

        if (rackMetaData == null) {
            log.debug("Initializing web configuration: " + unit);
            rackMetaData = new RackApplicationMetaData();
            unit.addAttachment(RackApplicationMetaData.class, rackMetaData);
        }

        Map<String, Object> web = (Map<String, Object>) dataObj;

        rackMetaData.setContextPath((String) web.get("context"));
        rackMetaData.setStaticPathPrefix((String) web.get("static"));

        if (web.get("rackup") != null) {
            rackMetaData.setRackUpScriptLocation((String) web.get("rackup"));
        }

        Object hosts = web.get("host");

        if (hosts instanceof List) {
            List<String> list = (List<String>) hosts;
            for (String each : list) {
                rackMetaData.addHost(each);
            }
        } else {
            rackMetaData.addHost((String) hosts);
        }

        log.debug("Configured as: " + rackMetaData);
    }

}
