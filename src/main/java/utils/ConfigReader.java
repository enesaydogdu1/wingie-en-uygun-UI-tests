package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ConfigReader {
    private static final Properties props = new Properties();

    static {
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream("src/main/resources/config.properties"), StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (IOException e) {
            throw new RuntimeException("config.properties file could not be read (UTF-8): " + e.getMessage(), e);
        }
    }

    public static String get(String key) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys;
        return props.getProperty(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static String getOrDefault(String key, String defaultValue) {
        String value = get(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

    public static int getIntOrDefault(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
