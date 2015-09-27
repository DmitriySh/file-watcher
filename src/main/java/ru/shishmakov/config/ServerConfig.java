package ru.shishmakov.config;


import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.shishmakov.core.PackageMarkerCore;
import ru.shishmakov.dao.PackageMarkerRepository;
import ru.shishmakov.entity.PackageMarkerEntity;
import ru.shishmakov.service.PackageMarkerService;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Extension of configuration for Server
 *
 * @author Dmitriy Shishmakov
 */
@Configuration
@Import(CommonConfig.class)
@ComponentScan(basePackageClasses =
        {PackageMarkerService.class, PackageMarkerRepository.class, PackageMarkerCore.class})
@EnableTransactionManagement(proxyTargetClass = true)
public class ServerConfig {

    @Autowired
    private AppConfig config;

    @Bean(destroyMethod = "close")
    public DataSource dataSource() throws Exception {
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        cpds.setDriverClass("org.postgresql.Driver");
        cpds.setJdbcUrl("jdbc:postgresql://localhost:5432/" + config.getDatabaseName());
        cpds.setUser("postgres");
        cpds.setPassword("postgres");

        // the settings below are optional -- c3p0 can work with defaults
        cpds.setMinPoolSize(5);
        cpds.setMaxPoolSize(20);
        cpds.setAcquireIncrement(5);
        cpds.setMaxStatements(50);
        cpds.setMaxIdleTime(3000);
        return cpds;
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() throws Exception {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        vendorAdapter.setShowSql(true);
        vendorAdapter.setDatabasePlatform("org.hibernate.dialect.PostgreSQL9Dialect");
        vendorAdapter.setDatabase(Database.POSTGRESQL);
        vendorAdapter.getJpaPropertyMap().put("hibernate.format_sql", "true");

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan(getMappingBasePackage());
        factory.setDataSource(dataSource());
        factory.setJpaProperties(getJpaProperties());
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean
    public JpaTransactionManager transactionManager() throws Exception {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory());
        txManager.setJpaDialect(new HibernateJpaDialect());
        return txManager;
    }

    private Properties getJpaProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.cache.use_second_level_cache", "true");
        properties.setProperty("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory");
        properties.setProperty("hibernate.cache.use_query_cache", "true");
        properties.setProperty("hibernate.generate_statistics", "true");
        properties.setProperty("hibernate.generate_statistics", "true");
        return properties;
    }

    /**
     * Return the base package to scan classes for mapped {@link Entity} annotations.
     */
    private String getMappingBasePackage() {
        final Package mappingPackage = PackageMarkerEntity.class.getPackage();
        return mappingPackage == null ? null : mappingPackage.getName();
    }
}
