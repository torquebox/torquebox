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

package org.torquebox.messaging.deployers;

import java.util.Map;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.deployers.AbstractSplitYamlParsingDeployer;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.TopicMetaData;

/**
 * <pre>
 * Stage: PARSE
 *    In: topics.yml
 *   Out: TopicMetaData
 * </pre>
 * 
 * Creates TopicMetaData instances from topics.yml
 */
public class TopicsYamlParsingDeployer extends AbstractSplitYamlParsingDeployer {

    public TopicsYamlParsingDeployer() {
        setSectionName( "topics" );
        setSupportsSuffix( true );
        addOutput( TopicMetaData.class );
    }

    @SuppressWarnings("unchecked")
    public void parse(VFSDeploymentUnit unit, Object baseData) throws Exception {
        Map<String, Map<String, Object>> data = (Map<String, Map<String, Object>>) baseData;

        for (String topicName : data.keySet()) {
            TopicMetaData topicMetaData = new TopicMetaData( topicName );
            AttachmentUtils.multipleAttach( unit, topicMetaData, topicName );
        }
    }

}
