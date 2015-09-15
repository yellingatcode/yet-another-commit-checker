package com.isroot.stash.plugin;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.setting.SettingsValidationErrors;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Uldis Ansmits
 * @author Jim Bethancourt
 */
public class YaccConfigServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(YaccConfigServlet.class);
    public static final String SETTINGS_MAP = "com.isroot.stash.plugin.yacc.settings";

    private final RepositoryHookService repositoryHookService;
    final private SoyTemplateRenderer soyTemplateRenderer;
    private final NavBuilder navBuilder;
    private ConfigValidator configValidator;
    private Map<String, String> fields;
    private Map<String, Iterable<String>> fieldErrors;
    private final PluginSettings pluginSettings;
    private HashMap<String, Object> settingsMap;

    public YaccConfigServlet(SoyTemplateRenderer soyTemplateRenderer,
                             PluginSettingsFactory pluginSettingsFactory,
                             JiraService jiraService,
                             RepositoryHookService repositoryHookService,
                             NavBuilder navBuilder) {
        this.soyTemplateRenderer = soyTemplateRenderer;
        this.navBuilder = navBuilder;
        this.repositoryHookService = repositoryHookService;

        pluginSettings = pluginSettingsFactory.createGlobalSettings();

        configValidator = new ConfigValidator(jiraService);

        fields = new HashMap<>();
        fieldErrors = new HashMap<>();
    }


    @SuppressWarnings("unchecked")
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        log.debug("doGet");
        settingsMap = (HashMap<String, Object>) pluginSettings.get(SETTINGS_MAP);
        if (settingsMap == null) {
            settingsMap = new HashMap<>();
        }

        validateSettings();
        doGetContinue(resp);
    }

    public void validateSettings() {
        Settings settings = repositoryHookService.createSettingsBuilder()
                .addAll(settingsMap)
                .build();
        configValidator.validate(settings, new SettingsValidationErrorsImpl(fieldErrors), null);
    }

    protected void doGetContinue(HttpServletResponse resp) throws IOException, ServletException {
        log.debug("doGetContinue");
        fields.clear();

        for (Map.Entry<String, Object> entry : settingsMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            log.debug("got plugin config " + key + "=" + value + " " + value.getClass().getName());
            if (value instanceof String) {
                fields.put(key, (String) value);
            }
        }

        log.debug("Config fields: " + fields);
        log.debug("Field errors: " + fieldErrors);

        resp.setContentType("text/html;charset=UTF-8");
        try {
            soyTemplateRenderer.render(resp.getWriter(), "com.isroot.stash.plugin.yacc:yaccHook-config-serverside", "com.atlassian.stash.repository.hook.ref.config",
                    ImmutableMap
                            .<String, Object>builder()
                            .put("config", fields)
                            .put("errors", fieldErrors)
                            .build()
            );
        } catch (SoyException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new ServletException(e);
        }

    }

    public void addStringFieldValue(Map<String, Object> settingsMap, HttpServletRequest req, String fieldName) {
        String o;
        o = req.getParameter(fieldName);
        if (o != null && !o.isEmpty()) settingsMap.put(fieldName, o);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        settingsMap.clear();

        for (Object key : req.getParameterMap().keySet()) {
            String parameterName = (String) key;

            // Plugin settings persister only supports map of strings
            if (!parameterName.equals("submit")) {
                addStringFieldValue(settingsMap, req, parameterName);
            }
        }

        try {
            validateSettings();
        } catch (Exception e) {
            //exceptions are dealt with as field errors
        }

        if (fieldErrors.size() > 0) {
            doGetContinue(resp);
            return;
        }

        if(log.isDebugEnabled()) {
            for (Map.Entry<String, Object> entry : settingsMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                log.debug("save plugin config " + key + "=" + value + " " + value.getClass().getName());
            }
        }

        pluginSettings.put(SETTINGS_MAP, settingsMap);

        String redirectUrl;
        redirectUrl = navBuilder.addons().buildRelative();
        log.debug("redirect: " + redirectUrl);
        resp.sendRedirect(redirectUrl);
    }

    private static class SettingsValidationErrorsImpl implements SettingsValidationErrors {

        Map<String, Iterable<String>> fieldErrors;

        public SettingsValidationErrorsImpl(Map<String, Iterable<String>> fieldErrors) {
            this.fieldErrors = fieldErrors;
            this.fieldErrors.clear();
        }

        @Override
        public void addFieldError(String fieldName, String errorMessage) {
            fieldErrors.put(fieldName, new ArrayList<>(Collections.singletonList(errorMessage)));
        }

        @Override
        public void addFormError(String errorMessage) {
            //not implemented
        }
    }
}