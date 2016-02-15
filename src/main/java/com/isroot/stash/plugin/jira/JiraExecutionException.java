package com.isroot.stash.plugin.jira;

import com.atlassian.sal.api.net.ResponseException;
import com.google.common.base.Joiner;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This class exists because the standard ResponseStatus class doesn't
 * allow access to the error body (it returns null, because the stream is
 * not parsed when the exception is thrown)
 *
 * @author Bradley Baetz
 */
public class JiraExecutionException extends ResponseException {
    private final List<String> errors;

    public JiraExecutionException(@Nonnull String message, @Nonnull List<String> errors) {
        super(message);
        this.errors = errors;
    }

    @Nonnull
    public List<String> getJiraErrors() {
        return errors;
    }

    @Override
    public String getMessage() {
        return Joiner.on(", ").join(errors);
    }
}