package org.plenix.rex.classloader;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.assertEquals;

@Ignore
public class ParentLastURLClassLoaderTest extends ClassLoaderTest {
    private final String packageName = "com.acme";
    private final String className = "Test";
    private final String codeTemplate = "public String toString() { return \"%s\"; }";

    @Test
    public void loadsClassFromLibraries() throws Exception {
        JavaSourceDirectory parentDirectory = new JavaSourceDirectory(newTemporaryDirectory());
        createSourceFile(parentDirectory, "parent");
        ClassLoader parentClassLoader = new URLClassLoader(
                new URL[]{parentDirectory.getSourceDirectory().toURI().toURL()},
                Thread.currentThread().getContextClassLoader());
        assertEquals("parent", toString(parentClassLoader));

        JavaSourceDirectory childDirectory = new JavaSourceDirectory(newTemporaryDirectory());
        createSourceFile(childDirectory, "child");
        File jarFile = File.createTempFile("rex", ".jar");
        childDirectory.buildJarLibrary(new FileOutputStream(jarFile));
        ClassLoader childClassLoader = new ParentLastURLClassLoader(
                new URL[]{jarFile.toURI().toURL()},
                parentClassLoader);
        assertEquals("child", toString(childClassLoader));
    }

    private String toString(ClassLoader classLoader) throws Exception {
        return classLoader.loadClass(packageName + "." + className).newInstance().toString();
    }

    private void createSourceFile(JavaSourceDirectory sourceDirectory, String value) throws IOException {
        String javaCode = String.format(codeTemplate, value);
        sourceDirectory.addJavaClass(packageName, className, javaCode);
        if (!sourceDirectory.compile()) {
            throw new IllegalArgumentException("Compilation error");
        }
    }
}
