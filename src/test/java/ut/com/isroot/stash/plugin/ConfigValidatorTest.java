package ut.com.isroot.stash.plugin;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;
import com.isroot.stash.plugin.ConfigValidator;
import com.isroot.stash.plugin.IssueKey;
import com.isroot.stash.plugin.JiraService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Sean Ford
 * @since 2014-01-13
 */
public class ConfigValidatorTest
{
	@Mock private JiraService jiraService;
	@Mock private Settings settings;
	@Mock private SettingsValidationErrors settingsValidationErrors;
	@Mock private Repository repository;


	private ConfigValidator configValidator;

	@Before
	public void setup()
	{
		MockitoAnnotations.initMocks(this);

		configValidator = new ConfigValidator(jiraService);
	}

	@Test
	public void testValidate_errorIfRequireJiraIssueIsOnAndNoAppLink()
	{
		when(settings.getBoolean("requireJiraIssue", false)).thenReturn(true);
		when(jiraService.doesJiraApplicationLinkExist()).thenReturn(false);

		configValidator.validate(settings, settingsValidationErrors, repository);

		verify(settingsValidationErrors).addFieldError("requireJiraIssue", "Can't be enabled because a JIRA application link does not exist.");
	}

	@Test
	public void testValidate_authenticationErrorWhenValidatingQueryReturnsError() throws Exception
	{
		when(settings.getString("issueJqlMatcher")).thenReturn("assignee is not empty");
		when(jiraService.doesIssueMatchJqlQuery(anyString(), any(IssueKey.class))).thenThrow(CredentialsRequiredException.class);

		configValidator.validate(settings, settingsValidationErrors, repository);

		verify(settings).getString("issueJqlMatcher");
		verify(jiraService).doesIssueMatchJqlQuery("assignee is not empty", new IssueKey("ABC-123"));
		verify(settingsValidationErrors).addFieldError("issueJqlMatcher", "Unable to validate JQL query with JIRA. Authentication failure when communicating with JIRA.");
	}

	@Test
	public void testValidate_invalidJqlQueryAddsValidationError() throws Exception
	{
		when(settings.getString("issueJqlMatcher")).thenReturn("this jql query is invalid");
		when(jiraService.doesIssueMatchJqlQuery(anyString(), any(IssueKey.class))).thenThrow(IllegalArgumentException.class);

		configValidator.validate(settings, settingsValidationErrors, repository);

		verify(settings).getString("issueJqlMatcher");
		verify(jiraService).doesIssueMatchJqlQuery("this jql query is invalid", new IssueKey("ABC-123"));
		verify(settingsValidationErrors).addFieldError("issueJqlMatcher", "The JQL query syntax is invalid.");
	}

	@Test
	public void testValidate_validJqlQueryIsAccepted() throws Exception
	{
		when(settings.getString("issueJqlMatcher")).thenReturn("assignee is not empty");

		configValidator.validate(settings, settingsValidationErrors, repository);

		verify(settings).getString("issueJqlMatcher");
		verify(jiraService).doesIssueMatchJqlQuery("assignee is not empty", new IssueKey("ABC-123"));
		verifyZeroInteractions(settingsValidationErrors);
	}
}
