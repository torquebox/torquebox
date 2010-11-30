package org.torquebox.integration.arquillian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@Run(RunModeType.AS_CLIENT)
public class FrozenRails_2_3_8_Test extends AbstractIntegrationTest {

    private static final String[] GEM_NAMES = { "railties", "activesupport", "actionpack", "activerecord", "actionmailer", "activeresource", };

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment("rails/2.3.8/frozen-rails.yml");
    }

    @Test
    public void testHighLevel() {
        driver.get("http://localhost:8080/frozen-rails");
        // System.err.println("RESULT: ");
        // System.err.println(driver.getPageSource());
        WebElement element = driver.findElementById("success");
        assertNotNull(element);
        assertEquals("frozen-rails", element.getAttribute("class"));

        List<WebElement> elements = driver.findElements(By.className("load_path_element"));

        for (WebElement each : elements) {
            String pathElement = each.getText();

            // Ensure that the mentioned gems are loaded absolutely from our frozen
            // vendored Rails, and not from system gems.  Inspect the paths for
            // known elements that indicate frozenness.
            for (int i = 0; i < GEM_NAMES.length; ++i) {
                if (pathElement.contains( "/" + GEM_NAMES[i] + "/" ) ) {
                    assert (pathElement.contains("frozen/vendor/rails/" + GEM_NAMES[i] + "/lib"));
                }
            }
        }
    }

}
