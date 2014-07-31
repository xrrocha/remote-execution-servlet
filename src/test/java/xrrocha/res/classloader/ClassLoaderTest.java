package xrrocha.res.classloader;

import java.io.File;

public abstract class ClassLoaderTest {
    private final File tempDirectory = new File(System.getProperty("java.io.tmpdir"));

    public File newTemporaryDirectory() {
        String directoryName = "res_" + String.valueOf(System.currentTimeMillis());
        File sourceDirectory = new File(tempDirectory, directoryName);
        sourceDirectory.mkdir();
        sourceDirectory.deleteOnExit();
        return sourceDirectory;
    }
}
