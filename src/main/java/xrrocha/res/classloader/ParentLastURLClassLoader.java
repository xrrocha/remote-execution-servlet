package xrrocha.res.classloader;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import static xrrocha.res.util.ClassLoaderUtils.findJarLibraries;

// TODO Verify resource loading
// TODO Add closing of jar files
public class ParentLastURLClassLoader extends URLClassLoader {
    private ClassLoader parent;

    public ParentLastURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, null);
        this.parent = parent;
    }

    // Currently used only by ReloadableClassExecutor
    public ParentLastURLClassLoader(File jarDirectory, ClassLoader parent) {
        this(findJarLibraries(jarDirectory), parent);
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
    public URL getResource(String name) {
        URL url = super.findResource(name);
        if (url == null) {
            url = this.parent.getResource(name);
        }
        return url;
    }
}
