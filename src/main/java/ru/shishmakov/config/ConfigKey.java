package ru.shishmakov.config;

/**
 * Names of configuration keys used to get values from {@link AppConfig} instance.
 *
 * @author Dmitriy Shishmakov
 * @see AppConfig
 */
public interface ConfigKey {

    String DB_DRIVER = "db.driver";
    String DB_URL = "db.url";
    String DB_USERNAME = "db.username";
    String DB_PASSWORD = "db.password";
    String DB_POOL_SIZE_MIN = "db.poolsize.min";
    String DB_POOL_SIZE_MAX = "db.poolsize.max";
    String DB_POOL_SIZE_INCREMENT = "db.poolsize.increment";
    String DB_STATEMENTS = "db.statements";
}
