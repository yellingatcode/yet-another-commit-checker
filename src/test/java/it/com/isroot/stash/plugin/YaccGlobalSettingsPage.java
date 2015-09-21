package it.com.isroot.stash.plugin;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

/**
 * @author Sean Ford
 * @since 2015-08-31
 */
public class YaccGlobalSettingsPage extends YaccSettingsCommon {
    @ElementBy(id = "submit")
    private PageElement submit;

    @Override
    public String getUrl() {
        return "/plugins/servlet/yaccHook/config";
    }

    public YaccSettingsCommon clickSubmit() {
        submit.click();
        waitABitForPageLoad();
        return this;
    }

}
