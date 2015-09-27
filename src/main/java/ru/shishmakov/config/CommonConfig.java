package ru.shishmakov.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;

/**
 * Configuration class for Client and Server classes.
 *
 * @author Dmitriy Shishmakov
 */
@Configuration
@PropertySources({@PropertySource("classpath:app.properties"),
        @PropertySource(value = "classpath:app.local.properties", ignoreResourceNotFound = true)})
public class CommonConfig {

    @Autowired
    private Environment environment;

    @Bean
    public AppConfig appConfig() {
        return new AppConfig() {
            @Override
            public String getProfileId() {
                return environment.getRequiredProperty(ConfigKey.PROFILE_ID);
            }

            @Override
            public String getConnectionHost() {
                return environment.getRequiredProperty(ConfigKey.CONNECT_HOST);
            }

            @Override
            public Integer getConnectionPort() {
                return environment.getRequiredProperty(ConfigKey.CONNECT_PORT, Integer.class);
            }

            @Override
            public String getConnectionUri() {
                return environment.getRequiredProperty(ConfigKey.CONNECT_URI);
            }

            @Override
            public String getDatabaseHost() {
                return environment.getRequiredProperty(ConfigKey.DATABASE_HOST);
            }

            @Override
            public Integer getDatabasePort() {
                return environment.getRequiredProperty(ConfigKey.DATABASE_PORT, Integer.class);
            }

            @Override
            public String getDatabaseName() {
                return environment.getRequiredProperty(ConfigKey.DATABASE_NAME);
            }

            @Override
            public String getCollectionName() {
                return environment.getRequiredProperty(ConfigKey.COLLECTION_NAME);
            }

            @Override
            public String getDatabaseUser() {
                return environment.getRequiredProperty(ConfigKey.DATABASE_USER);
            }

            @Override
            public String getDatabasePassword() {
                return environment.getRequiredProperty(ConfigKey.DATABASE_PASSWORD);
            }

            @Override
            public String getBindHost() {
                return environment.getRequiredProperty(ConfigKey.BIND_HOST);
            }

            @Override
            public Integer getBindPort() {
                return environment.getRequiredProperty(ConfigKey.BIND_PORT, Integer.class);
            }
        };
    }

}
