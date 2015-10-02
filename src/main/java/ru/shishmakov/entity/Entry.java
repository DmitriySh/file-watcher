package ru.shishmakov.entity;

import org.joda.time.LocalDateTime;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Dmitriy Shishmakov
 */
@Entity
@Table(name = "entry")
@NamedQueries(value = {
        @NamedQuery(name = "Entry.calculateEntries", query = "select count(e.id) from Entry e"),
        @NamedQuery(name = "Entry.findById", query = "from Entry e where e.id = :id"),})
public class Entry implements Serializable {

    public static interface QueryName {
        String FIND_BY_ID = "Entry.findById";
        String CALCULATE_ENTRIES = "Entry.calculateEntries";
    }

    private static final long serialVersionUID = -6635523387244453196L;

    @Column(name = "id", nullable = false, insertable = true, updatable = false)
    @SequenceGenerator(name = "entry_id_seq", sequenceName = "entry_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entry_id_seq")
    @Id
    private Long id;

    @Column(name = "version", nullable = false, insertable = true, updatable = true)
    @Version
    private long version;

    @Column(name = "content", nullable = false, insertable = true, updatable = false, length = 1024)
    @Basic
    private String content;

    @Column(name = "creation_date", nullable = true, insertable = true, updatable = true)
    @org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime creationDate;

    public Entry() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "Entry [id=" + getId() + "]";
    }
}
