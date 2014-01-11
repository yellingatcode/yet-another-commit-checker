package com.isroot.stash.plugin;

import com.atlassian.stash.content.Changeset;
import com.atlassian.stash.hook.HookResponse;
import com.atlassian.stash.hook.repository.PreReceiveRepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.RefChangeType;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.user.StashAuthenticationContext;
import com.atlassian.stash.user.StashUser;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Sean Ford
 * @since 2013-05-11
 */
public final class YaccHook implements PreReceiveRepositoryHook
{
    private static final Logger log = LoggerFactory.getLogger(YaccHook.class);

    private final StashAuthenticationContext stashAuthenticationContext;
    private final ChangesetsService changesetsService;
    private final JiraService jiraService;

    public YaccHook(StashAuthenticationContext stashAuthenticationContext, ChangesetsService changesetsService, JiraService jiraService)
    {
        this.stashAuthenticationContext = stashAuthenticationContext;
        this.changesetsService = changesetsService;
        this.jiraService = jiraService;
    }

    @Override
    public boolean onReceive(@Nonnull RepositoryHookContext repositoryHookContext,
                             @Nonnull Collection<RefChange> refChanges, @Nonnull HookResponse hookResponse)
    {
        Settings settings = repositoryHookContext.getSettings();

        StashUser stashUser = stashAuthenticationContext.getCurrentUser();
        log.debug("logged in user name={} email={} displayName={}", stashUser.getName(),
                stashUser.getEmailAddress(), stashUser.getDisplayName());

        List<String> errors = Lists.newArrayList();

        for (RefChange rf : refChanges)
        {
            if (rf.getType() == RefChangeType.DELETE)
            {
                continue;
            }

            log.debug("checking ref change refId={} fromHash={} toHash={} type={}", rf.getRefId(), rf.getFromHash(),
                    rf.getToHash(), rf.getType().toString());


            Set<Changeset> changesets = changesetsService.getNewChangesets(repositoryHookContext.getRepository(), rf);

            for (Changeset changeset : changesets)
            {
                if(settings.getBoolean("excludeMergeCommits", false) && changeset.getParents().size() > 1)
                {
                    log.debug("skipping commit {} because it is a merge commit", changeset.getId());

                    continue;
                }

                for(String e : checkChangeset(settings, changeset))
                {
                    errors.add(String.format("%s: %s: %s", rf.getRefId(), changeset.getId(), e));
                }
            }
        }

        if (errors.isEmpty())
        {
            log.debug("push allowed");

            return true;
        }
        else
        {
            String errorBears = "" +
                    "  (c).-.(c)    (c).-.(c)    (c).-.(c)    (c).-.(c)    (c).-.(c) \n" +
                    "   / ._. \\      / ._. \\      / ._. \\      / ._. \\      / ._. \\ \n" +
                    " __\\( Y )/__  __\\( Y )/__  __\\( Y )/__  __\\( Y )/__  __\\( Y )/__\n" +
                    "(_.-/'-'\\-._)(_.-/'-'\\-._)(_.-/'-'\\-._)(_.-/'-'\\-._)(_.-/'-'\\-._)\n" +
                    "   || E ||      || R ||      || R ||      || O ||      || R ||\n" +
                    " _.' `-' '._  _.' `-' '._  _.' `-' '._  _.' `-' '._  _.' `-' '.\n" +
                    "(.-./`-'\\.-.)(.-./`-`\\.-.)(.-./`-`\\.-.)(.-./`-'\\.-.)(.-./`-`\\.-.)\n" +
                    " `-'     `-'  `-'     `-'  `-'     `-'  `-'     `-'  `-'     `-' \n" +
                    "\n" +
                    "\n" +
                    "Push rejected.\n";

            hookResponse.err().println(errorBears);

            for (String error : errors)
            {
                log.debug("error: {}", error);

                hookResponse.err().println(error);
            }

            log.debug("push rejected");

            return false;
        }
    }

    private List<String> checkChangeset(Settings settings, Changeset changeset)
    {
        log.debug("checking commit id={} name={} email={} message={}", changeset.getId(),
                changeset.getAuthor().getName(), changeset.getAuthor().getEmailAddress(),
                changeset.getMessage());

        List<String> errors = Lists.newArrayList();

        errors.addAll(checkAuthorEmail(settings, changeset));
        errors.addAll(checkAuthorName(settings, changeset));
        errors.addAll(checkCommitMessageRegex(settings, changeset));

        // Checking JIRA issues might be dependent on the commit message regex, so only proceed if there are no errors.
        if(errors.isEmpty())
        {
            errors.addAll(checkJiraIssues(settings, changeset));
        }

        return errors;
    }

