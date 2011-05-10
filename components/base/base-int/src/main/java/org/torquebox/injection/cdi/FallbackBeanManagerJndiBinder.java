package org.torquebox.injection.cdi;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.DeploymentUnitVisitor;
import org.jboss.naming.Util;
import org.jboss.reloaded.naming.service.NameSpaces;
import org.jboss.weld.integration.deployer.DeployersUtils;
import org.jboss.weld.integration.util.IdFactory;
import org.jboss.weld.integration.util.JndiUtils;
import org.torquebox.core.app.RubyApplicationMetaData;

/** Binds the CDI BeanManager in cases where an application is not recognized as a JavaEE app.
 * 
 * @author Bob McWhirter
 */
public class FallbackBeanManagerJndiBinder {
    
    /** Attachment key determining if this action should be inhibited. */
    private static final String INHIBIT = FallbackBeanManagerJndiBinder.class.getName() + "$inhibit";
    
    /** Context into which to bind the beanManager. */
    private Context beanManagerContext;
    
    /** Global namespaces. */
    private NameSpaces nameSpaces;

    public void bind(DeploymentUnit unit) throws DeploymentException {
        if (DeployersUtils.checkForWeldFilesAcrossDeployment( unit )) {
            unit.visit( new BinderVisitor( getBeanManagerContext() ) );
        }
    }
    
    public void setNameSpaces(NameSpaces nameSpaces) {
        this.nameSpaces = nameSpaces;
    }
    
    public NameSpaces getNameSpaces() {
        return this.nameSpaces;
    }
    
    public Context getBeanManagerContext() {
        return this.beanManagerContext;
    }
    
    public void create() throws NamingException {
        this.beanManagerContext = (Context) getNameSpaces().getGlobalContext().lookup( JndiUtils.BEAN_MANAGER_GLOBAL_SUBCONTEXT );
    }

    public void unbind(DeploymentUnit unit) {
        BeanMetaData bbBMD = unit.getTopLevel().getAttachment( DeployersUtils.getBootstrapBeanAttachmentName( unit ), BeanMetaData.class );

        if (bbBMD != null) {
            try {
                unit.visit( new UnbinderVisitor( getBeanManagerContext() ) );
            } catch (DeploymentException e) {
                throw new RuntimeException( e );
            }
        }

        // TODO: cleanup the remaining subcontexts if any (e.g. EAR/WAR etc)
    }

    private class BinderVisitor implements DeploymentUnitVisitor {
        private final Context rootContext;

        private BinderVisitor(Context rootContext) {
            this.rootContext = rootContext;
        }

        public void visit(DeploymentUnit unit) throws DeploymentException {
            try {
                RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.class );
                if (rubyAppMetaData != null) {
                    String path = rubyAppMetaData.getApplicationName() + "/" + rubyAppMetaData.getApplicationName();
                    Context subcontext = Util.createSubcontext( rootContext, path );
                    Reference reference = new Reference( BeanManager.class.getName(), "org.jboss.weld.integration.deployer.jndi.JBossBeanManagerObjectFactory", null );
                    reference.add( new StringRefAddr( "id", IdFactory.getIdFromClassLoader( unit.getClassLoader() ) ) );
                    subcontext.bind( "BeanManager", reference );
                }
            } catch (NameAlreadyBoundException e) {
                unit.addAttachment( INHIBIT, Boolean.TRUE );
            } catch (NamingException e) {
                throw new DeploymentException( e );
            }
        }

        public void error(DeploymentUnit unit) {
            // do nothing
        }
    }

    private class UnbinderVisitor implements DeploymentUnitVisitor {
        private final Context rootContext;

        private UnbinderVisitor(Context rootContext) {
            this.rootContext = rootContext;
        }

        public void visit(DeploymentUnit unit) throws DeploymentException {
            if ( unit.isAttachmentPresent( INHIBIT) ) {
                return;
            }
            try {
                RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.class );
                if (rubyAppMetaData != null) {
                    String path = rubyAppMetaData.getApplicationName() + "/" + rubyAppMetaData.getApplicationName();
                    Context subcontext = (Context) rootContext.lookup( path );
                    subcontext.unbind( "BeanManager" );
                    rootContext.destroySubcontext( path );
                }
            } catch (NamingException e) {
                throw new DeploymentException( e );
            }
        }

        public void error(DeploymentUnit unit) {
            // do nothing
        }
    }

}
