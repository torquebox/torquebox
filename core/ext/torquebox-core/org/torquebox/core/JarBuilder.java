package org.torquebox.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarBuilder {

    public static void create(String outputPath, Map<String, String> entries) throws Exception {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, TorqueBoxMain.class.getName());
        FileOutputStream fileStream = new FileOutputStream(new File(outputPath));
        JarOutputStream jarStream = null;
        try {
            jarStream = new JarOutputStream(fileStream, manifest);
            copyMainClass(jarStream, TorqueBoxMain.class);
            for (Map.Entry<String, String> entry: entries.entrySet()) {
                addEntry(jarStream, entry.getKey(), entry.getValue());
            }
        } finally {
            if (jarStream != null) {
                jarStream.close();
            }
        }
    }

    protected static void copyMainClass(JarOutputStream jarStream, Class<?> clazz) throws IOException {
        String clazzPath = clazz.getName().replace(".", "/") + ".class";
        InputStream clazzResource = clazz.getClassLoader().getResourceAsStream(clazzPath);
        addEntry(jarStream, clazzPath, clazzResource);
        // Now load any anonymous classes defined in this class
        for (int i = 1; i < 100; i++) {
            clazzPath = clazz.getName().replace(".", "/") + "$" + i + ".class";
            clazzResource = clazz.getClassLoader().getResourceAsStream(clazzPath);
            if (clazzResource == null) {
                break;
            }
            addEntry(jarStream, clazzPath, clazzResource);
        }
    }

    protected static void addEntry(JarOutputStream jarStream, String name, String value) throws IOException {
        File file = new File(value);
        if (file.isDirectory()) {
            jarStream.putNextEntry(new JarEntry(name + "/"));
        } else {
            addEntry(jarStream, name, new FileInputStream(file));
        }
    }

    protected static void addEntry(JarOutputStream jarStream, String name, InputStream value) throws IOException {
        try {
            jarStream.putNextEntry(new JarEntry(name));
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = value.read(buffer)) != -1) {
                jarStream.write(buffer, 0, bytesRead);
            }
        } finally {
            value.close();
        }
    }
}