    private List<String> checkCommitMessageRegex(Settings settings, Changeset changeset)
    {
        List<String> errors = Lists.newArrayList();

        String regex = settings.getString("commitMessageRegex");
        if(isNullOrEmpty(regex) == false)
        {
            Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(changeset.getMessage());
            if(matcher.matches() == false)
            {
                errors.add("commit message doesn't match regex: " + regex);
            }
        }

        return errors;
    }

    private List<String> extractJiraIssuesFromCommitMessage(Settings settings, Changeset changeset)
    {
        List<String> issues = Lists.newArrayList();

        String message = changeset.getMessage();

        // If a commit message regex is present, see if it contains a group 1 that can be used to located JIRA issues.
        // If not, just ignore it.
        String regex = settings.getString("commitMessageRegex");
        if(isNullOrEmpty(regex) == false)
        {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message);
            if(matcher.matches() && matcher.groupCount() > 0)
            {
                message = matcher.group(1);
            }
        }

        Pattern singleIssuePattern = Pattern.compile("[a-zA-Z]+-[0-9]+");
        Matcher matcher = singleIssuePattern.matcher(message);
        while (matcher.find())
        {
            issues.add(matcher.group());
        }

        log.debug("found jira issues {} from commit message: {}", issues, message);

        return issues;
    }

    private List<String> checkJiraIssues(Settings settings, Changeset changeset)
    {
        if (!settings.getBoolean("requireJiraIssue", false))
        {
            return Lists.newArrayList();
        }

        List<String> errors = Lists.newArrayList();

        if (!jiraService.doesJiraApplicationLinkExist())
        {
            errors.add(String.format("Unable to verify JIRA issue because JIRA Application Link does not exist"));
            return errors;
        }

        List<String> issues = extractJiraIssuesFromCommitMessage(settings, changeset);
        if(issues.isEmpty() == false)
        {
            for(String issueId : issues)
            {
                if (!jiraService.doesIssueExist(issueId))
                {
                    errors.add(String.format("%s: JIRA Issue does not exist", issueId));
                    continue;
                }

                String jqlQuery = settings.getString("issueJqlMatcher");
                if (jqlQuery != null && !jqlQuery.isEmpty())
                {
                    if (!jiraService.doesIssueMatchJqlQuery(jqlQuery, issueId))
                    {
                        errors.add(String.format("%s: JIRA Issue does not match JQL Query: %s", issueId, jqlQuery));
                    }
                }
            }
        }
        else
        {
            errors.add(String.format("No JIRA Issue found in commit message."));
        }

        return errors;
    }

    private List<String> checkAuthorEmail(Settings settings, Changeset changeset)
    {
        StashUser stashUser = stashAuthenticationContext.getCurrentUser();
        final boolean requireMatchingAuthorEmail = settings.getBoolean("requireMatchingAuthorEmail", false);

        List<String> errors = Lists.newArrayList();

        log.debug("requireMatchingAuthorEmail={} authorName={} stashName={}", requireMatchingAuthorEmail, changeset.getAuthor().getEmailAddress(),
                stashUser.getEmailAddress());

        if (requireMatchingAuthorEmail && !changeset.getAuthor().getEmailAddress().equals(stashUser.getEmailAddress()))
        {
            errors.add(String.format("expected author email '%s' but found '%s'", stashUser.getEmailAddress(),
                    changeset.getAuthor().getEmailAddress()));
        }

        return errors;
    }

    private List<String> checkAuthorName(Settings settings, Changeset changeset)
    {
        StashUser stashUser = stashAuthenticationContext.getCurrentUser();
        final boolean requireMatchingAuthorName = settings.getBoolean("requireMatchingAuthorName", false);

        List<String> errors = Lists.newArrayList();

        log.debug("requireMatchingAuthorName={} authorName={} stashName={}", requireMatchingAuthorName, changeset.getAuthor().getName(),
                stashUser.getDisplayName());

        if (requireMatchingAuthorName && !changeset.getAuthor().getName().equals(stashUser.getDisplayName()))
        {
            errors.add(String.format("expected author name '%s' but found '%s'", stashUser.getDisplayName(),
                    changeset.getAuthor().getName()));
        }

        return errors;
    }

}
