package ru.shishmakov;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.shishmakov.config.ServerConfig;
import ru.shishmakov.core.Server;

import java.lang.invoke.MethodHandles;

/**
 * Main class to launch the {@link Server}.
 *
 * @author Dmitriy Shishmakov
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(ServerConfig.class);
            context.refresh();

            final Server server = context.getBean(Server.class);
            server.start();
            server.await();
        } catch (Exception e) {
            logger.error("The server failure: " + e.getMessage(), e);
        }
    }
}
