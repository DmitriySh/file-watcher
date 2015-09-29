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
        Server server = null;
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        try {
            context.register(ServerConfig.class);
            context.refresh();

            server = context.getBean(Server.class);
            server.start();
            server.await();
            System.out.println("after await");
        } catch (Throwable e) {
            logger.error("The server failure: " + e.getMessage(), e);
        }finally {
            System.out.println("finally");
            // spring context already closed
            if(server != null){
                server.stop(context);
            }else {
                logger.info("NULLLLLLLLLL");
            }
        }
        System.out.println("after try");
    }
}
