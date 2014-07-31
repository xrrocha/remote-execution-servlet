package xrrocha.rex;

import xrrocha.rex.classloader.FilesystemDirectoryClassLoader;
import xrrocha.rex.classloader.ParentLastURLClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class ReloadableClassExecutor<C> implements Executor<C> {
    private final File classDirectory;
    private final File jarDirectory;
    private final ClassLoader parentClassLoader;

    private ClassLoader parentLastClassLoader;

    private static final Logger logger = LoggerFactory.getLogger(ReloadableClassExecutor.class);

    public ReloadableClassExecutor(File classDirectory, File jarDirectory, ClassLoader parentClassLoader) {
        this.classDirectory = classDirectory;
        this.jarDirectory = jarDirectory;
        this.parentClassLoader = parentClassLoader;

        scanLibraries();
    }

    @Override
    public void execute(String className, Map<String, Object> parameters, C context) throws Exception {
        logger.debug("Loading class {}", className);
        // TODO Reload classes only on file change
        ClassLoader classLoader = new FilesystemDirectoryClassLoader(classDirectory, parentLastClassLoader);
        @SuppressWarnings("unchecked")
        Executable<C> executable = (Executable<C>) classLoader.loadClass(className).newInstance();

        logger.debug("Executing class {}", className);
        executable.execute(parameters, context);
    }

    public void scanLibraries() {
        parentLastClassLoader = new ParentLastURLClassLoader(jarDirectory, parentClassLoader);
    }
}
