package ru.shishmakov.config;

import org.apache.commons.lang3.StringUtils;
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
            public String getDbDriver() {
                return environment.getRequiredProperty(ConfigKey.DB_DRIVER);
            }

            @Override
            public String getDbUrl() {
                return environment.getRequiredProperty(ConfigKey.DB_URL);
            }

            @Override
            public String getDbUsername() {
                return environment.getRequiredProperty(ConfigKey.DB_USERNAME);
            }

            @Override
            public String getDbPassword() {
                return environment.getRequiredProperty(ConfigKey.DB_PASSWORD);
            }

            @Override
            public Integer getDbPoolSizeMin() {
                return environment.getRequiredProperty(ConfigKey.DB_POOL_SIZE_MIN, Integer.class);
            }

            @Override
            public Integer getDbPoolSizeMax() {
                return environment.getRequiredProperty(ConfigKey.DB_POOL_SIZE_MAX, Integer.class);
            }

            @Override
            public Integer getDbPoolSizeIncrement() {
                return environment.getRequiredProperty(ConfigKey.DB_POOL_SIZE_INCREMENT, Integer.class);
            }

            @Override
            public Integer getDbStatements() {
                return environment.getRequiredProperty(ConfigKey.DB_STATEMENTS, Integer.class);
            }

            @Override
            public String getDirectoryPath() {
                final String path = environment.getRequiredProperty(ConfigKey.DB_STATEMENTS);
                if(StringUtils.startsWithIgnoreCase(path, "{user.home}")) {
                    return StringUtils.replaceOnce(path,"{user.home}", System.getProperty("user.home"));
                }
                if(StringUtils.startsWithIgnoreCase(path, "{user.dir}")) {
                    return StringUtils.replaceOnce(path,"{user.dir}", System.getProperty("user.dir"));
                }
                return path;
            }
        };
    }

}
