package org.plenix.rex;

import org.plenix.rex.classloader.FilesystemDirectoryClassLoader;
import org.plenix.rex.classloader.ParentLastURLClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

public class ReloadableClassExecutor implements Executor {
    private File classDirectory;
    private File jarDirectory;
    private ClassLoader parentClassLoader;

    private URL[] jarLibraries;
    private ClassLoader parentLastClassLoader;

    private static final Logger logger = LoggerFactory.getLogger(ReloadableClassExecutor.class);

    public ReloadableClassExecutor(File classDirectory, File jarDirectory, ClassLoader parentClassLoader) {
        this.classDirectory = classDirectory;
        this.jarDirectory = jarDirectory;
        this.parentClassLoader = parentClassLoader;

        scanLibraries();
    }

    @Override
    public void execute(String className, Map<String, Object> parameters, ExecutionContext context) throws Exception {
        logger.debug("Loading class {}", className);
        // TODO Reload classes only on file change
        ClassLoader classLoader = new FilesystemDirectoryClassLoader(classDirectory, parentLastClassLoader);
        Executable<ExecutionContext> executable = (Executable<ExecutionContext>) classLoader.loadClass(className).newInstance();

        logger.debug("Executing class {}", className);
        executable.execute(parameters, context);
    }

    public void refreshLibraries() {
        scanLibraries();
    }

    private void scanLibraries() {
        jarLibraries = ParentLastURLClassLoader.findJarLibraries(jarDirectory);
        //parentLastClassLoader = new ParentLastURLClassLoader(jarLibraries, parentClassLoader);
        parentLastClassLoader = new URLClassLoader(jarLibraries, parentClassLoader);
    }
}
