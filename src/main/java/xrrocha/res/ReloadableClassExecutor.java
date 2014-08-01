package xrrocha.res;

import xrrocha.res.classloader.FilesystemDirectoryClassLoader;
import xrrocha.res.classloader.ParentLastURLClassLoader;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.File;
import java.util.Map;

public class ReloadableClassExecutor<C> implements Executor<C> {
    private final File classDirectory;
    private final File jarDirectory;
    private final ClassLoader parentClassLoader;

    private ClassLoader parentLastClassLoader;

    private static final Logger logger = Logger.getLogger(ReloadableClassExecutor.class.getName());

    public ReloadableClassExecutor(File classDirectory, File jarDirectory, ClassLoader parentClassLoader) {
        this.classDirectory = classDirectory;
        this.jarDirectory = jarDirectory;
        this.parentClassLoader = parentClassLoader;

        scanLibraries();
    }

    @Override
    public void execute(String className, Map<String, Object> parameters, C context) throws Exception {
        if (logger.isLoggable(Level.FINEST))
            logger.finest("Loading class " + className);
        // TODO Reload classes only on file change
        ClassLoader classLoader = new FilesystemDirectoryClassLoader(classDirectory, parentLastClassLoader);
        @SuppressWarnings("unchecked")
        Executable<C> executable = (Executable<C>) classLoader.loadClass(className).newInstance();

        if (logger.isLoggable(Level.FINEST))
            logger.finest("Executing class " + className);
        executable.execute(parameters, context);
    }

    public void scanLibraries() {
        parentLastClassLoader = new ParentLastURLClassLoader(jarDirectory, parentClassLoader);
    }
}
