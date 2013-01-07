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

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that handles the method calls for a {@link Proxy} that
 * implements an annotation.
 * 
 * @author Stuart Douglas
 * @see AnnotationInstanceProvider
 */
class AnnotationInvocationHandler implements InvocationHandler, Serializable
{
   
   private static class SerializationProxy
   {
      private final Map<String, ?> valueMap;
      private final Class<? extends Annotation> annotationType;
      
      private SerializationProxy(Map<String, ?> valueMap, Class<? extends Annotation> annotationType)
      {
         this.valueMap = valueMap;
         this.annotationType = annotationType;
      }
      
      private Object readResolve() throws ObjectStreamException
      {
         return new AnnotationInvocationHandler(valueMap, annotationType);
      }
      
   }

   private static final long serialVersionUID = 4801508041776645033L;

   private final Map<String, Object> valueMap;

   private final Class<? extends Annotation> annotationType;

   private final Method[] members;

   AnnotationInvocationHandler(Map<String, ?> values, Class<? extends Annotation> annotationType)
   {
      this.valueMap = new HashMap<String, Object>();
      valueMap.putAll(values);
      this.annotationType = annotationType;
      this.members = annotationType.getDeclaredMethods();
      for (Method m : members)
      {
         Object value = valueMap.get(m.getName());
         if (value == null)
         {
            value = m.getDefaultValue();
            if (value == null)
            {
               throw new NullMemberException(annotationType, m, "Error creating annotation @" + annotationType.getName() + " member " + m.getName() + " was null and does not provide a default value");
            } else
            {
               valueMap.put(m.getName(), value);
            }
         }
      }
   }
   
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      if (method.getName().equals("equals"))
      {
         return equals(args[0]);
      }
      else if (method.getName().equals("hashCode"))
      {
         return hashCode();
      }
      else if (method.getName().equals("toString"))
      {
         return toString();
      }
      else if (method.getName().equals("annotationType"))
      {
         return annotationType;
      }
      else
      {
         Object val = valueMap.get(method.getName());
         Class<?> r = method.getReturnType();
         val = performTypeCoercion(val, r);
         return val;
      }

   }

   private Object performTypeCoercion(Object val, Class<?> type)
   {
      if (Integer.class.isAssignableFrom(type) || type == int.class)
      {
         return ((Number) val).intValue();
      }
      else if (Long.class.isAssignableFrom(type) || type == long.class)
      {
         return ((Number) val).longValue();
      }
      else if (Short.class.isAssignableFrom(type) || type == short.class)
      {
         return ((Number) val).shortValue();
      }
      else if (Byte.class.isAssignableFrom(type) || type == byte.class)
      {
         return ((Number) val).byteValue();
      }
      else if (Double.class.isAssignableFrom(type) || type == double.class)
      {
         return ((Number) val).doubleValue();
      }
      else if (Float.class.isAssignableFrom(type) || type == float.class)
      {
         return ((Number) val).floatValue();
      }
      else if (Character.class.isAssignableFrom(type) || type == char.class)
      {
         if (String.class.isAssignableFrom(val.getClass()))
         {
            return val.toString().charAt(0);
         }
      }
      return val;
   }

   @Override
   public String toString()
   {
      StringBuilder string = new StringBuilder();
      string.append('@').append(annotationType.getName()).append('(');
      for (int i = 0; i < members.length; i++)
      {
         string.append(members[i].getName()).append('=');
         Object value = performTypeCoercion(valueMap.get(members[i].getName()), members[i].getReturnType());
         if (value instanceof boolean[])
         {
            appendInBraces(string, Arrays.toString((boolean[]) value));
         }
         else if (value instanceof byte[])
         {
            appendInBraces(string, Arrays.toString((byte[]) value));
         }
         else if (value instanceof short[])
         {
            appendInBraces(string, Arrays.toString((short[]) value));
         }
         else if (value instanceof int[])
         {
            appendInBraces(string, Arrays.toString((int[]) value));
         }
         else if (value instanceof long[])
         {
            appendInBraces(string, Arrays.toString((long[]) value));
         }
         else if (value instanceof float[])
         {
            appendInBraces(string, Arrays.toString((float[]) value));
         }
         else if (value instanceof double[])
         {
            appendInBraces(string, Arrays.toString((double[]) value));
         }
         else if (value instanceof char[])
         {
            appendInBraces(string, Arrays.toString((char[]) value));
         }
         else if (value instanceof String[])
         {
            String[] strings = (String[]) value;
            String[] quoted = new String[strings.length];
            for (int j = 0; j < strings.length; j++)
            {
               quoted[j] = "\"" + strings[j] + "\"";
            }
            appendInBraces(string, Arrays.toString(quoted));
         }
         else if (value instanceof Class<?>[])
         {
            Class<?>[] classes = (Class<?>[]) value;
            String[] names = new String[classes.length];
            for (int j = 0; j < classes.length; j++)
            {
               names[j] = classes[j].getName() + ".class";
            }
            appendInBraces(string, Arrays.toString(names));
         }
         else if (value instanceof Object[])
         {
            appendInBraces(string, Arrays.toString((Object[]) value));
         }
         else if (value instanceof String)
         {
            string.append('"').append(value).append('"');
         }
         else if (value instanceof Class<?>)
         {
            string.append(((Class<?>) value).getName()).append(".class");
         }
         else
         {
            string.append(value);
         }
         if (i < members.length - 1)
         {
            string.append(", ");
         }
      }
      return string.append(')').toString();
   }

   private void appendInBraces(StringBuilder buf, String s)
   {
      buf.append('{').append(s.substring(1, s.length() - 1)).append('}');
   }

   @Override
   public boolean equals(Object other)
   {
      if (other instanceof Annotation)
      {
         Annotation that = (Annotation) other;
         if (this.annotationType.equals(that.annotationType()))
         {
            for (Method member : members)
            {
               Object thisValue = performTypeCoercion(valueMap.get(member.getName()), member.getReturnType());
               Object thatValue = invoke(member, that);
               if (thisValue instanceof byte[] && thatValue instanceof byte[])
               {
                  if (!Arrays.equals((byte[]) thisValue, (byte[]) thatValue))
                     return false;
               }
               else if (thisValue instanceof short[] && thatValue instanceof short[])
               {
                  if (!Arrays.equals((short[]) thisValue, (short[]) thatValue))
                     return false;
               }
               else if (thisValue instanceof int[] && thatValue instanceof int[])
               {
                  if (!Arrays.equals((int[]) thisValue, (int[]) thatValue))
                     return false;
               }
               else if (thisValue instanceof long[] && thatValue instanceof long[])
               {
                  if (!Arrays.equals((long[]) thisValue, (long[]) thatValue))
                     return false;
               }
               else if (thisValue instanceof float[] && thatValue instanceof float[])
               {
                  if (!Arrays.equals((float[]) thisValue, (float[]) thatValue))
                     return false;
               }
               else if (thisValue instanceof double[] && thatValue instanceof double[])
               {
                  if (!Arrays.equals((double[]) thisValue, (double[]) thatValue))
                     return false;
               }
               else if (thisValue instanceof char[] && thatValue instanceof char[])
               {
                  if (!Arrays.equals((char[]) thisValue, (char[]) thatValue))
                     return false;
               }
               else if (thisValue instanceof boolean[] && thatValue instanceof boolean[])
               {
                  if (!Arrays.equals((boolean[]) thisValue, (boolean[]) thatValue))
                     return false;
               }
               else if (thisValue instanceof Object[] && thatValue instanceof Object[])
               {
                  if (!Arrays.equals((Object[]) thisValue, (Object[]) thatValue))
                     return false;
               }
               else
               {
                  if (!thisValue.equals(thatValue))
                  {
                     return false;
                  }
               }
            }
            return true;
         }
      }
      return false;
   }

   @Override
   public int hashCode()
   {
      int hashCode = 0;
      for (Method member : members)
      {
         int memberNameHashCode = 127 * member.getName().hashCode();
         Object value = performTypeCoercion(valueMap.get(member.getName()), member.getReturnType());
         int memberValueHashCode;
         if (value instanceof boolean[])
         {
            memberValueHashCode = Arrays.hashCode((boolean[]) value);
         }
         else if (value instanceof short[])
         {
            memberValueHashCode = Arrays.hashCode((short[]) value);
         }
         else if (value instanceof int[])
         {
            memberValueHashCode = Arrays.hashCode((int[]) value);
         }
         else if (value instanceof long[])
         {
            memberValueHashCode = Arrays.hashCode((long[]) value);
         }
         else if (value instanceof float[])
         {
            memberValueHashCode = Arrays.hashCode((float[]) value);
         }
         else if (value instanceof double[])
         {
            memberValueHashCode = Arrays.hashCode((double[]) value);
         }
         else if (value instanceof byte[])
         {
            memberValueHashCode = Arrays.hashCode((byte[]) value);
         }
         else if (value instanceof char[])
         {
            memberValueHashCode = Arrays.hashCode((char[]) value);
         }
         else if (value instanceof Object[])
         {
            memberValueHashCode = Arrays.hashCode((Object[]) value);
         }
         else
         {
            memberValueHashCode = value.hashCode();
         }
         hashCode += memberNameHashCode ^ memberValueHashCode;
      }
      return hashCode;
   }
   
   private Object writeReplace() throws ObjectStreamException
   {
      return new SerializationProxy(valueMap, annotationType);
   }
   
   private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      throw new UnsupportedOperationException("Must use SerializationProxy");
   }

   private static Object invoke(Method method, Object instance)
   {
      try
      {
         if (!method.isAccessible())
            method.setAccessible(true);
         return method.invoke(instance);
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException("Error checking value of member method " + method.getName() + " on " + method.getDeclaringClass(), e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException("Error checking value of member method " + method.getName() + " on " + method.getDeclaringClass(), e);
      }
      catch (InvocationTargetException e)
      {
         throw new RuntimeException("Error checking value of member method " + method.getName() + " on " + method.getDeclaringClass(), e);
      }
   }
}
