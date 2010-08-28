package org.torquebox.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebElement;

@Run(RunModeType.AS_CLIENT)
public class BasicSinatra_1_0_Test extends AbstractIntegrationTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return createDeployment("sinatra/1.0/basic-sinatra-rack.yml");
	}

	@Test
	public void testHighlevel() {
		driver.get("http://localhost:8080/basic-sinatra");
		WebElement element = driver.findElementById("success");
		assertNotNull(element);
		assertEquals("sinatra-basic", element.getAttribute("class"));
	}

	@Test
	public void testRequestMapping() {
		driver.get("http://localhost:8080/basic-sinatra/request-mapping");
		WebElement element = driver.findElementById("success");
		assertNotNull(element);

		WebElement scheme = driver.findElementById("scheme");
		assertNotNull(element);
		assertEquals("http", scheme.getText().trim());

	}

	// ** Currently ignoring this known-failing test.
	//@Ignore
	@Test
	public void testPostAlot() throws Exception {
		int numIterations = 500;
		for (int i = 0; i < numIterations; ++i) {
			if (i % 10 == 0) {
				System.err.print(".");
			}
			try {
				postCycle();
			} catch (Exception e) {
				System.err.println( "\n\nFailed at iteration " + i + ": " + e.getMessage() + "\n");
				throw e;
			}
		}
		System.err.println( "\nCompleted " + numIterations );
	}

	protected void postCycle() {
		driver.get("http://localhost:8080/basic-sinatra/poster");
		WebElement submit = driver.findElementById("submit");
		submit.click();
		WebElement success = driver.findElementById("success");
		assertNotNull(success);
		assertEquals("posted!", success.getText());
	}

}
