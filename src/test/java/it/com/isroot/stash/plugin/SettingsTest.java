package it.com.isroot.stash.plugin;

import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.stash.async.AsyncTestUtils;
import com.atlassian.stash.async.WaitCondition;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import com.atlassian.webdriver.stash.StashTestedProduct;
import com.atlassian.webdriver.stash.page.StashLoginPage;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Integration test to verify that both the YACC global and repo settings page are in sync and both working.
 *
 * @author Sean Ford
 * @since 2015-08-30
 */
public class SettingsTest {
    private static final StashTestedProduct STASH = TestedProductFactory.create(StashTestedProduct.class);

    @BeforeClass
    public static void setup() {
        waitForStashToBoot();
    }

    @After
    public void cleanup() {
        STASH.getTester().getDriver().manage().deleteAllCookies();
    }

    @Test
    public void testGlobalSettings() {
        YaccGlobalSettingsPage globalSettings = STASH.visit(StashLoginPage.class)
                .loginAsSysAdmin(YaccGlobalSettingsPage.class);

        verifyDefaults(globalSettings);
        setValues(globalSettings);
        globalSettings.clickSubmit();

        globalSettings = STASH.visit(YaccGlobalSettingsPage.class);

        verifyValues(globalSettings);
    }

    @Test
    public void testRepositorySettings() {
        YaccRepoSettingsPage repoSettingsPage = STASH.visit(StashLoginPage.class)
                .loginAsSysAdmin(YaccRepoSettingsPage.class)
                .clickEditYacc();

        verifyDefaults(repoSettingsPage);
        setValues(repoSettingsPage);
        repoSettingsPage.clickSubmit();

        repoSettingsPage = STASH.visit(YaccRepoSettingsPage.class)
                .clickEditYacc();

        verifyValues(repoSettingsPage);
    }

    private void verifyDefaults(YaccSettingsCommon yaccSettingsCommon) {
        yaccSettingsCommon.verifyRequireMatchingAuthorName(false)
                .verifyRequireMatchingAuthorEmail(false)
                .verifyCommitMessageRegex("")
                .verifyErrorMessageHeader("")
                .verifyErrorMessageCommitterEmail("")
                .verifyErrorMessageCommitterName("")
                .verifyErrorMessageCommitRegex("")
                .verifyErrorMessageIssueJql("")
                .verifyErrorMessageBranchName("")
                .verifyErrorMessageFooter("")
                .verifyExcludeMergeCommits(false)
                .verifyExcludeByRegex("")
                .verifyExcludeMergeCommits(false);
    }

    private void setValues(YaccSettingsCommon yaccSettingsCommon) {
        yaccSettingsCommon.clickRequireMatchingAuthorEmail()
                .clickRequireMatchingAuthorName()
                .setCommitMessageRegex(".*")
                .setErrorMessageHeader("header")
                .setErrorMessageCommitterEmail("email")
                .setErrorMessageCommitterName("name")
                .setErrorMessageCommitRegex("commit regex")
                .setErrorMessageIssueJql("issue jql")
                .setErrorMessageBranchName("branch name")
                .setErrorMessageFooter("footer")
                .clickExcludeMergeCommits()
                .setExcludeByRegex(".*")
                .clickExcludeServiceUserCommits();
    }

    private void verifyValues(YaccSettingsCommon yaccSettingsCommon) {
        yaccSettingsCommon.verifyRequireMatchingAuthorEmail(true)
                .verifyRequireMatchingAuthorName(true)
                .verifyCommitMessageRegex(".*")
                .verifyErrorMessageHeader("header")
                .verifyErrorMessageCommitterEmail("email")
                .verifyErrorMessageCommitterName("name")
                .verifyErrorMessageCommitRegex("commit regex")
                .verifyErrorMessageIssueJql("issue jql")
                .verifyErrorMessageBranchName("branch name")
                .verifyErrorMessageFooter("footer")
                .verifyExcludeMergeCommits(true)
                .verifyExcludeByRegex(".*")
                .verifyExcludeMergeCommits(true);
    }


    private static void waitForStashToBoot() {
        AsyncTestUtils.waitFor(new WaitCondition() {
            @Override
            public boolean test() throws Exception {
                WebDriverTester tester = STASH.getTester();

                tester.gotoUrl(System.getProperty("http.stash.url") + "/status");
                return tester.getDriver().getPageSource().contains("RUNNING");
            }

            @Override
            public void describeFailure(Description description) throws Exception {

            }
        }, 600000, 5000);
    }
}
