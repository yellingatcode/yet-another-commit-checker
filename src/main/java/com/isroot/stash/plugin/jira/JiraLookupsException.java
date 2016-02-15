package com.isroot.stash.plugin.jira;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseStatusException;
import com.google.common.base.Joiner;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author Bradley Baetz
 */
public class JiraLookupsException extends Exception {
    private final Map<ApplicationLink, Throwable> errors;

    public JiraLookupsException(@Nonnull Map<ApplicationLink, Throwable> errors) {
        checkNotNull(errors);
        checkState(!errors.isEmpty(), "No errors provided");

        this.errors = errors;
    }

    @Nonnull
    public Map<ApplicationLink, Throwable> getErrors() {
        return errors;
    }

    /**
     * FIXME: return YaccError instead of string
     */
    @Nonnull
    public List<String> getPrintableErrors() {
        List<String> ret = new ArrayList<>();

        for (Map.Entry<ApplicationLink, Throwable> entry : errors.entrySet()) {
            String errorStr;

            Throwable ex = entry.getValue();

            if (ex instanceof CredentialsRequiredException) {
                CredentialsRequiredException credentialsRequiredException = (CredentialsRequiredException) ex;
                errorStr = "Could not authenticate. Visit " + credentialsRequiredException.getAuthorisationURI().toASCIIString() + " to link your Stash account to your JIRA account";
            }
            else if (ex instanceof ResponseStatusException) {
                ResponseStatusException responseStatusException = (ResponseStatusException) ex;
                errorStr = responseStatusException.getResponse().getStatusText();
            }
            else if (ex instanceof ResponseException) {
                ResponseException responseException = (ResponseException) ex;
                if (responseException.getCause() != null) {
                    errorStr = responseException.getCause().getMessage();
                }
                else {
                    errorStr = responseException.getMessage();
                }
            }
            else {
                errorStr = "Internal error: " + ex.getMessage() + ". Check server logs for details.";
            }

            ret.add(entry.getKey().getName() + ": " + errorStr);
        }

        return ret;
    }

    @Override
    public String getMessage() {
        return "JIRA lookup errors: " + Joiner.on(", ").join(getPrintableErrors());
    }
}