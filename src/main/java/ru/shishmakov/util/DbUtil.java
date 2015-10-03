package ru.shishmakov.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.shishmakov.core.exception.ConnectionlessException;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;

/**
 * @author Dmitriy Shishmakov
 */
public class DbUtil {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static boolean hasDbConnection(DataSource dataSource) {
        logger.debug("Check connection to DB ... ");
        try {
            if (!dataSource.getConnection().isValid(5)) {
                throw new ConnectionlessException("The DB connection is not established");
            }
            return true;
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
        }
        return false;
    }
}
