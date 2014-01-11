package ut.com.isroot.stash.plugin;

import com.atlassian.stash.content.Changeset;
import com.atlassian.stash.hook.HookResponse;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;
import com.atlassian.stash.repository.RefChangeType;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.user.StashAuthenticationContext;
import com.atlassian.stash.user.StashUser;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.isroot.stash.plugin.ChangesetsService;
import com.isroot.stash.plugin.JiraService;
import com.isroot.stash.plugin.YaccHook;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.PrintWriter;
import java.io.StringWriter;
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
public class YaccHookTest
{
    @Mock private StashAuthenticationContext stashAuthenticationContext;
    @Mock private RepositoryHookContext repositoryHookContext;
    @Mock private HookResponse hookResponse;
    @Mock private ChangesetsService changesetsService;
    @Mock private JiraService jiraService;

    @Mock private Settings settings;
    @Mock private StashUser stashUser;

    private StringWriter hookErrOut;

    private YaccHook yaccHook;

    @Before
    public void setup()
    {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel","DEBUG");

        MockitoAnnotations.initMocks(this);

        yaccHook = new YaccHook(stashAuthenticationContext, changesetsService, jiraService);

        when(repositoryHookContext.getSettings()).thenReturn(settings);
        when(stashAuthenticationContext.getCurrentUser()).thenReturn(stashUser);

        hookErrOut = new StringWriter();
        when(hookResponse.err()).thenReturn(new PrintWriter(hookErrOut));
    }

    @Test
    public void testOnReceive_requestMatchingAuthorName_rejectOnMismatch() throws Exception
    {
        when(settings.getBoolean("requireMatchingAuthorName", false)).thenReturn(true);
        when(stashUser.getDisplayName()).thenReturn("John Smith");

        Changeset changeset = mockChangeset();
        when(changeset.getAuthor().getName()).thenReturn("Incorrect Name");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));

        boolean result = yaccHook.onReceive(repositoryHookContext, Lists.newArrayList(mockRefChange()), hookResponse);
        assertThat(result).isFalse();
    }

    @Test
    public void testOnReceive_requestMatchingAuthorName_allowOnMatch() throws Exception
    {
        when(settings.getBoolean("requireMatchingAuthorName", false)).thenReturn(true);
        when(stashUser.getDisplayName()).thenReturn("John Smith");

        Changeset changeset = mockChangeset();
        when(changeset.getAuthor().getName()).thenReturn("John Smith");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));

        boolean result = yaccHook.onReceive(repositoryHookContext, Lists.newArrayList(mockRefChange()), hookResponse);
        assertThat(result).isTrue();
    }

    @Test
    public void testOnReceive_requestMatchingAuthorEmail_rejectOnMismatch() throws Exception
    {
        when(settings.getBoolean("requireMatchingAuthorEmail", false)).thenReturn(true);
        when(stashUser.getEmailAddress()).thenReturn("correct@email.com");

        Changeset changeset = mockChangeset();
        when(changeset.getAuthor().getEmailAddress()).thenReturn("wrong@email.com");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));

        boolean result = yaccHook.onReceive(repositoryHookContext, Lists.newArrayList(mockRefChange()), hookResponse);
        assertThat(result).isFalse();
    }

    @Test
    public void testOnReceive_requestMatchingAuthorEmail_allowOnMatch() throws Exception
    {
        when(settings.getBoolean("requireMatchingAuthorEmail", false)).thenReturn(true);
        when(stashUser.getEmailAddress()).thenReturn("correct@email.com");

        Changeset changeset = mockChangeset();
        when(changeset.getAuthor().getEmailAddress()).thenReturn("correct@email.com");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));

        boolean result = yaccHook.onReceive(repositoryHookContext, Lists.newArrayList(mockRefChange()), hookResponse);
        assertThat(result).isTrue();
    }

    @Test
    public void testOnReceive_requireJiraIssue_rejectIfEnabledButNoJiraLinkExists() throws Exception
    {
        when(settings.getBoolean("requireJiraIssue", false)).thenReturn(true);
        when(jiraService.doesJiraApplicationLinkExist()).thenReturn(false);

        Set<Changeset> changesets = Sets.newHashSet(mockChangeset());
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(changesets);

        boolean result = yaccHook.onReceive(repositoryHookContext, Lists.newArrayList(mockRefChange()), hookResponse);
        assertThat(result).isFalse();
        assertThat(hookErrOut.toString()).contains("Unable to verify JIRA issue because JIRA Application Link does not exist");
    }

    @Test
    public void testOnReceive_requireJiraIssue_rejectIfNoJiraIssuesAreFound() throws Exception
    {
        when(settings.getBoolean("requireJiraIssue", false)).thenReturn(true);
        when(jiraService.doesJiraApplicationLinkExist()).thenReturn(true);

        Changeset changeset = mockChangeset();
        when(changeset.getMessage()).thenReturn("this commit message has no jira issues");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));

        boolean result = yaccHook.onReceive(repositoryHookContext, Lists.newArrayList(mockRefChange()), hookResponse);
        assertThat(result).isFalse();
        assertThat(hookErrOut.toString()).contains("No JIRA Issue found in commit message");
    }

    @Test
    public void testOnReceive_requireJiraIssue_allowedIfValidJiraIssueIsFound() throws Exception
    {
        when(settings.getBoolean("requireJiraIssue", false)).thenReturn(true);
        when(jiraService.doesJiraApplicationLinkExist()).thenReturn(true);
        when(jiraService.doesIssueExist(anyString())).thenReturn(true);

        Changeset changeset = mockChangeset();
        when(changeset.getMessage()).thenReturn("ABC-123: this commit has valid issue id");
        when(changesetsService.getNewChangesets(any(Repository.class), any(RefChange.class))).thenReturn(Sets.newHashSet(changeset));

        boolean result = yaccHook.onReceive(repositoryHookContext, Lists.newArrayList(mockRefChange()), hookResponse);
        assertThat(result).isTrue();
        verify(jiraService).doesJiraApplicationLinkExist();
        verify(jiraService).doesIssueExist("ABC-123");
    }

    private Changeset mockChangeset()
    {
        Changeset changeset = mock(Changeset.class, RETURNS_DEEP_STUBS);
        when(changeset.getAuthor().getName()).thenReturn("John Smith");
        when(changeset.getAuthor().getEmailAddress()).thenReturn("jsmith@example.com");
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
