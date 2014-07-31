package xrrocha.rex.servlet;

import xrrocha.rex.classloader.FilesystemDirectoryClassLoader;
import xrrocha.rex.classloader.ParentLastURLClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;
import java.io.*;
import java.util.Properties;

public class RemoteExecutionListener implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(RemoteExecutionListener.class);

    private Object mole;

    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Starting remote execution listener");

        ServletContext servletContext = sce.getServletContext();

        try {
            String configurationLocation = servletContext.getInitParameter("rex.configuration");
            Properties properties = loadConfiguration(configurationLocation);

            File classDirectory = new File(getProperty("classDirectory", properties));
            File jarDirectory = new File(getProperty("jarDirectory", properties));
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

            HttpServlet servlet = new RemoteExecutionServlet(classDirectory, jarDirectory, contextClassLoader);
            String servletMapping = getProperty("servletMapping", properties);
            logger.info("Mapping remote execution servlet to " + servletMapping);
            servletContext
                    .addServlet("RemoteExecutionServlet", servlet)
                    .addMapping(servletMapping);

            String moleClass = properties.getProperty("moleClass");
            if (moleClass != null) {
                ClassLoader parentClassLoader = new ParentLastURLClassLoader(jarDirectory, contextClassLoader);
                ClassLoader classLoader = new FilesystemDirectoryClassLoader(classDirectory, parentClassLoader);
                Object mole = classLoader.loadClass(moleClass);

                if (mole instanceof Properties) {
                    Properties moleProperties = (Properties) mole;

                    Properties subProperties = subProperties(properties, "mole.");
                    copyProperties(subProperties, moleProperties);

                    moleProperties.put("relServlet", servlet);
                    moleProperties.put("relContext", servletContext);
                }

                logger.info("Starting mole");
                if (mole instanceof Runnable) {
                    ((Runnable) mole).run();
                }
            }
        } catch (Exception e) {
            String errorMessage = "Error during listener initialization: " + e;
            logger.error(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Stopping remote execution listener");

        if (mole != null && mole instanceof Closeable) {
            try {
                ((Closeable) mole).close();
            } catch (Exception e) {
                logger.warn("Exception closing REL mole: " + e);
            }
        }
    }

    static Properties loadConfiguration(String configurationLocation) throws IOException {
        InputStream is = new FileInputStream(configurationLocation);
        Properties properties = new Properties();
        properties.load(is);
        return properties;
    }

    static String getProperty(String propertyName, Properties properties) {
        String propertyValue = properties.getProperty(propertyName);
        if (propertyValue == null) {
            throw new IllegalArgumentException("No such property: " + propertyName);
        }
        return propertyValue.trim();
    }

    static Properties subProperties(Properties properties, String prefix) {
        Properties subProperties = new Properties();
        for (String propertyName: properties.stringPropertyNames()) {
            if (propertyName.startsWith(prefix)) {
                String propertyValue = properties.getProperty(propertyName);
                subProperties.setProperty(propertyName, propertyValue.substring(prefix.length()));
            }
        }
        return subProperties;
    }

    static void copyProperties(Properties from, Properties to) {
        for (String propertyName: from.stringPropertyNames()) {
            to.setProperty(propertyName, from.getProperty(propertyName));
        }
    }
}
