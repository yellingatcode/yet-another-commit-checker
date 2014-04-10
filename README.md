# Yet Another Commit Checker

- [About](#about)
- [Quick Start](#quick-start)
    - [Configuration Settings](#configuration-settings)
- [Troubleshooting](#troubleshooting)
- [Future Work](#future-work)
    - [Known Issues](#known-issues)
    - [TODO](#todo)
- [Development](#development)
    - [Enable Logging](#enable-logging)
    - [Original Atlassian README](#original-atlassian-readme)

## About

This is an Atlassian Stash plugin that enforces commit message requirements. If a commit violates the
configured policies, the push to the repository will be rejected.

Features:

* Fully configurable!
* Require commit author name and email to match Stash user
* Require commit messages to match regex
* Require commit message to contain valid JIRA issue ids
* Issue JQL matcher to validate JIRA issue against. Require issues to be assigned, not closed, in a certain project, etc.
The possibilities are endless!
* No extra JIRA configuration is required. Will use existing JIRA Application Link!
* Branch friendly! Only *new* commits are checked. Commits that already exist in the repository will be skipped.

Questions? Comments? Found a bug? See https://github.com/sford/yet-another-commit-checker!

Author: [Sean Ford](https://github.com/sford)

## Quick Start

1. Install YACC plugin into Stash
2. If you want to require valid JIRA issues, configure a JIRA Application Link in Stash
4. Configure YACC in the Hook Settings for a repository

#### Configuration Settings

**Require Matching Author Email**

If enabled, commit author email doesn't match the email of the Stash user.

**Require Matching Author Name**

If enabled, commit author name doesn't match the name of the Stash user.

**Commit Message Regex**

If a regex is present, commit message must match regex.

Example,

    [A-Z0-9\-]+: .*

will require commit message to be in the form of:

    PROJ-123: added new feature xyz

**Require Valid JIRA Issue(s)**

If enabled, commit messages must contain valid JIRA issue ids. JIRA issue ids are defined as any item that matches
the regex `[A-Z][A-Z_0-9]+-[0-9]+`.

This check requires JIRA to be first linked with Stash using an Application Link. See https://confluence.atlassian.com/display/STASH/Linking+Stash+with+JIRA.

*Note:* This may have false positives if the commit message contains strings that look like JIRA issue, for example, `UTF-8`.
This can be avoided by including a regex group in the `Commit Message Regex`. If a group is present, JIRA issues will only be extracted
from the group.

For example,

    ([A-Z0-9\-]+): .*

will only check to see if `PROJ-123` is a valid JIRA issue in the following commit message

    PROJ-123: fixed bug involving UTF-8 support. I deserve a HIGH-5 for this fix!

**Exclude Merge Commits**

If enabled, merge commits will be excluded from commit requirements.

**Issue JQL Matcher**

If JQL query is present, detected JIRA issues must match this query.

For example,

     assignee is not empty and status="in progress" and project=PROJ

will require that JIRA issues be assigned, in progess, and from project PROJ.

See [JIRA Advanced Searching](https://confluence.atlassian.com/display/JIRA/Advanced+Searching) for documentation regarding writing and testing
JQL queries.

## Troubleshooting

#### I am getting a JIRA authentication failed message when attempting to push my code or when trying to configure an issue JQL matcher.

This can occur if Stash is configured to use OAuth to authenticate with JIRA and the currently logged in Stash user has
not yet gone through the OAuth authorization process to allow Stash access to JIRA.

To initialize the OAuth tokens, go into the Stash UI and do something that requires access to JIRA. For example, view
the commits for a repository and click on an linked JIRA issue for an existing commit. See the [Stash JIRA Integration](https://confluence.atlassian.com/display/STASH/JIRA+integration#JIRAintegration-SeetheJIRAissuesrelatedtocommitsandpullrequests)
for an example of this.

There might be a better way to do this, but this what has worked for me :-)

## Future Work

#### Known Issues

1. Issue JQL Matcher: Paging through search results not yet implemented. If query returns more than 50 issues, YACC
may not find the issue.
2. Commit Message Regex: If regex is set, Git revert commits might be rejected because Git sets the commit message of the
revert by default.

#### TODO

1. More unit tests!
2. Fix known issues
3. Add more awesome features

## Development

Interested in contributing? [Fork me!](https://github.com/sford/yet-another-commit-checker)

Some useful development information:

#### Enable Logging

Enabling YACC logging can be done using the Stash REST API. For example, see the following `curl` command which enables logging in the `atlas-run` development environment:

    curl -u admin -v -X PUT -d "" -H "Content-Type: application/json" http://localhost:7990/stash/rest/api/latest/logs/logger/com.isroot/debug

#### Original Atlassian README

This is the original Atlassian README with instructions on how to run it in the Atlassian SDK

    You have successfully created an Atlassian Plugin!

    Here are the SDK commands you'll use immediately:

    * atlas-run   -- installs this plugin into the product and starts it on localhost
    * atlas-debug -- same as atlas-run, but allows a debugger to attach at port 5005
    * atlas-cli   -- after atlas-run or atlas-debug, opens a Maven command line window:
                     - 'pi' reinstalls the plugin into the running product instance
    * atlas-help  -- prints description for all commands in the SDK

    Full documentation is always available at:

    https://developer.atlassian.com/display/DOCS/Introduction+to+the+Atlassian+Plugin+SDK

