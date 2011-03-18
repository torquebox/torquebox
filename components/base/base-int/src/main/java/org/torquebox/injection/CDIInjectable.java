package org.torquebox.injection;

import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.torquebox.mc.CDIKernelRegistryPlugin;

public class CDIInjectable extends SimpleNamedInjectable {

    public CDIInjectable(String name) {
        this( "cdi", name );
    }

    public CDIInjectable(String type, String name) {
        super( type, name );
    }

    @Override
    public ValueMetaData createMicrocontainerInjection(DeploymentUnit context, BeanMetaDataBuilder builder) {
        return builder.createInject( CDIKernelRegistryPlugin.CDI_DEPENDENCY_PREFIX + getName() );
    }

    @Override
    public String getKey() {
        String fullName = getName();

        int lastDot = fullName.lastIndexOf( '.' );

        String className = null;
        String packageName = null;

        if (lastDot > 0) {
            className = fullName.substring( lastDot + 1 );
            packageName = fullName.substring( 0, lastDot );
        } else {
            className = fullName;
        }
        return "Java::" + getPackageKeyPart( packageName ) + "::" + className;

    }

    public String getPackageKeyPart(String packageName) {
        if ( packageName == null ) {
            return "Default";
        }
        
        int length = packageName.length();

        StringBuilder buf = new StringBuilder();
        
        for (int start = 0, offset = 0; start < length; start = offset + 1) {
            if ((offset = packageName.indexOf( '.', start )) == -1) {
                offset = length;
            }
            buf.append( Character.toUpperCase( packageName.charAt( start ) ) ).append( packageName.substring( start + 1, offset ) );
        }
        return buf.toString();
    }

}
