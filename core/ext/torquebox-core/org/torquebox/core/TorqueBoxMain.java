package org.torquebox.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TorqueBoxMain {

    public static void main(String[] args) throws Throwable {
        TorqueBoxMain torqueBoxMain = new TorqueBoxMain();
        torqueBoxMain.run(args);
    }

    public void run(String[] args) throws Throwable {
        URLClassLoader wrappingLoader = null;
        final String root = extractJar();
        // JRuby's System.exit calls will bypass our finally block
        // so register a shutdown hook to be sure we clean up
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                deleteRecursively(new File(root));
            }
        });
        try {
            List<URL> urlList = new ArrayList<>();
            String jrubyHome = locateJRubyHome(root);
            File jrubyLib = new File(jrubyHome + "/lib");
            if (jrubyLib.isDirectory()) {
                System.setProperty("jruby.home", jrubyHome);
                for (File each : jrubyLib.listFiles()) {
                    if (each.getName().endsWith(".jar")) {
                        urlList.add(each.toURI().toURL());
                    }
                }
            } else {
                // perhaps jruby is on the classpath already
                try {
                    Class.forName("org.jruby.Main");
                } catch (ClassNotFoundException ignored) {
                    throw new RuntimeException("Unable to locate JRuby. Include JRuby in the jar or set -Djruby.home or $JRUBY_HOME");
                }
            }

            wrappingLoader = URLClassLoader.newInstance(urlList.toArray(new URL[urlList.size()]));
            try {
                Thread.currentThread().setContextClassLoader(wrappingLoader);
                Class <?> jrubyMainClass = Class.forName("org.jruby.Main", true, wrappingLoader);
                Method jrubyMainMethod = jrubyMainClass.getDeclaredMethod("main", String[].class);
                jrubyMainMethod.invoke(jrubyMainClass, new Object[] {processArgs(root, args)});
            } catch (InvocationTargetException e) {
                // Unwrap exceptions so they don't have our reflection usage
                // at the top of the stack
                if (e.getCause() != null) {
                    throw e.getCause();
                }
            }
        } finally {
            if (wrappingLoader != null) {
                wrappingLoader.close();
            }
            deleteRecursively(new File(root));
        }
    }

    protected String extractJar() throws Exception {
        String root = Files.createTempDirectory("torquebox").toFile().getPath();
        String mainPath = TorqueBoxMain.class.getName().replace(".", "/") + ".class";
        String mainUrl = TorqueBoxMain.class.getClassLoader().getResource(mainPath).toString();
        int from = "jar:file:".length();
        int to = mainUrl.indexOf("!/");
        String jarPath = mainUrl.substring(from, to);

        ZipInputStream zipStream = new ZipInputStream(new FileInputStream(jarPath));
        ZipEntry zipEntry = null;
        while ((zipEntry = zipStream.getNextEntry()) != null) {
            File file = new File(root + "/" + zipEntry.getName());
            if (zipEntry.isDirectory()) {
                file.mkdirs();
            } else {
                File parent = file.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
                FileOutputStream extractStream = new FileOutputStream(file);
                try {
                    byte[] buffer = new byte[4096];
                    int bytesRead = -1;
                    while ((bytesRead = zipStream.read(buffer)) != -1) {
                        extractStream.write(buffer, 0, bytesRead);
                    }
                } finally {
                    extractStream.close();
                }
            }
            zipStream.closeEntry();
        }

        return root;
    }

    protected String locateJRubyHome(String root) {
        File jrubyLib = new File(root + "/jruby/lib");
        if (jrubyLib.isDirectory()) {
            return root + "/jruby";
        }
        if (System.getProperty("jruby.home") != null) {
            return System.getProperty("jruby.home");
        }
        if (System.getenv("JRUBY_HOME") != null) {
            return System.getenv("JRUBY_HOME");
        }
        return null;
    }

    protected String[] processArgs(String root, String[] args) {
        List<String> arguments = new ArrayList<>(Arrays.asList(args));
        if (!arguments.remove("jruby")) {
            arguments.add(0, "run");
            arguments.add(0, "torquebox");
            arguments.add(0, "-S");
            if (usesBundler(root)) {
                arguments.add(0, "-rbundler/setup");
            }
            arguments.add(0, "-C" + root + "/app");
        }
        System.out.println("jruby " + join(arguments, " "));
        return arguments.toArray(new String[arguments.size()]);
    }

    protected boolean usesBundler(String root) {
        return new File(root + "/app/Gemfile").exists();
    }


    protected static String join(List<String> strings, String separator) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> i = strings.iterator(); i.hasNext();) {
            sb.append(i.next()).append(i.hasNext() ? separator : "");
        }
        return sb.toString();
    }

    protected static void deleteRecursively(File directory) {
        if (directory.isDirectory()) {
            for (File file : directory.listFiles())
                deleteRecursively(file);
        }
        directory.delete();
    }
}
