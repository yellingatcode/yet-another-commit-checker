package com.isroot.stash.plugin;

/**
 * Minimal metadata required to verify a changeset.
 */
public class YaccChangeset {
    /**
     * Construct a new changeset instance.
     *
     * @param id Changeset ID (eg, Git hash).
     * @param committer The changeset committer.
     * @param message Git commit message.
     * @param parentCount The number of parent commits listed in this commit.
     */
    public YaccChangeset (String id, YaccPerson committer, String message, int parentCount) {
        this.id = id;
        this.committer = committer;
        this.message = message;
        this.parentCount = parentCount;
    }

    /**
     * Return the git changeset ID.
     *
     * @return Git changeset ID.
     */
    public String getId() {
        return id;
    }


    /**
     * Return the git committer identity associated with this changeset.
     *
     * @return Git committer identity.
     */
    public YaccPerson getCommitter() {
        return committer;
    }

    /**
     * Return the commit message associated with this changeset.
     *
     * @return Commit message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Return the number of parent commits listed in this commit.
     *
     * @return Number of parents.
     */
    public int getParentCount() {
        return parentCount;
    }

    private final String id;
    private final YaccPerson committer;
    private final String message;
    private final int parentCount;
}
