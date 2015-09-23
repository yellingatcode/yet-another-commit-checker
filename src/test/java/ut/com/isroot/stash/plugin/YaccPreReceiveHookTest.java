package ut.com.isroot.stash.plugin;

import com.atlassian.bitbucket.hook.HookResponse;
import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.hook.repository.RepositoryHookService;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.EscalatedSecurityContext;
import com.atlassian.bitbucket.user.SecurityService;
import com.atlassian.bitbucket.util.UncheckedOperation;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import com.atlassian.sal.testresources.pluginsettings.MockPluginSettingsFactory;
import com.google.common.collect.Lists;
import com.isroot.stash.plugin.YaccConfigServlet;
import com.isroot.stash.plugin.YaccPreReceiveHook;
import com.isroot.stash.plugin.YaccService;
import com.isroot.stash.plugin.errors.YaccError;
import com.isroot.stash.plugin.errors.YaccErrorBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ut.com.isroot.stash.plugin.mock.MockSettingsBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


/**
 * Cloned from YaccHookTest.java and modified to test against the PreReceiveHook interface.
 *
 * @author Jim Bethancourt
 */
public class YaccPreReceiveHookTest {
    @Mock
    private YaccService yaccService;
    @Mock
    private HookResponse hookResponse;
    @Mock
    private RepositoryHookService repositoryHookService;
    @Mock
    private RepositoryHook repositoryHook;
    @Mock
    private Repository repository;
    @Mock
    private SecurityService securityService;
    @Mock
    private EscalatedSecurityContext escalatedSecurityContext;

    @Captor
    private ArgumentCaptor<Settings> settingsCapture;

    private PluginSettingsFactory pluginSettingsFactory;

    private Map<String, Object> globalSettingsMap = new HashMap<>();

    private StringWriter errorMessage;
    private YaccPreReceiveHook yaccPreReceiveHook;

    @Before
    public void setup() throws Throwable {
        MockitoAnnotations.initMocks(this);

        pluginSettingsFactory = new MockPluginSettingsFactory();

        yaccPreReceiveHook = new YaccPreReceiveHook(yaccService,
                pluginSettingsFactory, securityService, repositoryHookService);

        //mock hook retrieval
        when(securityService.withPermission(Permission.REPO_ADMIN, "Get plugin configuration"))
                .thenReturn(escalatedSecurityContext);
        when(escalatedSecurityContext.call(any(UncheckedOperation.class))).thenReturn(repositoryHook);

        when(repositoryHookService.createSettingsBuilder()).thenReturn(new MockSettingsBuilder());

        errorMessage = new StringWriter();
        when(hookResponse.err()).thenReturn(new PrintWriter(errorMessage));

        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        pluginSettings.put(YaccConfigServlet.SETTINGS_MAP, globalSettingsMap);
    }

    @Test
    public void testOnReceive_repositoryHookConfigured() {
        when(repositoryHook.isConfigured()).thenReturn(true);
        when(repositoryHook.isEnabled()).thenReturn(true);

        RefChange refChange = mock(RefChange.class);
        boolean allowed = yaccPreReceiveHook.onReceive(repository, Lists.newArrayList(refChange), null);
        assertThat(allowed).isTrue();
    }

    @Test
    public void testOnReceive_deleteRefChangesIgnored() {
        RefChange refChange = mock(RefChange.class);
        when(refChange.getType()).thenReturn(RefChangeType.DELETE);

        boolean allowed = yaccPreReceiveHook.onReceive(repository, Lists.newArrayList(refChange), null);
        assertThat(allowed).isTrue();
        verifyZeroInteractions(yaccService);
    }

    @Test
    public void testOnReceive_NullRefChangesIgnored() {
        RefChange refChange = mock(RefChange.class);
        when(refChange.getType()).thenReturn(RefChangeType.ADD);
        when(refChange.getToHash()).thenReturn("0000000000000000000000000000000000000000");

        boolean allowed = yaccPreReceiveHook.onReceive(repository, Lists.newArrayList(refChange), null);
        assertThat(allowed).isTrue();
        verifyZeroInteractions(yaccService);
    }

