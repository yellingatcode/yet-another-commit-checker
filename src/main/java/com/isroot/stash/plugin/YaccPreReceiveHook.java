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
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
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

        if (hook.isEnabled() && hook.isConfigured()) {
            // Repository hook is configured and enabled.
            // Repository hook overrides default pre-receive hook configuration
            log.debug("PreReceiveRepositoryHook configured. Skip PreReceiveHook");
            return true;
        } else {
            // Repository hook not configured
            log.debug("PreReceiveRepositoryHook not configured. Run PreReceiveHook");

            Settings storedConfig = YaccUtils.buildYaccConfig(pluginSettingsFactory, repositoryHookService);

            log.debug("global settings: {}", storedConfig.asMap());

            if(areThereEnabledSettings(storedConfig.asMap())) {
                return yaccHook.onReceive(new RepositoryHookContext(repository, storedConfig), refChanges, hookResponse);
            } else {
                log.debug("no need to run yacc because no global settings configured");

                return true;
            }
        }
    }

    /**
     * Return true if there are enabled settings, else false. This allows us to only run
     * {@link YaccHook} if there something is enabled. YACC can take a while to run on
     * large repositories, and we don't want to run it globally unless it is actually
     * configured to do something.
     */
    private boolean areThereEnabledSettings(Map<String, Object> settings) {
        for(Map.Entry<String, Object> setting : settings.entrySet()) {
            if(setting.getKey().startsWith("errorMessage")) {
                continue;
            }

            if(setting.getValue() == null) {
                continue;
            }

            String val = setting.getValue().toString();

            if(val.equals("true")) {
                return true;
            }

            // 'false' strings are assumed to be disabled boolean settings, so they are
            // not considered enabled settings.
            if(!val.isEmpty() && !val.equalsIgnoreCase("false")) {
                return true;
            }
        }

        return false;
    }
}
