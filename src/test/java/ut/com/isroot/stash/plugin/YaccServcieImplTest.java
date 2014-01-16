package ut.com.isroot.stash.plugin;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.stash.content.Changeset;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.RefChangeType;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.user.StashAuthenticationContext;
import com.atlassian.stash.user.StashUser;
import com.google.common.collect.Sets;
import com.isroot.stash.plugin.ChangesetsService;
import com.isroot.stash.plugin.JiraService;
import com.isroot.stash.plugin.YaccService;
import com.isroot.stash.plugin.YaccServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Sean Ford
 * @since 2013-10-26
 */
public class YaccServcieImplTest
{
    @Mock private StashAuthenticationContext stashAuthenticationContext;
    @Mock private ChangesetsService changesetsService;
    @Mock private JiraService jiraService;

    @Mock private Settings settings;
    @Mock private StashUser stashUser;

    private YaccService yaccService;

    @Before
    public void setup()
    {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel","DEBUG");

        MockitoAnnotations.initMocks(this);

        yaccService = new YaccServiceImpl(stashAuthenticationContext, changesetsService, jiraService);

        when(stashAuthenticationContext.getCurrentUser()).thenReturn(stashUser);
	}

    @Test
    public void testCheckRefChange_requestMatchingAuthorName_rejectOnMismatch() throws Exception
    {
        when(settings.getBoolean("requireMatchingAuthorName", false)).thenReturn(true);
        when(stashUser.getDisplayName()).thenReturn("John Smith");

        Changeset changeset = mockChangeset();
        when(changeset.getAuthor().getName()).thenReturn("Incorrect Name");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));

        List<String> errors = yaccService.checkRefChange(null, settings, mockRefChange());
       	assertThat(errors).contains("refs/heads/master: deadbeef: expected author name 'John Smith' but found 'Incorrect Name'");
    }

    @Test
    public void testCheckRefChange_requestMatchingAuthorName_allowOnMatch() throws Exception
    {
        when(settings.getBoolean("requireMatchingAuthorName", false)).thenReturn(true);
        when(stashUser.getDisplayName()).thenReturn("John Smith");

        Changeset changeset = mockChangeset();
        when(changeset.getAuthor().getName()).thenReturn("John Smith");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));

		List<String> errors = yaccService.checkRefChange(null, settings, mockRefChange());
		assertThat(errors).isEmpty();
    }

    @Test
    public void testCheckRefChange_requestMatchingAuthorEmail_rejectOnMismatch() throws Exception
    {
        when(settings.getBoolean("requireMatchingAuthorEmail", false)).thenReturn(true);
        when(stashUser.getEmailAddress()).thenReturn("correct@email.com");

        Changeset changeset = mockChangeset();
        when(changeset.getAuthor().getEmailAddress()).thenReturn("wrong@email.com");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));

		List<String> errors = yaccService.checkRefChange(null, settings, mockRefChange());
		assertThat(errors).contains("refs/heads/master: deadbeef: expected author email 'correct@email.com' but found 'wrong@email.com'");
    }

    @Test
    public void testCheckRefChange_requestMatchingAuthorEmail_allowOnMatch() throws Exception
    {
        when(settings.getBoolean("requireMatchingAuthorEmail", false)).thenReturn(true);
        when(stashUser.getEmailAddress()).thenReturn("correct@email.com");

        Changeset changeset = mockChangeset();
        when(changeset.getAuthor().getEmailAddress()).thenReturn("correct@email.com");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));

		List<String> errors = yaccService.checkRefChange(null, settings, mockRefChange());
		assertThat(errors).isEmpty();
    }

    @Test
    public void testCheckRefChange_requireJiraIssue_rejectIfEnabledButNoJiraLinkExists() throws Exception
    {
        when(settings.getBoolean("requireJiraIssue", false)).thenReturn(true);
        when(jiraService.doesJiraApplicationLinkExist()).thenReturn(false);

        Set<Changeset> changesets = Sets.newHashSet(mockChangeset());
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(changesets);

		List<String> errors = yaccService.checkRefChange(null, settings, mockRefChange());
		assertThat(errors).contains("refs/heads/master: deadbeef: Unable to verify JIRA issue because JIRA Application Link does not exist");
	}

    @Test
    public void testCheckRefChange_requireJiraIssue_rejectIfNoJiraIssuesAreFound() throws Exception
    {
        when(settings.getBoolean("requireJiraIssue", false)).thenReturn(true);
        when(jiraService.doesJiraApplicationLinkExist()).thenReturn(true);

        Changeset changeset = mockChangeset();
        when(changeset.getMessage()).thenReturn("this commit message has no jira issues");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));

		List<String> errors = yaccService.checkRefChange(null, settings, mockRefChange());
		assertThat(errors).contains("refs/heads/master: deadbeef: No JIRA Issue found in commit message.");
    }

    @Test
    public void testCheckRefChange_requireJiraIssue_allowedIfValidJiraIssueIsFound() throws Exception
    {
        when(settings.getBoolean("requireJiraIssue", false)).thenReturn(true);
        when(jiraService.doesJiraApplicationLinkExist()).thenReturn(true);
        when(jiraService.doesIssueExist(anyString())).thenReturn(true);

        Changeset changeset = mockChangeset();
        when(changeset.getMessage()).thenReturn("ABC-123: this commit has valid issue id");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));


		List<String> errors = yaccService.checkRefChange(null, settings, mockRefChange());
		assertThat(errors).isEmpty();
        verify(jiraService).doesJiraApplicationLinkExist();
        verify(jiraService).doesIssueExist("ABC-123");
    }
	
	@Test
	public void testCheckRefChange_requireJiraIssue_errorReturnedIfJiraAuthenticationFails() throws Exception
	{
		when(settings.getBoolean("requireJiraIssue", false)).thenReturn(true);
		when(jiraService.doesJiraApplicationLinkExist()).thenReturn(true);
		when(jiraService.doesIssueExist(anyString())).thenThrow(CredentialsRequiredException.class);

		Changeset changeset = mockChangeset();
		when(changeset.getMessage()).thenReturn("ABC-123: this commit has valid issue id");
		when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));


		List<String> errors = yaccService.checkRefChange(null, settings, mockRefChange());
		assertThat(errors).contains("refs/heads/master: deadbeef: ABC-123: Unable to validate JIRA issue because there was an authentication failure when communicating with JIRA.");
		verify(jiraService).doesIssueExist("ABC-123");
	}

    private Changeset mockChangeset()
    {
        Changeset changeset = mock(Changeset.class, RETURNS_DEEP_STUBS);
        when(changeset.getAuthor().getName()).thenReturn("John Smith");
        when(changeset.getAuthor().getEmailAddress()).thenReturn("jsmith@example.com");
		when(changeset.getId()).thenReturn("deadbeef");
        return changeset;
    }

    private RefChange mockRefChange()
    {
        RefChange refChange = mock(RefChange.class);
        when(refChange.getFromHash()).thenReturn("5773fc438a763e64df8a9c5c32f3b1e83010ada7");
        when(refChange.getToHash()).thenReturn("35d938b060bb361503e021f228e43351f1a71551");
        when(refChange.getRefId()).thenReturn("refs/heads/master");
        when(refChange.getType()).thenReturn(RefChangeType.UPDATE);
        return refChange;
    }
}
