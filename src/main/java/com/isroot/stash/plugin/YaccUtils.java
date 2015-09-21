package com.isroot.stash.plugin;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.atlassian.stash.setting.Settings;

/**
 * @author Hiroyuki Wada
 */
public class YaccUtils {

    public static Settings buildYaccConfig(PluginSettingsFactory pluginSettingsFactory,
            RepositoryHookService repositoryHookService) {
        Map<String, Object> settingsMap = getSettingsMap(pluginSettingsFactory);
        HashMap<String, Object> config = new HashMap<>();
        for (String fieldName : settingsMap.keySet()) {
            addFieldValueToPluginConfigMap(settingsMap, config, fieldName);
        }
        return repositoryHookService.createSettingsBuilder().addAll(config).build();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getSettingsMap(PluginSettingsFactory pluginSettingsFactory) {
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();

        Map<String, Object> settingsMap = (HashMap<String, Object>) pluginSettings.get(YaccConfigServlet.SETTINGS_MAP);

        if (settingsMap == null) {
            settingsMap = new HashMap<>();
        }

        return settingsMap;
    }

    private static void addFieldValueToPluginConfigMap(Map<String, Object> settingsMap, HashMap<String, Object> config,
            String fieldName) {
        String value = (String) settingsMap.get(fieldName);
        if (value != null && (value.equals("on") || value.equals("true"))) { // handle "on" value
            config.put(fieldName, true);
        } else if (value != null && !value.isEmpty()) {
            config.put(fieldName, value);
        }
    }
}
