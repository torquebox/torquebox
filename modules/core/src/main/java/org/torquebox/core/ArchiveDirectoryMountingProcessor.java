package org.torquebox.core;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.torquebox.core.app.RubyApplicationMetaData;

public class ArchiveDirectoryMountingProcessor implements DeploymentUnitProcessor {

    private final AttachmentKey<AttachmentList<Closeable>> CLOSEABLE_ATTACHMENTS_KEY = AttachmentKey.createList( Closeable.class );

    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();

        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.ATTACHMENT_KEY );

        if (rubyAppMetaData == null) {
            return;
        }

        if (rubyAppMetaData.isArchive()) {
            try {
                mountDir( unit, rubyAppMetaData.getRoot(), "log", System.getProperty( "jboss.server.log.dir" ) + "/" + rubyAppMetaData.getApplicationName() );
                mountDir( unit, rubyAppMetaData.getRoot(), "tmp", System.getProperty( "jboss.server.temp.dir" ) + "/rails/" + rubyAppMetaData.getApplicationName() );
            } catch (Exception e) {
                throw new DeploymentUnitProcessingException( e );
            }
        }
    }

    public void undeploy(DeploymentUnit unit) {

        List<Closeable> mounts = unit.getAttachmentList( CLOSEABLE_ATTACHMENTS_KEY );

        for (Closeable eachMount : mounts) {
            try {
                eachMount.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    protected void mountDir(DeploymentUnit unit, VirtualFile root, String name, String path) throws IOException {
        VirtualFile logical = root.getChild( name );
        File physical = new File( path );
        physical.mkdirs();
        Closeable mount = VFS.mountReal( physical, logical );
        unit.addToAttachmentList( CLOSEABLE_ATTACHMENTS_KEY, mount );
    }
}
