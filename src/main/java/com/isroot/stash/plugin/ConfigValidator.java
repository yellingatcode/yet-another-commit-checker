package com.isroot.stash.plugin;

import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.RepositorySettingsValidator;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;

import javax.annotation.Nonnull;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author sdford
 * @since 2013-05-11
 */
public class ConfigValidator implements RepositorySettingsValidator
{
    private final JiraService jiraService;

    public ConfigValidator(JiraService jiraService)
    {
        this.jiraService = jiraService;
    }

    @Override
    public void validate(@Nonnull Settings settings, @Nonnull SettingsValidationErrors settingsValidationErrors,
                         @Nonnull Repository repository)
    {
        String commitMessageRegex = settings.getString("commitMessageRegex");
        if(isNullOrEmpty(commitMessageRegex) == false)
        {
            try
            {
                Pattern.compile(commitMessageRegex);
            }
            catch(PatternSyntaxException ex)
            {
                settingsValidationErrors.addFieldError("commitMessageRegex", "Invalid Regex: " + ex.getMessage());
            }
        }

        if(settings.getBoolean("requireJiraIssue", false))
        {
            if(jiraService.doesJiraApplicationLinkExist() == false)
            {
                settingsValidationErrors.addFieldError("requireJiraIssue", "Can't be enabled because a JIRA application link does not exist.");
            }
        }

        // TODO: validate JQL matcher
    }
}