    @Test
    public void testOnReceive_pushRejectedIfThereAreErrors() {
        when(yaccService.checkRefChange(any(Repository.class), any(Settings.class), any(RefChange.class)))
                .thenReturn(Lists.newArrayList(new YaccError("error with commit")));

        boolean allowed = yaccPreReceiveHook.onReceive(repository, Lists.newArrayList(mock(RefChange.class)), hookResponse);
        assertThat(allowed).isFalse();
    }

    @Test
    public void testOnReceive_errorsArePrintedToHookStdErr() {
        when(yaccService.checkRefChange(any(Repository.class), any(Settings.class), any(RefChange.class)))
                .thenReturn(Lists.newArrayList(new YaccError("error1"), new YaccError("error2")));

        yaccPreReceiveHook.onReceive(repository, getMockRefChanges(), hookResponse);

        assertThat(errorMessage.toString())
                .isEqualTo(YaccErrorBuilder.ERROR_BEARS + "\n" +
                        "\n" +
                        "refs/heads/master: error1\n" +
                        "\n" +
                        "refs/heads/master: error2\n" +
                        "\n");
    }

    @Test
    public void testOnReceive_defaultHeaderDisplayedIfErrorMessageHeaderIsEmpty() {
        when(yaccService.checkRefChange(any(Repository.class), any(Settings.class), any(RefChange.class)))
                .thenReturn(Lists.newArrayList(new YaccError("error1")));

        globalSettingsMap.put("errorMessageHeader", "");

        yaccPreReceiveHook.onReceive(repository, getMockRefChanges(), hookResponse);

        assertThat(errorMessage.toString()).startsWith(YaccErrorBuilder.ERROR_BEARS);
    }

    @Test
    public void testOnReceive_nonEmptyErrorMessageHeaderReplacesDefaultHeader() {
        when(yaccService.checkRefChange(any(Repository.class), any(Settings.class), any(RefChange.class)))
                .thenReturn(Lists.newArrayList(new YaccError("error1")));

        globalSettingsMap.put("errorMessageHeader", "Custom Header");

        yaccPreReceiveHook.onReceive(repository, getMockRefChanges(), hookResponse);

        assertThat(errorMessage.toString()).isEqualTo("Custom Header\n" +
                "\n" +
                "refs/heads/master: error1\n\n");
    }

    @Test
    public void testOnReceive_errorMessageFooterAddedToEndOfOutput() {
        when(yaccService.checkRefChange(any(Repository.class), any(Settings.class), any(RefChange.class)))
                .thenReturn(Lists.newArrayList(new YaccError("error1")));

        globalSettingsMap.put("errorMessageFooter", "Custom Footer");

        yaccPreReceiveHook.onReceive(repository, getMockRefChanges(), hookResponse);

        assertThat(errorMessage.toString()).endsWith("\nCustom Footer\n\n");
    }

    @Test
    public void testOnReceive_nullSettingsMap_hookWorksBeforeItHasBeenConfigured() {
        pluginSettingsFactory.createGlobalSettings().put(YaccConfigServlet.SETTINGS_MAP, null);

        boolean isPushAllowed = yaccPreReceiveHook.onReceive(repository, getMockRefChanges(), hookResponse);

        assertThat(isPushAllowed).isTrue();
    }

    @Test
    public void testOnReceive_globalHookSettingsPassedToHook() {
        globalSettingsMap.put("commitMessageRegex", "bar");
        globalSettingsMap.put("requireMatchingAuthorEmail", "true");

        yaccPreReceiveHook.onReceive(repository, getMockRefChanges(), hookResponse);

        verify(yaccService).checkRefChange(eq(repository), settingsCapture.capture(), any(RefChange.class));

        Settings hookSettings = settingsCapture.getValue();

        assertThat(hookSettings.asMap())
                .contains(
                        entry("commitMessageRegex", "bar"),
                        entry("requireMatchingAuthorEmail", true));
    }

    private List<RefChange> getMockRefChanges() {
        List<RefChange> refChanges = new ArrayList<RefChange>();
        RefChange refChange = mock(RefChange.class);

        when(refChange.getRefId()).thenReturn("refs/heads/master");

        refChanges.add(refChange);
        return refChanges;
    }

}
