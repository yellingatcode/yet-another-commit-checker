package com.isroot.stash.plugin;

/**
 * @author Sean Ford
 * @since 2013-10-26
 */
public interface JiraService
{
    public boolean doesJiraApplicationLinkExist();
    public boolean doesIssueMatchJqlQuery(String jqlQuery, String issueId);
    public boolean doesIssueExist(String issueId);
}
