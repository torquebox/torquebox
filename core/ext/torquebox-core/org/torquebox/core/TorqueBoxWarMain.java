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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;

public class TorqueBoxWarMain {

    public static void main(String[] args) throws Throwable {
        TorqueBoxWarMain torqueBoxWarMain = new TorqueBoxWarMain();
        try {
            torqueBoxWarMain.run(args);
        } catch (Exception ex) {
            checkForSystemExit(ex);
            throw ex;
        }
    }

    public void run(String[] args) throws Throwable {
        String extractRoot = null;
        try {
            extractRoot = Files.createTempDirectory("wunderboss").toFile().getAbsolutePath();
            File jarFile = extractJar(extractRoot);
            URLClassLoader classLoader = new URLClassLoader(new URL[] {jarFile.toURI().toURL()});
            Class<?> clazz = classLoader.loadClass("org.projectodd.wunderboss.ApplicationRunner");
            Object runner = clazz.getConstructor(String.class).newInstance("application");
            try {
                clazz.getDeclaredMethod("start", String[].class).invoke(runner, new Object[]{args});
            } catch (InvocationTargetException ex) {
                throw ex.getCause();
            }
        } finally {
            if (extractRoot != null) {
                deleteRecursively(new File(extractRoot));
            }
        }
    }

    private File extractJar(String extractRoot) throws IOException {
        String jarName = "app.jar"; // hardcoded name in war.rb
        byte[] buffer = new byte[4096];
        InputStream jarInputStream = Thread.currentThread().getContextClassLoader().
                getResourceAsStream("WEB-INF/lib/" + jarName);
        File file = new File(extractRoot + "/" + jarName);
        FileOutputStream extractStream = new FileOutputStream(file);
        try {
            int bytesRead = -1;
            while ((bytesRead = jarInputStream.read(buffer)) != -1) {
                extractStream.write(buffer, 0, bytesRead);
            }
        } finally {
            extractStream.close();
        }
        return file;
    }

    public static void deleteRecursively(File directory) {
        if (directory.isDirectory()) {
            File[] children = directory.listFiles();
            if (children != null) {
                for (File file : children) {
                    deleteRecursively(file);
                }
            }
        }
        directory.delete();
    }

    /*
     * Catch any RubySystemExit exceptions and propagate the exit
     * status to System.exit using reflection because JRuby libs
     * are not visible at this point.
     * Any changes to this need to be copied to the same method in
     * TorqueBoxMain.java
     */
    public static void checkForSystemExit(Exception ex) throws Exception {
        if (ex.getClass().getName().equals("org.jruby.exceptions.RaiseException")) {
            Object rubyException = ex.getClass().getDeclaredMethod("getException").invoke(ex);
            if (rubyException.getClass().getName().equals("org.jruby.RubySystemExit")) {
                Object rubyStatus = rubyException.getClass().
                        getDeclaredMethod("status").invoke(rubyException);
                int status = 0;
                if (rubyStatus != null) {
                    Long longStatus = (Long) rubyStatus.getClass().
                            getDeclaredMethod("getLongValue").invoke(rubyStatus);
                    status = longStatus.intValue();
                }
                System.exit(status);
            }
        }
    }
}
