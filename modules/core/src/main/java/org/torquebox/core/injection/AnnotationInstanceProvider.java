/*
 * Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

package org.torquebox.core.injection;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 * Provides dynamic annotation creation. If you know the type of the annotation
 * instance to create at development time you should use
 * {@link AnnotationLiteral} instead of {@link AnnotationInstanceProvider}.
 * </p>
 * 
 * <p>
 * {@link AnnotationInstanceProvider} creates a proxy, and will return values as
 * provided when the annotation instance was created. An
 * {@link AnnotationInstanceProvider} will cache generated proxy classes.
 * </p>
 * 
 * @author Stuart Douglas
 * @author Pete Muir
 * 
 * @see AnnotationLiteral
 * 
 */
public class AnnotationInstanceProvider
{

   private final ConcurrentMap<Class<?>, Class<?>> cache;

   public AnnotationInstanceProvider()
   {
      cache = new ConcurrentHashMap<Class<?>, Class<?>>();
   }

   /**
    * <p>
    * Returns an instance of the given annotation type with attribute values
    * specified in the map.
    * </p>
    * 
    * <ul>
    * <li>
    * For {@link Annotation}, array and enum types the values must exactly match
    * the declared return type of the attribute or a {@link ClassCastException}
    * will result.</li>
    * 
    * <li>
    * For character types the the value must be an instance of {@link Character}
    * or {@link String}.</li>
    * 
    * <li>
    * Numeric types do not have to match exactly, as they are converted using
    * {@link Number}.</li>
    * </ul>
    * 
    * <p>
    * If am member does not have a corresponding entry in the value map then the
    * annotations default value will be used.
    * </p>
    * 
    * <p>
    * If the annotation member does not have a default value then a
    * NullMemberException will be thrown
    * </p>
    * 
    * @param annotationType the type of the annotation instance to generate
    * @param values the attribute values of this annotation
    */
   public <T extends Annotation> T get(Class<T> annotationType, Map<String, ?> values)
   {
      if (annotationType == null)
      {
         throw new IllegalArgumentException("Must specify an annotation");
      }
      Class<?> clazz = cache.get(annotationType);
      // Not safe against data race, but doesn't matter, we can recompute and
      // get the same value
      if (clazz == null)
      {
         // create the proxy class
         clazz = Proxy.getProxyClass(annotationType.getClassLoader(), annotationType, Serializable.class);
         cache.put(annotationType, clazz);
      }
      AnnotationInvocationHandler handler = new AnnotationInvocationHandler(values, annotationType);
      // create a new instance by obtaining the constructor via relection
      try
      {
         return annotationType.cast(clazz.getConstructor(new Class[] { InvocationHandler.class }).newInstance(new Object[] { handler }));
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalStateException("Error instantiating proxy for annotation. Annotation type: " + annotationType, e);
      }
      catch (InstantiationException e)
      {
         throw new IllegalStateException("Error instantiating proxy for annotation. Annotation type: " + annotationType, e);
      }
      catch (IllegalAccessException e)
      {
         throw new IllegalStateException("Error instantiating proxy for annotation. Annotation type: " + annotationType, e);
      }
      catch (InvocationTargetException e)
      {
         throw new IllegalStateException("Error instantiating proxy for annotation. Annotation type: " + annotationType, e.getCause());
      }
      catch (SecurityException e)
      {
         throw new IllegalStateException("Error accessing proxy constructor for annotation. Annotation type: " + annotationType, e);
      }
      catch (NoSuchMethodException e)
      {
         throw new IllegalStateException("Error accessing proxy constructor for annotation. Annotation type: " + annotationType, e);
      }
   }
}
