/*
 * Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

package org.torquebox.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.as.server.deployment.AttachmentKey;
import org.projectodd.yaml.Schema;
import org.projectodd.yaml.SchemaException;
import org.torquebox.core.app.processors.ApplicationYamlParsingProcessor;
import org.torquebox.core.app.processors.RubyYamlParsingProcessor;
import org.torquebox.core.pool.processors.PoolingYamlParsingProcessor;
import org.torquebox.core.processors.TorqueBoxYamlParsingProcessor;

/**
 * Generalized opaque holder of <code>torquebox.yml</code>-specified metadata.
 * 
 * <p>
 * Once <code>torquebox.yml</code> has been parsed, each top-level section and
 * its associated un-casted value block are added to the TorqueBoxMetaData for
 * use by other, more-specific deployment processors.
 * </p>
 * 
 * @see TorqueBoxYamlParsingProcessor
 * @see RubyYamlParsingProcessor
 * @see ApplicationYamlParsingProcessor
 * @see PoolingYamlParsingProcessor
 */
public class TorqueBoxMetaData {

    public static final AttachmentKey<TorqueBoxMetaData> ATTACHMENT_KEY = AttachmentKey.create( TorqueBoxMetaData.class );

    private static Schema schema;

    static {
        try {
            schema = new Schema( TorqueBoxMetaData.class.getResourceAsStream( "/org/torquebox/schema.yml" ), false );
        } catch (SchemaException e) {
            throw new RuntimeException( e );
        }
    }

    private Map<String, Object> data;

    /**
     * Construct with parsed YAML results.
     * 
     * @param data The data, keyed by section name.
     */
    public TorqueBoxMetaData(Map<String, Object> data) {
        this.data = normalizeSectionNames( data );
    }

    /**
     * Normalize section names, since some drift has occurred.
     * 
     * @param data
     * @return
     */
    private Map<String, Object> normalizeSectionNames(Map<String, Object> data) {
        Map<String, Object> normalized = new HashMap<String, Object>();

        Set<String> keys = data.keySet();

        for (String key : keys) {
            normalized.put( normalizeSectionName( key ), data.get( key ) );
        }
        return normalized;
    }

    private String normalizeSectionName(String name) {
        if (name.equalsIgnoreCase( "app" )) {
            return "application";
        }

        return name.toLowerCase();
    }

    public Object getSection(String name) {
        return this.data.get( normalizeSectionName( name ) );
    }
    
    @SuppressWarnings("unchecked")
    public String getApplicationRoot() {
        return findApplicationRoot( (Map<String, String>) getSection( "application" ) );
    }

    public static String findApplicationRoot(Map<String, String> section) {
        if (section != null) {
            return getOneOf( section, "root", "RAILS_ROOT", "RACK_ROOT" );
        }
        return null;
    }

    public static File findApplicationRootFile(Map<String, String> section) {
        String path = findApplicationRoot( section );

        if (path == null) {
            return null;
        }

        if (path.startsWith( "~" )) {
            path = System.getProperty( "user.home" ) + path.substring( 1 );
        }
        return new File( path );
    }

    @SuppressWarnings("unchecked")
    public File getApplicationRootFile() {
        return findApplicationRootFile( (Map<String, String>) getSection( "application" ) );
    }

    @SuppressWarnings("unchecked")
    public String getApplicationEnvironment() {
        return findApplicationEnvironment( (Map<String, String>) getSection( "application" ) );
    }

    public static String findApplicationEnvironment(Map<String, String> section) {
        if (section != null) {
            return getOneOf( section, "env", "RAILS_ENV", "RACK_ENV" );
        }
        return null;
    }

    protected static String getOneOf(Map<String, String> map, String... keys) {
        for (String each : keys) {
            for (String key : map.keySet()) {
                if (each.equalsIgnoreCase( key )) {
                    return map.get( key );
                }
            }
        }
        return null;
    }

    protected String determineEnvironmentKey(Map<String, String> section) {
        if (section.containsKey( "RAILS_ENV" )) {
            return "RAILS_ENV";
        }

        if (section.containsKey( "RACK_ENV" )) {
            return "RACK_ENV";
        }

        return "env";
    }

    protected String determineRootKey(Map<String, String> section) {
        if (section.containsKey( "RAILS_ROOT" )) {
            return "RAILS_ROOT";
        }

        if (section.containsKey( "RACK_ROOT" )) {
            return "RACK_ROOT";
        }

        return "root";
    }

    @SuppressWarnings("unchecked")
    public TorqueBoxMetaData overlayOnto(TorqueBoxMetaData baseMetaData) {
        Map<String, Object> thisData = this.data;
        Map<String, Object> baseData = baseMetaData.data;

        Map<String, Object> mergedData = new HashMap<String, Object>();
        mergedData.putAll( baseData );

        for (String key : thisData.keySet()) {
            if (key.equals( "application" )) {
                // From the application: section, only overly
                // env/RACK_ENV/RAILS_ENV and do it smartly using
                // whatever key(s) are in use by the base and overlay
                // data maps.

                Map<String, String> thisAppSection = (Map<String, String>) thisData.get( "application" );
                String envKey = determineEnvironmentKey( thisAppSection );
                String envName = thisAppSection.get( envKey );

                if (envName != null && !envName.trim().equals( "" )) {
                    Map<String, String> mergedAppSection = (Map<String, String>) mergedData.get( "application" );

                    if (mergedAppSection == null) {
                        mergedAppSection = new HashMap<String, String>();
                        mergedData.put( "application", mergedAppSection );
                    }

                    envKey = determineEnvironmentKey( mergedAppSection );
                    mergedAppSection.put( envKey, envName );
                }

            } else {
                mergedData.put( key, merge( key, thisData, baseData ) );
            }
        }

        return new TorqueBoxMetaData( mergedData );
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Object merge(Object key, Map src, Map tgt) {
        Object value = src.get( key );
        if (value instanceof Map && tgt != null) {
            Map source = (Map) value;
            Object target = tgt.get( key );
            if (target != null) {
                Map result = new HashMap( (Map) target );
                for (Object k : source.keySet()) {
                    result.put( k, merge( k, source, result ) );
                }
                return result;
            }
        }
        return value;
    }

    public void validate() throws SchemaException {
        schema.validate( this.data, false );
    }

    public String toString() {
        return "[TorqueBoxMetaData: data=" + this.data + "]";
    }

	public Object getTorqueBoxInit() {
		return getSection("torquebox_init");
	}
}
