package xrrocha.res.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClassLoaderUtils {
    public static URL[] findJarLibraries(File directory) {
        if (!directory.isDirectory() && directory.canRead()) {
            throw new IllegalArgumentException("Not a readable directory: " + directory.getAbsolutePath());
        }

        List<URL> urls = new ArrayList<>();
        findJarLibraries(directory, urls);
        return urls.toArray(new URL[urls.size()]);
    }

    public static URL[] collectUrls(List<File> jarDirectories, List<File> classDirectories) {
        List<URL> urls = new ArrayList<>();
        if (jarDirectories != null) {
            collectJarUrls(jarDirectories, urls);
            if (classDirectories != null) {
                collectClassUrls(classDirectories, urls);
            }
        }
        return urls.toArray(new URL[urls.size()]);
    }

    public static void collectClassUrls(List<File> directories, List<URL> urls) {
        for (File directory: directories) {
            if (directory.isDirectory() && directory.canRead()) {
                urls.add(file2URL(directory));
            } else {
                throw new IllegalArgumentException("Not a readable directory: " + directory.getAbsolutePath());
            }
        }
    }

    public static void collectJarUrls(List<File> directories, List<URL> urls) {
        for (File directory: directories) {
            if (directory.isDirectory() && directory.canRead()) {
                findJarLibraries(directory, urls);
            } else {
                throw new IllegalArgumentException("Not a readable directory: " + directory.getAbsolutePath());
            }
        }
    }

    static void findJarLibraries(File directory, List<URL> libraries) {
        for (File file : directory.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".jar") && file.canRead()) {
                libraries.add(file2URL(file));
            } else if (file.isDirectory()) {
                findJarLibraries(file, libraries);
            }
        }
    }

    public static URL file2URL(File file) {
        try {
            return file.getAbsoluteFile().toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Error converting file to URL: " + e, e);
        }
    }
}
