package xrrocha.rex.classloader;

import org.junit.Test;
import xrrocha.rex.classloader.FilesystemDirectoryClassLoader;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class FilesystemDirectoryClassLoaderTest {
    @Test
    public void handlesNonExistentClasses() {
        // TODO Ensure non-existent classes don't cause infinite loop
    }

    @Test
    public void reloadsClasses() throws Exception {
        File sourceDirectory = new File(new File(System.getProperty("java.io.tmpdir")), "rel_"
                + String.valueOf(System.currentTimeMillis()));
        sourceDirectory.mkdir();

        try {
            File packageFile = new File(sourceDirectory, "com/acme/test");
            packageFile.mkdirs();
            File javaFile = new File(packageFile, "MyTest.java");

            testCode(sourceDirectory, javaFile, 0);
            testCode(sourceDirectory, javaFile, 1);
        } finally {
            delete(sourceDirectory);
        }
    }

    private void testCode(File sourceDirectory, File javaFile, int value) throws Exception {
        String codeTemplate = "package com.acme.test;\n" + "public class MyTest {\n"
                + "    public int value() { return %d; }\n" + "}\n";

        writeFile(javaFile, codeTemplate, value);
        compile(javaFile);

        URLClassLoader parentClassLoader = new URLClassLoader(new URL[]{}, getClass().getClassLoader());
        FilesystemDirectoryClassLoader classLoader = new FilesystemDirectoryClassLoader(sourceDirectory, parentClassLoader);

        Class<?> clazz = classLoader.loadClass("com.acme.test.MyTest");

        Method method = clazz.getMethod("value", new Class<?>[]{});
        Object instance = clazz.newInstance();

        assertEquals(method.invoke(instance, new Object[]{}), value);
    }

    private void writeFile(File file, String template, int value) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            String string = String.format(template, value);
            fos.write(string.getBytes());
            fos.flush();
        }
    }

    private void compile(File javaFile) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays
                .asList(new File[]{javaFile}));
        compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();
    }

    private void delete(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                delete(child);
            }
        }
        file.delete();
    }
}
