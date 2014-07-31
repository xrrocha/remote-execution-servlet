package xrrocha.res.servlet;

import xrrocha.res.classloader.FilesystemDirectoryClassLoader;
import xrrocha.res.classloader.ParentLastURLClassLoader;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;
import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;
import static xrrocha.res.util.PropertyUtils.*;

public class RemoteExecutionListener implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(RemoteExecutionListener.class.getName());

    private Object mole;

    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Starting remote execution listener");

        ServletContext servletContext = sce.getServletContext();

        try {
            String configurationLocation = servletContext.getInitParameter("res.configuration");
            Properties properties = loadProperties(configurationLocation);

            // FIXME Class and/or jar directories should be optional
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
}
