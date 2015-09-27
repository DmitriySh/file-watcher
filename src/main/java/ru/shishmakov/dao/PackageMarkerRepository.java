package ru.shishmakov.dao;

/**
 * Empty marker class.<br/>
 * It used to identify package with classes of "Data Access Object"
 * for type-safe in Spring {@code @ComponentScan(basePackageClasses = )}.
 *
 * @author Dmitriy Shishmakov
 */
public abstract class PackageMarkerRepository {
    private PackageMarkerRepository() {
    }
}
