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

package org.torquebox.integration.arquillian.override;

import java.util.zip.ZipFile;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;

@Ignore
@Run(RunModeType.AS_CLIENT)
public class ArchivedWithoutDescriptorTest extends ArchivedInternalTest {

    @Deployment
    public static JavaArchive createDeployment() throws Exception {
        ZipFile app = new ZipFile( System.getProperty( "user.dir" ) + "/apps/sinatra/override.knob" );
        return ShrinkWrap.create( ZipImporter.class, "indescribable.knob" ).importZip( app ).as( JavaArchive.class );
    }

    public ArchivedWithoutDescriptorTest() {
        home = "/indescribable.knob";
    }

}
