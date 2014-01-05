package org.plenix.rex.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RemoteExecutionListener implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(RemoteExecutionListener.class);

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
        } catch (Exception e) {
            String errorMessage = "Error during listener initialization: " + e;
            logger.error(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Stopping remote execution listener");
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
}
