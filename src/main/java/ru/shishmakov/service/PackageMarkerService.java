package ru.shishmakov.service;

/**
 * Empty marker class.<br/>
 * It used to identify package with classes of "Business Service Facade"
 * for type-safe in Spring {@code @ComponentScan(basePackageClasses = )}.
 *
 * @author Dmitriy Shishmakov
 */
public abstract class PackageMarkerService {
    private PackageMarkerService() {
    }
}
