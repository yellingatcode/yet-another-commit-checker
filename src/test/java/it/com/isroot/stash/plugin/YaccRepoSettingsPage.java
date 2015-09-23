package it.com.isroot.stash.plugin;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

/**
 * @author Sean Ford
 * @since 2015-09-13
 */
public class YaccRepoSettingsPage extends YaccSettingsCommon {
    @ElementBy(cssSelector = "tr[data-key=\"com.isroot.stash.plugin.yacc:yaccHook\"] .edit-button")
    private PageElement editYacc;

    @ElementBy(cssSelector = "tr[data-key=\"com.isroot.stash.plugin.yacc:yaccHook\"] .mode-disabled")
    private PageElement disableYacc;

    @ElementBy(className = "button-panel-submit-button")
    private PageElement submitButton;

    @Override
    public String getUrl() {
        return "/projects/PROJECT_1/repos/rep_1/settings/hooks";
    }

    public YaccRepoSettingsPage clickEditYacc() {
        editYacc.click();
        return this;
    }

    public YaccRepoSettingsPage clickSubmit() {
        submitButton.click();
        waitABitForPageLoad();
        return this;
    }

    public YaccRepoSettingsPage clickDisable() {
        disableYacc.click();
        return this;
    }
}
