package ru.shishmakov.util;

import ru.shishmakov.entity.Entry;

import java.nio.file.Path;

/**
 * Wrapper for source file and his transient entity.
 * It is like a boat sends the soul of data to the world of the dead.
 *
 * @author Dmitriy Shishmakov
 */
public class CharonBoat {

    private Path file;
    private Entry entry;

    private CharonBoat(Path file, Entry entry) {
        this.file = file;
        this.entry = entry;
    }

    public static CharonBoat build(Path file, Entry entry) {
        return new CharonBoat(file, entry);
    }

    public Path getFile() {
        return file;
    }

    public Entry getEntry() {
        return entry;
    }

    @Override
    public String toString() {
        return "{" +
                "file=" + file.getFileName() +
                ", entry=" + entry +
                '}';
    }
}
