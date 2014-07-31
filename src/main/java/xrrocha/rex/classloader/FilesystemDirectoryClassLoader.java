package xrrocha.rex.classloader;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
}
