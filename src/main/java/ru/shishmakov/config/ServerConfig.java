package ru.shishmakov.config;


import com.jolbox.bonecp.BoneCPDataSource;
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

    @Bean(name = "dataSource", destroyMethod = "close")
    public BoneCPDataSource dataSource() throws Exception {
        BoneCPDataSource dataSource = new BoneCPDataSource();
        dataSource.setDriverClass(config.getDbDriver());
        dataSource.setJdbcUrl(config.getDbUrl());
        dataSource.setUsername(config.getDbUsername());
        dataSource.setPassword(config.getDbPassword());

        dataSource.setIdleConnectionTestPeriodInMinutes(60);
        dataSource.setIdleMaxAgeInMinutes(420);
        dataSource.setReleaseHelperThreads(3);

        dataSource.setPartitionCount(3);
        dataSource.setMinConnectionsPerPartition(config.getDbPoolSizeMin());
        dataSource.setMaxConnectionsPerPartition(config.getDbPoolSizeMax());
        dataSource.setAcquireIncrement(config.getDbPoolSizeIncrement());
        dataSource.setStatementsCacheSize(config.getDbStatements());
        return dataSource;
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
        properties.setProperty("hibernate.cache.use_query_cache", "true");
        properties.setProperty("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory");
        properties.setProperty("hibernate.cache.provider_configuration_file_resource_path", "classpath:ehcache.xml");
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
