package ru.shishmakov.config;

/**
 * @author Dmitriy Shishmakov
 */
public interface AppConfig {

    String getDbDriver();

    String getDbUrl();

    String getDbUsername();

    String getDbPassword();

    Integer getDbPoolSizeMin();

    Integer getDbPoolSizeMax();

    Integer getDbPoolSizeIncrement();

    Integer getDbStatements();

    String getDirectoryPath();

    Integer getParserCount();

    Integer getPersistCount();
}
