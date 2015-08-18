package ut.com.isroot.stash.plugin;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsBuilder;
import com.google.common.collect.ImmutableMap;
import com.isroot.stash.plugin.JiraService;
import com.isroot.stash.plugin.YaccConfigServlet;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by jimb on 8/17/2015.
 */
public class YaccConfigServletTest {

    @Mock private SoyTemplateRenderer soyTemplateRenderer;
    @Mock private PluginSettingsFactory pluginSettingsFactory;
    @Mock private PluginSettings pluginSettings;
    @Mock private JiraService jiraService;
    @Mock private RepositoryHookService repositoryHookService;
    @Mock private NavBuilder navBuilder;
    @Mock private NavBuilder.Addons addons;
    @Mock private SettingsBuilder settingsBuilder;
    @Mock private Settings settings;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    YaccConfigServlet yaccConfigServlet;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(pluginSettingsFactory.createGlobalSettings()).thenReturn(pluginSettings);
        when(pluginSettings.get(anyString())).thenReturn(new HashMap<String, String>());

        when(repositoryHookService.createSettingsBuilder()).thenReturn(settingsBuilder);
        when(settingsBuilder.build()).thenReturn(settings);
        when(settingsBuilder.addAll(anyMap())).thenReturn(settingsBuilder);

        when(navBuilder.addons()).thenReturn(addons);
        when(addons.buildRelative()).thenReturn("/yaccHook/config");

        yaccConfigServlet = new YaccConfigServlet(soyTemplateRenderer,
                pluginSettingsFactory, jiraService, repositoryHookService, navBuilder);
    }

    @Test
    public void testValidateSettings() {
        yaccConfigServlet.validateSettings();
        verify(repositoryHookService, times(1)).createSettingsBuilder();
    }

    @Test
    public void testDoGet() throws IOException, ServletException, SoyException {
        yaccConfigServlet.doGet(request, response);
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("config", new HashMap());
        map.put("errors", new HashMap());
        verify(soyTemplateRenderer, times(1)).render(null,
                "com.isroot.stash.plugin.yacc:yaccHook-config-serverside", "com.atlassian.stash.repository.hook.ref.config", map);
    }

    @Test
    public void testDoPostNoParams() throws IOException, ServletException {
        yaccConfigServlet.doGet(request, response); // calling doGet to populate the settings map
        yaccConfigServlet.doPost(request, response);
        verify(response, times(1)).sendRedirect("/yaccHook/config");
    }

    @Test
    public void testDoPostWithParams() throws IOException, ServletException {
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        parameterMap.put("requireMatchingAuthorEmail", "true");
        parameterMap.put("requireMatchingAuthorName", "true");
        parameterMap.put("branchNameRegex", "master");

        when(pluginSettings.get(anyString())).thenReturn(parameterMap);

        yaccConfigServlet = new YaccConfigServlet(soyTemplateRenderer,
                pluginSettingsFactory, jiraService, repositoryHookService, navBuilder);

        when(request.getParameterMap()).thenReturn(parameterMap);

        yaccConfigServlet.doGet(request, response); // calling doGet to populate the settings map
        yaccConfigServlet.doPost(request, response);

        spy(yaccConfigServlet).addStringFieldValue(parameterMap, request, "branchNameRegex");
        verify(response, times(1)).sendRedirect("/yaccHook/config");
    }

}
