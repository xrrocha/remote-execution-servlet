package org.plenix.rex.classloader;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class JavaSourceDirectory {
    private final File sourceDirectory;
    private final String baseFilename;

    private static final int BUFFER_SIZE = 4096;

    public JavaSourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
        sourceDirectory.mkdirs();

        baseFilename = sourceDirectory.getAbsolutePath();
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public void addJavaClass(String packageName, String className, String sourceCode) throws IOException {
        File packageDirectory;
        if (packageName.length() == 0) {
            packageDirectory = sourceDirectory;
        } else {
            packageDirectory = createDirectory(packageName.replace(".", "/"));
        }
        File sourceFile = new File(packageDirectory, className + ".java");

        try (PrintWriter out = new PrintWriter(new FileWriter(sourceFile), true)) {
            out.println(String.format("package %s;", packageName));
            out.println(String.format("public class %s {", className));
            out.println(sourceCode);
            out.println("}");
        }
    }

    public void addFile(String packageName, String filename, InputStream in) throws IOException {
        File packageFile = createDirectory(packageName.replace(".", "/"));
        File file = new File(packageFile, filename);

        try (OutputStream os = new FileOutputStream(file)) {
            copy(in, new FileOutputStream(file));
        }
    }

    public boolean compile() {
        List<File> sourceFiles = collectFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().endsWith(".java");
            }
        });

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFiles);
        return compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();
    }

    public void buildJarLibrary(OutputStream destination) throws IOException {
        compile();

        List<File> resourceFiles = collectFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.getName().endsWith(".java");
            }
        });

        try (JarOutputStream jos = new JarOutputStream(destination)) {
            for (File resourceFile : resourceFiles) {
                String absolutePath = resourceFile.getAbsolutePath().replace("\\", "/");
                String filename = absolutePath.substring(baseFilename.length() + 1, absolutePath.length());
                JarEntry jarEntry = new JarEntry(filename);
                jarEntry.setTime(resourceFile.lastModified());
                jos.putNextEntry(jarEntry);
                try (InputStream is = new FileInputStream(resourceFile)) {
                    copy(is, jos);
                }
                jos.closeEntry();
            }
            //jos.finish();
        }
    }

    // TODO
    public void purge() {
        List<File> allFiles = collectFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return true;
            }
        });

        for (File file : allFiles) {

        }
    }


    private List<File> collectFiles(FileFilter filter) {
        List<File> sourceFiles = new ArrayList<>();
        collectFiles(sourceDirectory, filter, sourceFiles);
        return sourceFiles;
    }

    private void collectFiles(File directory, FileFilter filter, List<File> fileList) {
        File[] children = directory.listFiles(filter);
        for (File child : children) {
            if (child.isDirectory()) {
                collectFiles(child, filter, fileList);
            } else {
                fileList.add(child);
            }
        }
    }

    private File createDirectory(String name) {
        File subdir = new File(sourceDirectory, name);
        subdir.mkdirs();
        return subdir;
    }

    private void copy(InputStream is, OutputStream os) throws IOException {
        int bytesRead;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((bytesRead = is.read(buffer)) > 0) {
            os.write(buffer, 0, bytesRead);
        }
        os.flush();
    }
}
