package xrrocha.res.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtils {
   public  static Properties loadProperties(String filename) throws IOException {
        InputStream is = new FileInputStream(filename);
        Properties properties = new Properties();
        properties.load(is);
        return properties;
    }

    public static String getProperty(String propertyName, Properties properties) {
        String propertyValue = properties.getProperty(propertyName);
        if (propertyValue == null) {
            throw new IllegalArgumentException("No such property: " + propertyName);
        }
        return propertyValue.trim();
    }

    public static Properties subProperties(Properties properties, String prefix) {
        Properties subProperties = new Properties();
        for (String propertyName: properties.stringPropertyNames()) {
            if (propertyName.startsWith(prefix)) {
                String propertyValue = properties.getProperty(propertyName);
                subProperties.setProperty(propertyName, propertyValue.substring(prefix.length()));
            }
        }
        return subProperties;
    }

    public static void copyProperties(Properties from, Properties to) {
        for (String propertyName: from.stringPropertyNames()) {
            to.setProperty(propertyName, from.getProperty(propertyName));
        }
    }
}
