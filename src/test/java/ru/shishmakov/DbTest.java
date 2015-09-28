package ru.shishmakov;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.shishmakov.config.AppConfig;
import ru.shishmakov.config.ServerConfig;

/**
 * Test Database for project
 *
 * @author Dmitriy Shishmakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServerConfig.class)
public class DbTest extends TestBase {

    @Autowired
    private AppConfig config;

    @Test
    public void testConnection() {
        logger.info(this.getClass().getName() + " !!!");
    }
}
