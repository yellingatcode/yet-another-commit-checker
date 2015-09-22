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

        // temp sleep
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {

        }

        return this;
    }
}
