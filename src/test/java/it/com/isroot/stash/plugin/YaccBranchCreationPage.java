package it.com.isroot.stash.plugin;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.webdriver.bitbucket.page.BranchCreationPage;

/**
 * @author Sean Ford
 * @since 2015-09-21
 */
public class YaccBranchCreationPage extends BranchCreationPage {
    @ElementBy(cssSelector = ".aui-flag li")
    private PageElement errorFlag;

    @ElementBy(id = "create-branch-submit")
    private PageElement submit;

    @Override
    public String getUrl() {
        return "/plugins/servlet/create-branch?repoId=1&branchFrom=refs%2Fheads%2Fmaster";
    }

    public YaccBranchCreationPage createBranchWithError() {
        Poller.waitUntilTrue(submit.timed().isEnabled());
        submit.click();
        Poller.waitUntilTrue(errorFlag.timed().isPresent());
        Poller.waitUntilTrue(errorFlag.timed().hasText("Invalid branch name"));

        return this;
    }

    public String getError() {
        return errorFlag.getText();
    }
}
