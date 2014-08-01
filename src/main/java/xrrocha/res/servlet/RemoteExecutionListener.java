package xrrocha.res.servlet;

import xrrocha.res.classloader.FilesystemDirectoryClassLoader;
import xrrocha.res.classloader.ParentLastURLClassLoader;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import static xrrocha.res.util.PropertyUtils.*;

import static xrrocha.res.util.ClassLoaderUtils.*;

public class RemoteExecutionListener implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(RemoteExecutionListener.class.getName());

    private Object mole;

    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Starting remote execution listener");

        ServletContext servletContext = sce.getServletContext();

        try {
            String configurationLocation = servletContext.getInitParameter("res.configuration");
            Properties properties = loadProperties(configurationLocation);

            List<File> jarDirectories = getDirectories(properties.getProperty("jarDirectories"));
            List<File> classDirectories = getDirectories(properties.getProperty("classDirectories"));
            if (jarDirectories.size() == 0 && classDirectories.size() == 0) {
                throw new IllegalArgumentException("At least one of 'jarDirectories' and 'classDirectories' must be specified");
            }

            URL[] urls = collectUrls(jarDirectories, classDirectories);
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            ClassLoader classLoader = new ParentLastURLClassLoader(urls, contextClassLoader);

            HttpServlet servlet = new RemoteExecutionServlet(/*classDirectories, jarDirectories, contextClassLoader*/);
            String servletMapping = getRequiredProperty("servletMapping", properties);
            logger.info("Mapping remote execution servlet to " + servletMapping);
            servletContext
                    .addServlet("RemoteExecutionServlet", servlet)
                    .addMapping(servletMapping);

            String moleClass = properties.getProperty("moleClass");
            if (moleClass != null) {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest("Loading mole class: " + moleClass);
                Object mole = classLoader.loadClass(moleClass).newInstance();

                if (mole instanceof Properties) {
                    if (logger.isLoggable(Level.FINEST))
                        logger.finest("Mole class extends Properties");
                    Properties moleProperties = (Properties) mole;

                    Properties subProperties = subProperties(properties, "mole.");
                    copyProperties(subProperties, moleProperties);

                    moleProperties.put("resServlet", servlet);
                    moleProperties.put("resContext", servletContext);
                }

                if (mole instanceof Runnable) {
                    logger.info("Starting mole");
                    ((Runnable) mole).run();
                }
            }
        } catch (Exception e) {
            String errorMessage = "Error during listener initialization: " + e;
            logger.severe(errorMessage);
            throw new IllegalStateException(errorMessage, e);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Stopping remote execution listener");

        if (mole != null && mole instanceof Closeable) {
            try {
                logger.info("Stopping mole");
                ((Closeable) mole).close();
            } catch (Exception e) {
                logger.warning("Exception closing RES mole: " + e);
            }
        }
    }

    static List<File> getDirectories(String directoryList) {
        List<File> directories = new ArrayList<>();
        if (directoryList != null) {
            String[] directoryNames = directoryList.trim().split("\\s*,\\s*");
            for (int i = 0; i < directoryNames.length; i++) {
                File directory = new File(directoryNames[i]);
                if (directory.isDirectory() && directory.canRead()) {
                    directories.add(directory.getAbsoluteFile());
                } else {
                    logger.warning("Inaccessible directory: " + directory.getAbsolutePath());
                }
            }
        }
        return directories;
    }
}
