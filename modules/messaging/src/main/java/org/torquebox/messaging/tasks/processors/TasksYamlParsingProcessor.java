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

package org.torquebox.messaging.tasks.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.projectodd.polyglot.core.util.DeploymentUtils;
import org.torquebox.core.processors.AbstractSplitYamlParsingProcessor;
import org.torquebox.messaging.tasks.TaskMetaData;
import org.yaml.snakeyaml.Yaml;

/**
 * <pre>
 * Stage: PARSE
 *    In: messaging.yml
 *   Out: TaskMetaData
 * </pre>
 * 
 * Creates TaskMetaData instances from messaging.yml
 */
public class TasksYamlParsingProcessor extends AbstractSplitYamlParsingProcessor {

    public TasksYamlParsingProcessor() {
        setSectionName( "tasks" );
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        if (DeploymentUtils.isUnitRootless( phaseContext.getDeploymentUnit() )) {
            return;
        }
        super.deploy( phaseContext );
    }

    @Override
    public void parse(DeploymentUnit unit, Object dataObj) throws Exception {
        for (TaskMetaData metaData : Parser.parse( dataObj, unit.getAttachmentList( TaskMetaData.ATTACHMENTS_KEY ) ) ) {
            unit.addToAttachmentList( TaskMetaData.ATTACHMENTS_KEY, metaData );
        }
    }

    public static class Parser {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        static List<TaskMetaData> parse(Object data, List<? extends TaskMetaData> existingTasks) throws Exception {
            List<TaskMetaData> result = null;

            if (data instanceof String) {
                String s = (String) data;
                if (s.trim().length() == 0) {
                    result = Collections.emptyList();
                } else {
                    result = parse( new Yaml().load( (String) data ), existingTasks );
                }
            } else if (data instanceof Map) {
                result = parseTasks( (Map<String, Map>)data, existingTasks );
            } 
            
            return result;
        }

        @SuppressWarnings("rawtypes")
        static List<TaskMetaData> parseTasks( Map<String, Map>tasks, List<? extends TaskMetaData> existingTasks) {
            List<TaskMetaData> result = new ArrayList<TaskMetaData>();

            for (String rubyClassName :  tasks.keySet()) {
                TaskMetaData task = existingTaskMetaData( rubyClassName, existingTasks );

                Map options = tasks.get( rubyClassName );

                if (task == null) {
                    task = new TaskMetaData();
                    task.setRubyClassName( rubyClassName );
                    result.add( task ); 
                }

                if (options != null) {
                    task.setConcurrency( (Integer)options.get( "concurrency") );
                    task.setDurable( (Boolean)options.get( "durable" ) );
                }
            }
            
            return result;
        }
        
        static TaskMetaData existingTaskMetaData(String rubyClassName, List<? extends TaskMetaData> existingTasks) {
            for (TaskMetaData each : existingTasks) {
                if (rubyClassName.equals( each.getSimpleName() )) {
                    return each;
                }
            }
             
            return null;
        }

    }
}
