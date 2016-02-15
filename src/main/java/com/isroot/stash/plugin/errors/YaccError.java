package com.isroot.stash.plugin.errors;

import javax.annotation.Nullable;

/**
 * @author Sean Ford
 * @since 2015-04-26
 */
public class YaccError {
    public enum Type {
        COMMITTER_NAME,
        COMMITTER_EMAIL,
        COMMIT_REGEX,
        ISSUE_JQL,
        BRANCH_NAME,
        OTHER
    }

    private final Type type;
    private final String message;

    public YaccError(String message) {
        this.type = Type.OTHER;
        this.message = message;
    }

    public YaccError(Type type, String message, @Nullable Object... args) {
        this.type = type;
        this.message = String.format(message, args);
    }

    /**
     * Return new error with text prepended to the original message.
     */
    public YaccError prependText(String text) {
        return new YaccError(type, text + ": " + message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        YaccError yaccError = (YaccError) o;

        if (!message.equals(yaccError.message)) {
            return false;
        }
        if (type != yaccError.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + message.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "YaccError{" +
                "type=" + type +
                ", message='" + message + '\'' +
                '}';
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
