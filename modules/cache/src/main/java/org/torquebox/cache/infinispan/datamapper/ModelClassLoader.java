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

package org.torquebox.cache.infinispan.datamapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ModelClassLoader extends ClassLoader {

    private ClassLoader classloader;
    private Map<String, Class<?>> map = new HashMap<String, Class<?>>();

    public ModelClassLoader(ClassLoader cl){
        this.classloader = cl;
    }
    
    public void register(Class<?> clazz){
        map.put(clazz.getName(), clazz);
    }

    public void clear(){
        map.clear();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if(map.containsKey(name)){
            return map.get(name);
        }
        return classloader.loadClass(name);
    }

    public void clearAssertionStatus() {
        classloader.clearAssertionStatus();
    }

    public boolean equals(Object obj) {
        return classloader.equals(obj);
    }

    public URL getResource(String name) {
        return classloader.getResource(name);
    }

    public InputStream getResourceAsStream(String name) {
        return classloader.getResourceAsStream(name);
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        return classloader.getResources(name);
    }

    public int hashCode() {
        return classloader.hashCode();
    }

    public void setClassAssertionStatus(String className, boolean enabled) {
        classloader.setClassAssertionStatus(className, enabled);
    }

    public void setDefaultAssertionStatus(boolean enabled) {
        classloader.setDefaultAssertionStatus(enabled);
    }

    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        classloader.setPackageAssertionStatus(packageName, enabled);
    }

    public String toString() {
        return classloader.toString();
    }
}
