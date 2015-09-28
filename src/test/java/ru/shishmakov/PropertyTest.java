package ru.shishmakov;

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
    public void testConfiguration() {
        logger.info(this.getClass().getName() + " !!!");
    }
}
