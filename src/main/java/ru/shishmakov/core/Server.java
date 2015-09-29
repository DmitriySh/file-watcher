package ru.shishmakov.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.util.concurrent.Exchanger;

/**
 * Manage life cycle of File Watch server.
 *
 * @author Dmitriy Shishmakov
 */
@Component
public class Server {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private DataSource dataSource;

    /**
     * Synchronized rendezvous
     */
    private Exchanger<String> exchanger = new Exchanger<>();

    public void start() throws InterruptedException {
        logger.debug("Initialise server ...");
        registerShutdownHook();

        while (!checkDbConnection()) {
            logger.debug("Trying to check the DB connection again ...");
            Thread.sleep(10_000);
        }

        logger.info("Start the server: {}. Watch on: {}", this.getClass().getSimpleName(),
                System.getProperty("user.home"));
    }

    public void stop(ConfigurableApplicationContext context) {
        logger.debug("Finalization server ...");
        context.close();
        logger.info("Shutdown the server: {}", System.getProperty("user.home"));
    }

    public void await() throws InterruptedException {
        exchanger.exchange("I wait you!");
        logger.debug("Shutdown hook has been received");
    }

    public boolean checkDbConnection() {
        logger.debug("Check connection to DB ... ");
        try {
            final boolean valid = dataSource.getConnection().isValid(5);
            if (!valid) {
                throw new ConnectionlessException("The DB connection is not established");
            }
            logger.debug("Connected to DB on {}: driver: {}", dataSource.getConnection().getMetaData().getURL(), dataSource.getConnection().getMetaData().getDriverVersion());
            return true;
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
        }
        return false;
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    exchanger.exchange("I came back!");
                    logger.debug("Shutdown hook has been invoked");
                } catch (InterruptedException ignored) {
                }
            }
        });
    }
}