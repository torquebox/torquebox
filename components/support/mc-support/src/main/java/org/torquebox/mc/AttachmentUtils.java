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

package org.torquebox.mc;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;

public class AttachmentUtils {
    
    private static final AtomicInteger factoryCounter = new AtomicInteger();
    
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( AttachmentUtils.class );

    public static void attach(DeploymentUnit unit, BeanMetaData bmd) {
        String attachmentName = BeanMetaData.class.getName() + "$" + bmd.getName();
        unit.addAttachment( attachmentName, bmd, BeanMetaData.class );
    }
    
    public static void attach(DeploymentUnit unit, BeanMetaDataFactory factory) {
        String attachmentName = BeanMetaDataFactory.class.getName() + "$" + factoryCounter.getAndIncrement();
        unit.addAttachment( attachmentName, factory, BeanMetaDataFactory.class );
    }

    public static void multipleAttach(DeploymentUnit unit, Object metaData, String name) {
        unit.addAttachment( metaData.getClass().getName() + '$' + name, metaData );
    }

    public static <T> T getAttachment(DeploymentUnit unit, String name, Class<T> expectedType) {
        return unit.getAttachment( expectedType.getName() + '$' + name, expectedType );
    }

    public static String beanName(DeploymentUnit unit, Class<?> beanClass) {
        return beanName( unit, beanClass, null );
    }

    public static String beanName(DeploymentUnit unit, Class<?> beanClass, String name) {
        String simpleClassName = beanClass.getName();
        int lastDot = simpleClassName.lastIndexOf( '.' );

        if (lastDot > 0) {
            simpleClassName = simpleClassName.substring( lastDot + 1 );
        }

        return beanName( unit, simpleClassName, name );
    }

    public static String beanName(DeploymentUnit unit, String category, String name) {
        String unitName = unit.getSimpleName();
        if ( unitName.endsWith( ".trq" ) ) {
            unitName = unitName.substring( 0, unitName.length() - 4 );
        }
        if (name == null) {
            return "torquebox." + unitName + "." + category;
        } else {
            return "torquebox." + unitName + "." + category + "." + name;
        }
    }
    
}
