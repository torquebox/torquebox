package org.torquebox.integration.arquillian.override;

import java.util.zip.ZipFile;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;


@Run(RunModeType.AS_CLIENT)
public class ArchivedWithoutDescriptorTest extends ArchivedInternalTest {

	@Deployment
	public static JavaArchive createDeployment() throws Exception {
        ZipFile app = new ZipFile( System.getProperty("user.dir") + "/apps/sinatra/1.0/override.rack" );
        return ShrinkWrap.create(ZipImporter.class, "indescribable.rack")
            .importZip(app)
            .as(JavaArchive.class);
	}

    public ArchivedWithoutDescriptorTest() {
        home = "/indescribable.rack";
    }

}
