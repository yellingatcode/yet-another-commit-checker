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
import com.isroot.stash.plugin.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Sean Ford
 * @since 2013-10-26
 */
public class YaccServiceImplTest
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

        YaccChangeset changeset = mockChangeset();
        when(changeset.getCommitter().getName()).thenReturn("Incorrect Name");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));

        List<String> errors = yaccService.checkRefChange(null, settings, mockRefChange());
       	assertThat(errors).contains("refs/heads/master: deadbeef: expected author name 'John Smith' but found 'Incorrect Name'");
    }

    @Test
    public void testCheckRefChange_requestMatchingAuthorName_allowOnMatch() throws Exception
    {
        when(settings.getBoolean("requireMatchingAuthorName", false)).thenReturn(true);
        when(stashUser.getDisplayName()).thenReturn("John Smith");

        YaccChangeset changeset = mockChangeset();
        when(changeset.getCommitter().getName()).thenReturn("John Smith");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));

		List<String> errors = yaccService.checkRefChange(null, settings, mockRefChange());
		assertThat(errors).isEmpty();
    }

    @Test
    public void testCheckRefChange_requestMatchingAuthorEmail_rejectOnMismatch() throws Exception
    {
        when(settings.getBoolean("requireMatchingAuthorEmail", false)).thenReturn(true);
        when(stashUser.getEmailAddress()).thenReturn("correct@email.com");

        YaccChangeset changeset = mockChangeset();
        when(changeset.getCommitter().getEmailAddress()).thenReturn("wrong@email.com");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));

		List<String> errors = yaccService.checkRefChange(null, settings, mockRefChange());
		assertThat(errors).contains("refs/heads/master: deadbeef: expected author email 'correct@email.com' but found 'wrong@email.com'");
    }

    @Test
    public void testCheckRefChange_requestMatchingAuthorEmail_allowOnMatch() throws Exception
    {
        when(settings.getBoolean("requireMatchingAuthorEmail", false)).thenReturn(true);
        when(stashUser.getEmailAddress()).thenReturn("correct@email.com");

        YaccChangeset changeset = mockChangeset();
        when(changeset.getCommitter().getEmailAddress()).thenReturn("correct@email.com");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));

		List<String> errors = yaccService.checkRefChange(null, settings, mockRefChange());
		assertThat(errors).isEmpty();
    }

    @Test
    public void testCheckRefChange_requireJiraIssue_rejectIfEnabledButNoJiraLinkExists() throws Exception
    {
        when(settings.getBoolean("requireJiraIssue", false)).thenReturn(true);
        when(jiraService.doesJiraApplicationLinkExist()).thenReturn(false);

        Set<YaccChangeset> changesets = Sets.newHashSet(mockChangeset());
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(changesets);

		List<String> errors = yaccService.checkRefChange(null, settings, mockRefChange());
		assertThat(errors).contains("refs/heads/master: deadbeef: Unable to verify JIRA issue because JIRA Application Link does not exist");
	}

    @Test
    public void testCheckRefChange_requireJiraIssue_rejectIfNoJiraIssuesAreFound() throws Exception
    {
        when(settings.getBoolean("requireJiraIssue", false)).thenReturn(true);
        when(jiraService.doesJiraApplicationLinkExist()).thenReturn(true);

        YaccChangeset changeset = mockChangeset();
        when(changeset.getMessage()).thenReturn("this commit message has no jira issues. abc-123 is not a valid issue because it is lowercase.");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));

		List<String> errors = yaccService.checkRefChange(null, settings, mockRefChange());
		assertThat(errors).contains("refs/heads/master: deadbeef: No JIRA Issue found in commit message.");
    }

    @Test
    public void testCheckRefChange_requireJiraIssue_ignoreUnknownJiraProjectKeys() throws Exception
    {
        when(settings.getBoolean("requireJiraIssue", false)).thenReturn(true);
        when(settings.getBoolean("ignoreUnknownIssueProjectKeys", false)).thenReturn(true);

        when(jiraService.doesJiraApplicationLinkExist()).thenReturn(true);
        when(jiraService.doesIssueExist(new IssueKey("ABC-123"))).thenReturn(true);
        when(jiraService.doesIssueExist(new IssueKey("UTF-123"))).thenReturn(false);
        when(jiraService.doesProjectExist("ABC")).thenReturn(true);
        when(jiraService.doesProjectExist("UTF")).thenReturn(false);

        YaccChangeset changeset = mockChangeset();
        when(changeset.getMessage()).thenReturn("ABC-123: this commit has valid issue id and an invalid issue id of UTF-8");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));


        List<String> errors = yaccService.checkRefChange(null, settings, mockRefChange());
        assertThat(errors).isEmpty();
        verify(jiraService).doesJiraApplicationLinkExist();
        verify(jiraService).doesIssueExist(new IssueKey("ABC-123"));
    }

    @Test
    public void testCheckRefChange_requireJiraIssue_allowedIfValidJiraIssueIsFound() throws Exception
    {
        when(settings.getBoolean("requireJiraIssue", false)).thenReturn(true);
        when(jiraService.doesJiraApplicationLinkExist()).thenReturn(true);
        when(jiraService.doesIssueExist(any(IssueKey.class))).thenReturn(true);

        YaccChangeset changeset = mockChangeset();
        when(changeset.getMessage()).thenReturn("ABC-123: this commit has valid issue id");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));


		List<String> errors = yaccService.checkRefChange(null, settings, mockRefChange());
		assertThat(errors).isEmpty();
        verify(jiraService).doesJiraApplicationLinkExist();
        verify(jiraService).doesIssueExist(new IssueKey("ABC-123"));
	}

    @Test
    public void testCheckRefChange_requireJiraIssue_jiraIssueIdsAreExtractedFromCommitMessage() throws Exception
    {
        when(settings.getBoolean("requireJiraIssue", false)).thenReturn(true);
        when(jiraService.doesJiraApplicationLinkExist()).thenReturn(true);

        YaccChangeset changeset = mockChangeset();
        when(changeset.getMessage()).thenReturn("these issue ids should be extracted: ABC-123, ABC_D-123, ABC2-123");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));

        yaccService.checkRefChange(null, settings, mockRefChange());
        verify(jiraService).doesIssueExist(new IssueKey("ABC-123"));
        verify(jiraService).doesIssueExist(new IssueKey("ABC_D-123"));
        verify(jiraService).doesIssueExist(new IssueKey("ABC2-123"));
    }

    @Test
    public void testCheckRefChange_requireJiraIssue_errorReturnedIfJiraAuthenticationFails() throws Exception
    {
        when(settings.getBoolean("requireJiraIssue", false)).thenReturn(true);
        when(jiraService.doesJiraApplicationLinkExist()).thenReturn(true);
        when(jiraService.doesIssueExist(any(IssueKey.class))).thenThrow(CredentialsRequiredException.class);

        YaccChangeset changeset = mockChangeset();
        when(changeset.getMessage()).thenReturn("ABC-123: this commit has valid issue id");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));


        List<String> errors = yaccService.checkRefChange(null, settings, mockRefChange());
        assertThat(errors).contains("refs/heads/master: deadbeef: ABC-123: Unable to validate JIRA issue because there was an authentication failure when communicating with JIRA.");
        verify(jiraService).doesIssueExist(new IssueKey("ABC-123"));
    }

    private YaccChangeset mockChangeset()
    {
        YaccChangeset changeset = mock(YaccChangeset.class, RETURNS_DEEP_STUBS);
        when(changeset.getCommitter().getName()).thenReturn("John Smith");
        when(changeset.getCommitter().getEmailAddress()).thenReturn("jsmith@example.com");
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
