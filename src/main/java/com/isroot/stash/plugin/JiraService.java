package com.isroot.stash.plugin;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.sal.api.net.ResponseException;

/**
 * Service object to interact with JIRA.
 *
 * @author Sean Ford
 * @since 2013-10-26
 */
public interface JiraService
{
    public boolean doesJiraApplicationLinkExist();
    public boolean doesIssueMatchJqlQuery(String jqlQuery, String issueId) throws CredentialsRequiredException, ResponseException;
    public boolean doesIssueExist(String issueId) throws CredentialsRequiredException, ResponseException;
}
