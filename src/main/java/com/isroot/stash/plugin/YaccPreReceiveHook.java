package com.isroot.stash.plugin;

import com.atlassian.bitbucket.hook.HookResponse;
import com.atlassian.bitbucket.hook.PreReceiveHook;
import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.hook.repository.RepositoryHookContext;
import com.atlassian.bitbucket.hook.repository.RepositoryHookService;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.user.SecurityService;
import com.atlassian.bitbucket.util.UncheckedOperation;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Uldis Ansmits
 * @author Jim Bethancourt
 *
 * System-wide pre-receive hook.  Will defer to the local repository YACC hook configuration if present.
 */
public class YaccPreReceiveHook implements PreReceiveHook {

    private static final Logger log = LoggerFactory.getLogger(YaccPreReceiveHook.class);

    private final YaccHook yaccHook;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final SecurityService securityService;
    private final RepositoryHookService repositoryHookService;


    public YaccPreReceiveHook(YaccService yaccService,
                              PluginSettingsFactory pluginSettingsFactory,
                              SecurityService securityService,
                              RepositoryHookService repositoryHookService) {
        yaccHook = new YaccHook(yaccService);
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.securityService = securityService;
        this.repositoryHookService = repositoryHookService;
    }

    @Override
    public boolean onReceive(@Nonnull final Repository repository, @Nonnull Collection<RefChange> refChanges, @Nonnull HookResponse hookResponse) {

        RepositoryHook hook = securityService.withPermission(Permission.REPO_ADMIN, "Get plugin configuration").call(new UncheckedOperation<RepositoryHook>() {
            public RepositoryHook perform() {
                return repositoryHookService.getByKey(repository, "com.isroot.stash.plugin.yacc:yaccHook");
            }
        });

        Settings settings = repositoryHookService.createSettingsBuilder().build(); // generate a default settings object
        if (hook.isEnabled() && hook.isConfigured()) {
            // Repository hook is configured and enabled.
            // Repository hook overrides default pre-receive hook configuration
            log.debug("PreReceiveRepositoryHook configured. Skip PreReceiveHook");
            return true;
        } else {
            // Repository hook not configured
            log.debug("PreReceiveRepositoryHook not configured. Run PreReceiveHook");
            PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();

            Map<String, Object> settingsMap =
                    (HashMap<String, Object>) pluginSettings.get(YaccConfigServlet.SETTINGS_MAP);

            Settings storedConfig = buildYaccConfig(settingsMap);

            if (storedConfig != null) {
                settings = storedConfig;
            }
        }

        return yaccHook.onReceive(new RepositoryHookContext(repository, settings), refChanges, hookResponse);
    }

    Settings buildYaccConfig(Map<String, Object> settingsMap) {
        HashMap<String, Object> config = new HashMap<String, Object>();
        for (String fieldName : settingsMap.keySet()) {
            addFieldValueToPluginConfigMap(settingsMap, config, fieldName);
        }

        return repositoryHookService.createSettingsBuilder().addAll(config).build();
    }

    void addFieldValueToPluginConfigMap(Map<String, Object> settingsMap, HashMap<String, Object> config, String fieldName) {
        String value = (String) settingsMap.get(fieldName);
        if (value != null && (value.equals("on") || value.equals("true"))) { // handle "on" value
            config.put(fieldName, true);
        } else if (value != null && !value.isEmpty()) {
            config.put(fieldName, value);
        }
    }
}
