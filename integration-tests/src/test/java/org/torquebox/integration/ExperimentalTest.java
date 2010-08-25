package org.torquebox.integration;

import java.net.URL;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Run(RunModeType.AS_CLIENT)
public class ExperimentalTest {

	@Before
	public void setUp() {
		System.err.println("setUp()");
	}

	@After
	public void tearDown() {
		System.err.println("tearDown()");
	}
	
	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive archive = ShrinkWrap.create( JavaArchive.class, "experimental.jar" );
		ClassLoader classLoader = ExperimentalTest.class.getClassLoader();
		URL appRailsYml = classLoader.getResource( "org/torquebox/integration/basic-rails.yml" );
		archive.addResource( appRailsYml, "/META-INF/basic-rails.yml" );
		System.err.println( "DEPLOYING " + archive );
		return archive;
	}

	@Test
	public void testSomething() {
		System.err.println("testSomething()");
	}

}
