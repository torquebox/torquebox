/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.impl;

import java.net.URL;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.jboss.arquillian.spi.ApplicationArchiveGenerator;
import org.jboss.arquillian.spi.TestClass;

import org.jruby.Ruby;
import org.jruby.RubyObject;
import org.jruby.RubyClass;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.javasupport.JavaEmbedUtils;

/**
 * Use this class by naming it in src/test/resources/META-INF/services/org.jboss.arquillian.spi.ApplicationArchiveGenerator 
 */
public class RSpecArchiveGenerator implements ApplicationArchiveGenerator {
    
    public Archive<?> generateApplicationArchive(TestClass testCase) {
        System.out.println("JC: generateApplicationArchive for "+testCase.getName());
        Validate.notNull(testCase, "TestCase must be specified");
        return createDeployment("rack/1.1.0/basic-rack.yml");
    }

	public JavaArchive createDeployment(String name) {
        System.out.println("JC: createDeployment");
		String tail = name.substring( name.lastIndexOf( '/' ) + 1 );
		String base = tail.substring(0, tail.lastIndexOf( '.' ) );
		
		JavaArchive archive = ShrinkWrap.create( JavaArchive.class, base + ".jar" );
		ClassLoader classLoader = getClass().getClassLoader();
		URL deploymentDescriptorUrl = classLoader.getResource( name );
		archive.addResource( deploymentDescriptorUrl, "/META-INF/" + tail );
		return archive;
	}

    private ApplicationArchiveGenerator delegate = new DeploymentAnnotationArchiveGenerator();
}
