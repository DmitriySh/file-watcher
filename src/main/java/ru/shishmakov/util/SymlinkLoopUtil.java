package ru.shishmakov.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dmitriy Shishmakov
 */
public final class SymlinkLoopUtil {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static boolean isNotSymbolicLinkLoop(Path path) {
        return !isSymbolicLinkLoop(path);
    }

    public static boolean isSymbolicLinkLoop(Path path) {
        if (!Files.isSymbolicLink(path)) {
            return false;
        }
        logger.debug("Check symlink ... ");
        try {
            final Path link = path.normalize();
            final Path target = Files.readSymbolicLink(link);
            logger.debug("Target of link \'{}\' -> \'{}\'", link, target);
            if (Files.isSymbolicLink(target)) {
                final Map<String, String> map = new LinkedHashMap<>();
                map.put(link.toString(), link.toString());
                map.put(target.toString(), target.toString());
                return linkLoop(target, map);
            }
        } catch (Exception ignored) {
            return true;
        }
        return false;
    }

    private static boolean linkLoop(Path link, Map<String, String> links) throws IOException {
        final Path target = Files.readSymbolicLink(link);
        if (!Files.isSymbolicLink(target)) {
            return false;
        }
        if (hasLinks(target, links)) {
            final StringBuilder builder = new StringBuilder(links.size() * 10);
            for (String path : links.values()) {
                builder.append("\'").append(path).append("\'").append(" -> ");
            }
            builder.append("\'").append(target).append("\'");
            logger.debug("Loop symlink {}", builder);
            return true;
        }
        links.put(target.toString(), target.toString());
        return linkLoop(target, links);
    }

    private static boolean hasLinks(Path target, Map<String, String> links) {
        return StringUtils.equalsIgnoreCase(links.get(target.toString()), target.toString());
    }
}
