package com.rickenbazolo.paymux.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for loading configuration properties from files.
 * <p>
 * This class provides methods to load properties from the classpath or from specific files.
 * It supports loading from multiple property file names and merging them.
 * </p>
 * <p>
 * Typical usage:
 * <pre>{@code
 * Properties props = ConfigurationLoader.loadFromClasspath("paymux.properties");
 * String apiKey = ConfigurationLoader.getRequiredProperty(props, "paymux.airtel.congo.api-key");
 * }</pre>
 * </p>
 * @author Ricken Bazolo
 * @since 0.1.0
 */
public final class ConfigurationLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);

    private static final String[] DEFAULT_PROPERTY_FILES = {
        "paymux.properties",
        "application.properties"
    };

    private static final String[] DEFAULT_YAML_FILES = {
        "paymux.yml",
        "application.yml",
        "application.yaml"
    };

    /**
     * Pattern to match environment variable placeholders: ${VAR_NAME} or ${VAR_NAME:default}
     */
    private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\$\\{([^}:]+)(?::([^}]*))?\\}");

    /**
     * Private constructor to prevent instantiation.
     */
    private ConfigurationLoader() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Loads properties from the classpath.
     * <p>
 * Attempts to load from default property and YAML file names:
 * <ul>
 *   <li>paymux.yml</li>
 *   <li>application.yml / application.yaml</li>
 *   <li>paymux.properties</li>
 *   <li>application.properties</li>
     * </ul>
     * YAML files take precedence over properties files.
     * </p>
     *
     * @return loaded properties, or empty Properties if no files found
     */
    public static Properties loadFromClasspath() {
        Properties properties = new Properties();

        for (String fileName : DEFAULT_YAML_FILES) {
            try {
                Properties yamlProps = loadYamlFromClasspath(fileName);
                properties.putAll(yamlProps);
                logger.debug("Loaded YAML configuration from: {}", fileName);
            } catch (ConfigurationException e) {
                logger.debug("YAML file not found: {}", fileName);
            }
        }

        for (String fileName : DEFAULT_PROPERTY_FILES) {
            try {
                Properties fileProps = loadFromClasspath(fileName);
                properties.putAll(fileProps);
                logger.debug("Loaded properties from: {}", fileName);
            } catch (ConfigurationException e) {
                logger.debug("Property file not found: {}", fileName);
            }
        }

        return properties;
    }

    /**
     * Loads properties from a specific file on the classpath.
     *
     * @param fileName the property file name
     * @return loaded properties
     * @throws ConfigurationException if the file cannot be loaded
     */
    public static Properties loadFromClasspath(String fileName) {
        Properties properties = new Properties();

        try (InputStream input = ConfigurationLoader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new ConfigurationException("Property file not found in classpath: " + fileName);
            }

            properties.load(input);
            resolveEnvironmentVariables(properties);
            logger.debug("Successfully loaded configuration from: {}", fileName);
            return properties;

        } catch (IOException e) {
            throw new ConfigurationException("Failed to load property file: " + fileName, e);
        }
    }

    /**
     * Gets a required property value.
     * <p>
     * Throws an exception if the property is not found or is empty.
     * </p>
     *
     * @param properties the properties object
     * @param key the property key
     * @return the property value
     * @throws ConfigurationException if the property is missing or empty
     */
    public static String getRequiredProperty(Properties properties, String key) {
        String value = properties.getProperty(key);

        if (value == null || value.trim().isEmpty()) {
            throw new ConfigurationException(
                "Required property '" + key + "' is not set. " +
                "Please configure it in your properties file."
            );
        }

        return value.trim();
    }

    /**
     * Gets an optional property value with a default.
     *
     * @param properties the properties object
     * @param key the property key
     * @param defaultValue the default value if property is not found
     * @return the property value or default value
     */
    public static String getProperty(Properties properties, String key, String defaultValue) {
        String value = properties.getProperty(key);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : defaultValue;
    }

    /**
     * Gets a boolean property value.
     *
     * @param properties the properties object
     * @param key the property key
     * @param defaultValue the default value if property is not found
     * @return the boolean property value
     */
    public static boolean getBooleanProperty(Properties properties, String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    /**
     * Gets an integer property value.
     *
     * @param properties the properties object
     * @param key the property key
     * @param defaultValue the default value if property is not found
     * @return the integer property value
     * @throws ConfigurationException if the value cannot be parsed as an integer
     */
    public static int getIntProperty(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ConfigurationException(
                "Property '" + key + "' has invalid integer value: " + value, e
            );
        }
    }

    /**
     * Validates that all required properties are present.
     *
     * @param properties the properties object
     * @param requiredKeys the required property keys
     * @throws ConfigurationException if any required property is missing
     */
    public static void validateRequiredProperties(Properties properties, String... requiredKeys) {
        for (String key : requiredKeys) {
            getRequiredProperty(properties, key);
        }
    }

    /**
     * Loads properties from a YAML file on the classpath.
     * <p>
     * This method requires SnakeYAML to be on the classpath.
     * </p>
     *
     * @param fileName the YAML file name
     * @return loaded properties
     * @throws ConfigurationException if the file cannot be loaded or SnakeYAML is not available
     */
    public static Properties loadYamlFromClasspath(String fileName) {
        try {
            Class.forName("org.yaml.snakeyaml.Yaml");
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(
                "SnakeYAML library not found. Add org.yaml:snakeyaml dependency to use YAML configuration files.", e
            );
        }

        try (InputStream input = ConfigurationLoader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new ConfigurationException("YAML file not found in classpath: " + fileName);
            }

            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(input);

            if (yamlData == null) {
                return new Properties();
            }

            Properties properties = new Properties();
            flattenYamlMap("", yamlData, properties);
            resolveEnvironmentVariables(properties);

            logger.info("Successfully loaded YAML configuration from: {}", fileName);
            return properties;

        } catch (IOException e) {
            throw new ConfigurationException("Failed to load YAML file: " + fileName, e);
        } catch (Exception e) {
            throw new ConfigurationException("Failed to parse YAML file: " + fileName, e);
        }
    }

    /**
     * Flattens a nested YAML map into dot-notation properties.
     * <p>
     * Example: {paymux: {mtn: {congo: {api-user: "xxx"}}}} becomes
     * "paymux.mtn.congo.api-user" = "xxx"
     * </p>
     *
     * @param prefix the current key prefix
     * @param map the YAML map to flatten
     * @param properties the properties object to populate
     */
    @SuppressWarnings("unchecked")
    private static void flattenYamlMap(String prefix, Map<String, Object> map, Properties properties) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                flattenYamlMap(key, (Map<String, Object>) value, properties);
            } else if (value != null) {
                properties.setProperty(key, value.toString());
            }
        }
    }

    /**
     * Resolves environment variable placeholders in all property values.
     * <p>
     * Supports the following placeholder formats:
     * <ul>
     *   <li>${ENV_VAR} - replaced with the value of ENV_VAR, throws exception if not set</li>
     *   <li>${ENV_VAR:default} - replaced with the value of ENV_VAR, or "default" if not set</li>
     * </ul>
     * </p>
     * <p>
     * Example:
     * <pre>
     * # In YAML or properties file:
     * paymux.mtn.congo.api-user=${CG_MOMO_API_USER}
     * paymux.mtn.congo.api-key=${CG_MOMO_API_KEY:default-key}
     * </pre>
     * </p>
     *
     * @param properties the properties object to resolve
     */
    private static void resolveEnvironmentVariables(Properties properties) {
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (value != null) {
                String resolvedValue = resolveEnvironmentVariable(value);
                if (!value.equals(resolvedValue)) {
                    properties.setProperty(key, resolvedValue);
                    logger.debug("Resolved environment variable for property: {}", key);
                }
            }
        }
    }

    /**
     * Resolves environment variable placeholders in a single value.
     * <p>
     * This method can handle multiple placeholders in the same value.
     * For example: "${HOST}:${PORT}" will resolve both HOST and PORT.
     * </p>
     *
     * @param value the value that may contain environment variable placeholders
     * @return the resolved value with all placeholders replaced
     * @throws ConfigurationException if a required environment variable is not set
     */
    private static String resolveEnvironmentVariable(String value) {
        if (value == null || !value.contains("${")) {
            return value;
        }

        Matcher matcher = ENV_VAR_PATTERN.matcher(value);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String envVarName = matcher.group(1);
            String defaultValue = matcher.group(2);

            String envValue = System.getenv(envVarName);

            if (envValue == null) {
                if (defaultValue != null) {
                    envValue = defaultValue;
                    logger.debug("Environment variable '{}' not set, using default value", envVarName);
                } else {
                    throw new ConfigurationException(
                        "Environment variable '" + envVarName + "' is not set and no default value provided. " +
                        "Please set the environment variable or provide a default value using ${" + envVarName + ":defaultValue}"
                    );
                }
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(envValue));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Exception thrown when configuration loading or validation fails.
     */
    public static class ConfigurationException extends RuntimeException {
        public ConfigurationException(String message) {
            super(message);
        }

        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
