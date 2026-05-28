package helperUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    private static Properties property;

    static {

        try {
            property = new Properties();

            FileInputStream file = new FileInputStream(
                    System.getProperty("user.dir") + "/src/test/resources/config.properties"
            );
            property.load(file);
            file.close();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties file", e);
        }
    }

    public static String getProperty(String key) {
        return property.getProperty(key);
    }

    // Get browser directly
    public static String getBrowser() {
        return property.getProperty("browser");
    }

    // Get URL directly
    public static String getUrl() {
        return property.getProperty("WebSiteUrl");
    }

    public static long getImplicitWaitfromConfig(){
        return Long.parseLong(property.getProperty("implicit.wait"));
    }

}