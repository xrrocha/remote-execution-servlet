package org.plenix.rex.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class ParentLastURLClassLoader extends URLClassLoader {
    public ParentLastURLClassLoader(File directory, ClassLoader parent) {
        this(findJarLibraries(directory), parent);
    }

    private ClassLoader parent;

    public ParentLastURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, null);
        this.parent = parent;
    }

    @Override
    public synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class loadedClass = this.findLoadedClass(name);

        if (loadedClass == null) {
            try {
                loadedClass = super.findClass(name);
            } catch (ClassNotFoundException e) {
            }

            if (loadedClass == null) {
                if (this.parent == null) {
                    throw new ClassNotFoundException("Class not found: " + name);
                }
                loadedClass = parent.loadClass(name);
            }
        }

        return loadedClass;
    }

    @Override
    public URL getResource(String name) { // TODO Override getResources
        URL url = super.findResource(name);
        if (url == null) {
            url = this.parent.getResource(name);
        }
        return url;
    }

    public static URL[] findJarLibraries(File directory) {
        if (!directory.isDirectory() && directory.canRead()) {
            throw new IllegalArgumentException("Not a readable directory: " + directory.getAbsolutePath());
        }

        List<URL> urls = new ArrayList<>();
        findJarLibraries(directory, urls);
        return urls.toArray(new URL[urls.size()]);
    }

    static void findJarLibraries(File directory, List<URL> libraries) {
        for (File file : directory.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                try {
                    libraries.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new IllegalStateException("Error converting file to URL: " + e, e);
                }
            } else if (file.isDirectory()) {
                findJarLibraries(file, libraries);
            }
        }
    }
}
