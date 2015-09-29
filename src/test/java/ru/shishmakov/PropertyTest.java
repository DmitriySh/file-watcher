package ru.shishmakov;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.shishmakov.config.AppConfig;
import ru.shishmakov.config.CommonConfig;

/**
 * Check loaded values from property file
 *
 * @author Dmitriy Shishmakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CommonConfig.class)
public class PropertyTest extends TestBase {

    @Autowired
    private AppConfig config;

    @Test
    public void configurationShouldNotBeEmpty() {
        Assert.assertTrue("shouldn't be empty", StringUtils.isNotBlank(config.getDbUrl()));
        Assert.assertTrue("shouldn't be empty", StringUtils.isNotBlank(config.getDbDriver()));
        Assert.assertTrue("shouldn't be empty", StringUtils.isNotBlank(config.getDbPassword()));
        Assert.assertTrue("shouldn't be empty", StringUtils.isNotBlank(config.getDbUsername()));
        Assert.assertTrue("shouldn't be empty", StringUtils.isNotBlank(config.getDirectoryPath()));
    }

    @Test
    public void configurationShouldBe() {
        Assert.assertNotNull("config should be", config.getDbPoolSizeMax());
        Assert.assertNotNull("config should be", config.getDbPoolSizeMin());
        Assert.assertNotNull("config should be", config.getDbPoolSizeIncrement());
        Assert.assertNotNull("config should be", config.getDbDriver());
        Assert.assertNotNull("config should be", config.getDbUrl());
        Assert.assertNotNull("config should be", config.getDbPassword());
        Assert.assertNotNull("config should be", config.getDbUsername());
        Assert.assertNotNull("config should be", config.getDbStatements());
        Assert.assertNotNull("config should be", config.getDirectoryPath());
    }

    @Test
    public void localPropertiesShouldOverrideValues() {
        Assert.assertEquals(10, config.getDbStatements().intValue());
        Assert.assertEquals(1, config.getDbPoolSizeMin().intValue());
    }
}













