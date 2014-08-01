package xrrocha.res.classloader;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;

// FIXME Can't find resources
// FIXME Stack overflow on ClassNotFound
public class FilesystemDirectoryClassLoader extends ClassLoader {
    private File baseDirectory;

    public FilesystemDirectoryClassLoader(File baseDirectory, ClassLoader parent) {
        super(parent);
        if (!(baseDirectory.isDirectory()) && baseDirectory.canRead()) {
            throw new IllegalArgumentException("Invalid base directory: " + baseDirectory.getAbsolutePath());
        }
        this.baseDirectory = baseDirectory;
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return findClass(className);
    }

    @Override
    public Class<?> findClass(String className) throws ClassNotFoundException {
        File file = new File(baseDirectory, className.replaceAll("\\.", "/") + ".class");

        if (!file.exists()) {
            return super.loadClass(className);
        }

        try {
            int size = (int) file.length();
            byte bytes[] = new byte[size];
            FileInputStream fis = new FileInputStream(file);
            try (DataInputStream dis = new DataInputStream(fis)) {
                dis.readFully(bytes);
            }
            return defineClass(className, bytes, 0, bytes.length);
        } catch (IOException ioe) {
            throw new ClassNotFoundException(ioe.getMessage());
        }
    }

    @Override
    public Enumeration<URL> findResources(final String name) {  // TODO Override getResources
        final URL resource = getResource(name);

        return new Enumeration<URL>() {
            private boolean moreElements = resource != null;

            @Override
            public boolean hasMoreElements() {
                return moreElements;
            }

            @Override
            public URL nextElement() {
                if (moreElements) {
                    moreElements = false;
                    return resource;
                }
                throw new NoSuchElementException("No more resources: " + name);
            }
        };
    }

    @Override
    public URL findResource(String name) {
        File file = new File(baseDirectory, name);
        if (file.exists()) {
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                throw new IllegalStateException("Can't open URL: " + file.getAbsolutePath());
            }
        }
        return super.findResource(name);
    }
}
