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

package org.torquebox.core;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map;

import org.jruby.Ruby;
import org.junit.Test;


public class TorqueBoxMetaDataTest {
    
    Ruby ruby = Ruby.newInstance();

    @Test
    public void testOverlayingWebSection() {
        Map externalWeb = ruby.evalScriptlet( "{ 'web' => { 'context' => '/', 'value' => 'special' } }" ).convertToHash();
        Map internalWeb = ruby.evalScriptlet( "{ 'web' => { 'rackup' => 'foo.ru', 'value' => 'normal' } }" ).convertToHash();
        
        TorqueBoxMetaData externalMetaData = new TorqueBoxMetaData(externalWeb);
        TorqueBoxMetaData internalMetaData = new TorqueBoxMetaData(internalWeb);
        TorqueBoxMetaData mergedMetaData = externalMetaData.overlayOnto(internalMetaData);

        Map<String,String> merged = (Map<String,String>) mergedMetaData.getSection("web");
        assertEquals("/", merged.get("context"));
        assertEquals("foo.ru", merged.get("rackup"));
        assertEquals("special", merged.get("value"));
    }
    
    @Test
    public void testOverlayingDeepNests() {
        Map external = ruby.evalScriptlet( "{ 'jobs' => { 'x' => { 'job' => 'Y' } } }" ).convertToHash();
        Map internal = ruby.evalScriptlet( "{ 'jobs' => { 'x' => { 'job' => 'X', 'cron' => '0 */5 * * * ?' } } }" ).convertToHash();
        
        TorqueBoxMetaData externalMetaData = new TorqueBoxMetaData(external);
        TorqueBoxMetaData internalMetaData = new TorqueBoxMetaData(internal);
        TorqueBoxMetaData mergedMetaData = externalMetaData.overlayOnto(internalMetaData);

        Map<String,Map> merged = (Map<String,Map>) mergedMetaData.getSection("jobs");
        assertEquals("Y", merged.get("x").get("job"));
        assertEquals("0 */5 * * * ?", merged.get("x").get("cron"));
    }
    
    @Test (expected=ClassCastException.class)
    public void testUnexpectedType() {
        Map external = ruby.evalScriptlet( "{ 'foo' => { 'x' => 'y' } }" ).convertToHash();
        Map internal = ruby.evalScriptlet( "{ 'foo' => 'scalar' }" ).convertToHash();
        
        TorqueBoxMetaData externalMetaData = new TorqueBoxMetaData(external);
        TorqueBoxMetaData internalMetaData = new TorqueBoxMetaData(internal);
        TorqueBoxMetaData mergedMetaData = externalMetaData.overlayOnto(internalMetaData);
    }
    
    @Test
    public void testHomeTildeExpansion() {
        Map<String,Object> torqueboxYml = ruby.evalScriptlet( "{ 'application' => { 'root' => '~/tacos' } }" ).convertToHash();
        TorqueBoxMetaData metaData = new TorqueBoxMetaData( torqueboxYml );

        String expected = System.getProperty( "user.home" ) + "/tacos";
        // Normalize the file paths across OSes
        File expectedFile = new File( expected );
                
        assertEquals( expectedFile, metaData.getApplicationRootFile() );
        
    }
    
}
