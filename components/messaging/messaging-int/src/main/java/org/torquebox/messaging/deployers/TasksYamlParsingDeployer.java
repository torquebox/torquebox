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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.torquebox.base.deployers.AbstractSplitYamlParsingDeployer;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.messaging.metadata.TaskMetaData;
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
public class TasksYamlParsingDeployer extends AbstractSplitYamlParsingDeployer {

    public TasksYamlParsingDeployer() {
        addInput( TaskMetaData.class );
        addOutput( TaskMetaData.class );
        setSectionName( "tasks" );
    }

    @Override
    public void parse(VFSDeploymentUnit unit, Object dataObj) throws Exception {
        for (TaskMetaData metadata : Parser.parse( dataObj, unit.getAllMetaData( TaskMetaData.class ) )) {
            AttachmentUtils.multipleAttach( unit, metadata, metadata.getName() );
        }
    }

    public static class Parser {

        static List<TaskMetaData> parse(Object data, Set<? extends TaskMetaData> existingTasks) throws Exception {
            List<TaskMetaData> result = null;

            if (data instanceof String) {
                String s = (String) data;
                if (s.trim().length() == 0) {
                    result = Collections.EMPTY_LIST;
                } else {
                    result = parse( new Yaml().load( (String) data ), existingTasks );
                }
            } else if (data instanceof Map) {
                result = parseTasks( (Map<String, Map>)data, existingTasks );
            } 
            return result;
        }

        static List<TaskMetaData> parseTasks( Map<String, Map>tasks, Set existingTasks) {
            List<TaskMetaData> result = new ArrayList<TaskMetaData>();

            for (String rubyClassName :  tasks.keySet()) {
                result.add( createTaskMetaData( rubyClassName, tasks.get( rubyClassName ), existingTasks ) );
            }
            
            return result;
        }

        static TaskMetaData createTaskMetaData(String rubyClassName, Map options, Set<? extends TaskMetaData> existingTasks) {
            if (options == null)
                options = Collections.EMPTY_MAP;

            TaskMetaData task = null;

            for (TaskMetaData each : existingTasks) {
                if (rubyClassName.equals( each.getSimpleName() )) {
                    task = each; 
                }
            }
            if (task == null) {
                task = new TaskMetaData();
                task.setRubyClassName( rubyClassName );
            }
            
            task.setConcurrency( (Integer)options.get( "concurrency") );

            return task;
        }

    }
}
