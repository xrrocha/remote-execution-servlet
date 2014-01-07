package org.plenix.rex.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * A parent-last classloader that will try the child classloader first and then the parent. This takes a fair bit of doing
 * because java really prefers parent-first.
 * <p/>
 * For those not familiar with class loading trickery, be wary
 */
public class ParentLastURLClassLoader extends ClassLoader {
    private final ChildURLClassLoader childClassLoader;

    /**
     * This class allows me to call findClass on a classloader
     */
    private static class FindClassClassLoader extends ClassLoader {
        public FindClassClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            return super.findClass(name);
        }
    }

    /**
     * This class delegates (child then parent) for the findClass method for a URLClassLoader. We need this because findClass is
     * protected in URLClassLoader
     */
    private static class ChildURLClassLoader extends URLClassLoader {
        private final FindClassClassLoader realParent;

        public ChildURLClassLoader(URL[] urls, FindClassClassLoader realParent) {
            super(urls, null);

            this.realParent = realParent;
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                // first try to use the URLClassLoader findClass
                return super.findClass(name);
            } catch (ClassNotFoundException e) {
                // if that fails, we ask our real parent classloader to load the class (we give up)
                return realParent.loadClass(name);
            }
        }
    }

    public ParentLastURLClassLoader(File directory, ClassLoader parent) {
        this(findJarLibraries(directory), parent);
    }

    public ParentLastURLClassLoader(URL[] urls, ClassLoader parent) {
        super(parent);
        childClassLoader = new ChildURLClassLoader(urls, new FindClassClassLoader(this.getParent()));
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            // first we try to find a class inside the child classloader
            return childClassLoader.findClass(name);
        } catch (ClassNotFoundException e) {
            // didn't find it, try the parent
            return super.loadClass(name, resolve);
        }
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
