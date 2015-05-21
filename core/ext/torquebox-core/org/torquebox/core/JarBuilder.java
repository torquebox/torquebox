/*
* Copyright 2014 Red Hat, Inc, and individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.torquebox.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarBuilder {

    public JarBuilder addManifestAttribute(String name, String value) {
        manifestAttributes.put(new Attributes.Name(name), value);
        return this;
    }

    public JarBuilder addFile(String name, String path) {
        if (new File(path).isDirectory()) {
            if (!name.endsWith("/")) {
                name = name + "/";
            }
        }
        addEntry(name, path);
        return this;
    }

    public JarBuilder addString(String name, String value) throws Exception {
        addEntry(name, new ByteArrayInputStream(value.getBytes("UTF-8")));
        return this;
    }

    public JarBuilder addResource(String name, String resource) {
        InputStream value = Thread.currentThread().getContextClassLoader().
                getResourceAsStream(resource);
        addEntry(name, value);
        return this;
    }

    public JarBuilder shadeJar(String jar, String... exclusions) throws Exception {
        List<String> exclusionList = Arrays.asList(exclusions);
        JarInputStream jarInput = new JarInputStream(new FileInputStream(jar));
        try {
            Manifest manifest = jarInput.getManifest();
            if (manifest != null) {
                for (Map.Entry<Object, Object> attribute : manifest.getMainAttributes().entrySet()) {
                    Attributes.Name key = (Attributes.Name) attribute.getKey();
                    String value = (String) attribute.getValue();
                    // First entry for a given attribute wins
                    if (!manifestAttributes.containsKey(key)) {
                        manifestAttributes.put(key, value);
                    }
                }
            }
            JarEntry jarEntry = jarInput.getNextJarEntry();
            while (jarEntry != null) {
                if (!exclusionList.contains(jarEntry.getName())) {
                    addEntry(jarEntry.getName(), jar + "!/" + jarEntry.getName());
                }
                jarInput.closeEntry();
                jarEntry = jarInput.getNextJarEntry();
            }
        } finally {
            jarInput.close();
        }
        return this;
    }

    public void create(String outputPath) throws Exception {
        Manifest manifest = new Manifest();
        if (!manifestAttributes.containsKey(Attributes.Name.MANIFEST_VERSION)) {
            manifestAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        }
        for (Map.Entry<Attributes.Name, String> entry : manifestAttributes.entrySet()) {
            manifest.getMainAttributes().put(entry.getKey(), entry.getValue());
        }
        FileOutputStream fileStream = new FileOutputStream(new File(outputPath));
        JarOutputStream jarStream = null;
        try {
            jarStream = new JarOutputStream(fileStream, manifest);
            // shadeJars(jarStream);
            for (Map.Entry<String, Object> entry: entries.entrySet()) {
                writeEntry(jarStream, entry.getKey(), entry.getValue());
            }
        } finally {
            if (jarStream != null) {
                jarStream.close();
            }
            for (JarInputStream jarInput : jarCache.values()) {
                jarInput.close();
            }
        }
    }

    public boolean hasEntry(String name) {
        return this.entries.containsKey(name);
    }

    @SuppressWarnings("unchecked")
    protected void addEntry(String name, Object value) {
        if (entries.containsKey(name)) {
            Object entryValue = entries.get(name);
            if (entryValue instanceof List) {
                ((List) entryValue).add(value);
            } else {
                List<Object> entryList = new ArrayList<>();
                entryList.add(entryValue);
                entryList.add(value);
                entries.put(name, entryList);
            }
        } else {
            entries.put(name, value);
        }
    }

    protected void writeEntry(JarOutputStream jarOutput, String name, Object value) throws IOException {
        jarOutput.putNextEntry(new JarEntry(name));
        if (!name.endsWith("/")) {
            writeCurrentEntry(jarOutput, name, value);
        }
        jarOutput.closeEntry();
    }

    protected void writeCurrentEntry(JarOutputStream jarOutput, String name, Object value) throws IOException {
        if (value instanceof String) {
            writeCurrentEntry(jarOutput, name, (String) value);
        } else if (value instanceof InputStream) {
            writeCurrentEntry(jarOutput, (InputStream) value);
        } else if (value instanceof List) {
            writeCurrentEntry(jarOutput, name, (List) value);
        } else {
            throw new RuntimeException("Unknown Jar entry type - " + value.getClass() + " - this should never happen");
        }
    }

    protected void writeCurrentEntry(JarOutputStream jarOutput, String name, String value) throws IOException {
        if (value.contains("!/")) {
            String jarPath = value.substring(0, value.indexOf("!/"));
            writeShadedEntry(jarOutput, name, jarPath);
        } else {
            try {
                FileInputStream fileStream = new FileInputStream(value);
                try {
                    writeCurrentEntry(jarOutput, fileStream);
                } finally {
                    fileStream.close();
                }
            } catch (FileNotFoundException ex) {
                System.err.println("Omitting file '" + value + "' from the archive - it could not be read.");
            }
        }
    }

    protected void writeCurrentEntry(JarOutputStream jarOutput, InputStream value) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = value.read(buffer)) != -1) {
            jarOutput.write(buffer, 0, bytesRead);
        }
    }

    protected void writeCurrentEntry(JarOutputStream jarOutput, String name, List value) throws IOException {
        for (Object part : value) {
            writeCurrentEntry(jarOutput, name, part);
            if (name.endsWith(".properties")) {
                // duplicate .properties entries are joined by concatenating them together
                writeCurrentEntry(jarOutput, new ByteArrayInputStream("\n".getBytes("UTF-8")));
            } else {
                // for everything else, first entry added wins
                break;
            }
        }
    }

    protected void writeShadedEntry(JarOutputStream jarOutput, String name, String jarPath) throws IOException {
        writeShadedEntry(jarOutput, name, jarPath, false);
    }

    protected void writeShadedEntry(JarOutputStream jarOutput, String name, String jarPath, boolean reopened) throws IOException {
        JarInputStream jarInput = jarCache.get(jarPath);
        if (jarInput == null) {
            jarInput = new JarInputStream(new FileInputStream(jarPath));
            jarCache.put(jarPath, jarInput);
        }
        JarEntry jarEntry = jarInput.getNextJarEntry();
        while (jarEntry != null && !jarEntry.getName().equals(name)) {
            jarInput.closeEntry();
            jarEntry = jarInput.getNextJarEntry();
        }
        if (jarEntry != null) {
            writeCurrentEntry(jarOutput, jarInput);
            jarInput.closeEntry();
        } else {
            if (!reopened) {
                jarInput.close();
                jarCache.remove(jarPath);
                writeShadedEntry(jarOutput, name, jarPath, true);
            } else {
                throw new RuntimeException("Unable to locate entry " + name + " in jar " + jarPath + " - this should never happen");
            }
        }
    }

    private Map<Attributes.Name, String> manifestAttributes = new LinkedHashMap<>();
    private Map<String, Object> entries = new LinkedHashMap<>();
    private Map<String, JarInputStream> jarCache = new HashMap<>();
}
