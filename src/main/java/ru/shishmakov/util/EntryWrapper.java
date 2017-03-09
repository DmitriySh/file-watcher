package ru.shishmakov.util;

import ru.shishmakov.entity.Entry;

import java.nio.file.Path;

/**
 * Wrapper for source file and his transient entity.
 *
 * @author Dmitriy Shishmakov
 */
public class EntryWrapper {
    private final Path file;
    private final Entry entry;

    private EntryWrapper(Path file, Entry entry) {
        this.file = file;
        this.entry = entry;
    }

    public static EntryWrapper build(Path file, Entry entry) {
        return new EntryWrapper(file, entry);
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
