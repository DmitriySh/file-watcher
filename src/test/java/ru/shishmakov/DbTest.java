package ru.shishmakov;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.shishmakov.config.ServerConfig;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Test Database for project
 *
 * @author Dmitriy Shishmakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ServerConfig.class)
public class DbTest extends TestBase {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testConnection() throws SQLException, PropertyVetoException {
        Connection connection = dataSource.getConnection();
        Assert.assertNotNull(connection);
        Assert.assertTrue("connection should be available", connection.isValid(5));
    }
}
