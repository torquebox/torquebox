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
        String root = extractJar();
        System.setProperty("jruby.home", root + "/jruby");
        List<URL> urlList = new ArrayList<URL>();
        for (File each : new File(root + "/jruby/lib").listFiles()) {
            if (each.getName().endsWith(".jar")) {
                urlList.add(each.toURI().toURL());
            }
        }

        URLClassLoader wrappingLoader = null;
        try {
            wrappingLoader = URLClassLoader.newInstance(urlList.toArray(new URL[urlList.size()]));
            try {
                Thread.currentThread().setContextClassLoader(wrappingLoader);
                Class <?> jrubyMainClass = Class.forName("org.jruby.Main", true, wrappingLoader);
                Method jrubyMainMethod = jrubyMainClass.getDeclaredMethod("main", String[].class);
                jrubyMainMethod.invoke(jrubyMainClass, new Object[] {processArgs(root, args)});

//                Class wunderBossClass = Class.forName("org.projectodd.wunderboss.WunderBoss");
//                Method findLanguageMethod = wunderBossClass.getDeclaredMethod("findLanguage", String.class);
//                Object language = findLanguageMethod.invoke(wunderBossClass, "ruby");
//                Method evalMethod = language.getClass().getDeclaredMethod("eval", String.class);
//                evalMethod.invoke(language, "require 'torquebox-core'; TorqueBox::CLI.new(ARGV)");
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
        }
    }

    protected String extractJar() throws Exception {
        String root = Files.createTempDirectory("torquebox").toFile().getPath();
        String mainPath = TorqueBoxMain.class.getName().replace(".", "/") + ".class";
        String mainUrl = TorqueBoxMain.class.getClassLoader().getResource(mainPath).toString();
        int from = "jar:file:".length();
        int to = mainUrl.indexOf("!/");
        String jarPath = mainUrl.substring(from, to);
        System.err.println("!!! EXTRACTING JAR " + jarPath  + " to " + root);

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

    protected String[] processArgs(String root, String[] args) {
        List<String> arguments = new ArrayList<>(Arrays.asList(args));
        if (!arguments.remove("jruby")) {
            arguments.add(0, "run");
            arguments.add(0, "torquebox");
            arguments.add(0, "-S");
            arguments.add(0, "-rbundler/setup");
            arguments.add(0, "-C" + root + "/app");
        }
        System.out.println("Calling JRuby with arguments: " + join(arguments, " "));
        return arguments.toArray(new String[arguments.size()]);
    }


    protected static String join(List<String> strings, String separator) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> i = strings.iterator(); i.hasNext();) {
            sb.append(i.next()).append(i.hasNext() ? separator : "");
        }
        return sb.toString();
    }
}
