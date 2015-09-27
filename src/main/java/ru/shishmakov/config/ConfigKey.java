package ru.shishmakov.config;

/**
 * Names of configuration keys used to get values from {@link AppConfig} instance.
 *
 * @author Dmitriy Shishmakov
 * @see AppConfig
 */
public final class ConfigKey {

    private ConfigKey() {
    }

    public static final String DATABASE_HOST = "database.host";
    public static final String DATABASE_PORT = "database.port";
    public static final String DATABASE_USER = "database.user";
    public static final String DATABASE_PASSWORD = "database.password";
    public static final String DATABASE_NAME = "database.name";
    public static final String COLLECTION_NAME = "collection.name";
    public static final String BIND_HOST = "bind.host";
    public static final String BIND_PORT = "bind.port";
    public static final String CONNECT_HOST = "connect.host";
    public static final String CONNECT_PORT = "connect.port";
    public static final String CONNECT_URI = "connect.uri";
    public static final String PROFILE_ID = "profile.id";
}
