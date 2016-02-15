package com.isroot.stash.plugin;

import com.isroot.stash.plugin.jira.JiraLookupsException;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Service object to interact with JIRA.
 *
 * @author Sean Ford
 * @since 2013-10-26
 */
public interface JiraService {
    boolean doesJiraApplicationLinkExist();
    boolean doesIssueMatchJqlQuery(String jqlQuery, IssueKey issueKey) throws JiraLookupsException;
    boolean doesIssueExist(IssueKey issueKey) throws JiraLookupsException;
    boolean doesProjectExist(String projectKey) throws JiraLookupsException;
    List<String> checkJqlQuery(@Nonnull String jqlQuery);
}
